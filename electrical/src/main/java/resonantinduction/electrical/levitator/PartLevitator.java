package resonantinduction.electrical.levitator;

import java.lang.ref.WeakReference;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFluid;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.BlockVine;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.IFluidBlock;
import resonantinduction.core.MultipartUtility;
import resonantinduction.core.Settings;
import resonantinduction.core.prefab.part.PartFace;
import resonantinduction.electrical.Electrical;
import resonantinduction.electrical.tesla.TileTesla;
import universalelectricity.api.vector.Vector3;
import universalelectricity.api.vector.VectorWorld;
import calclavia.lib.render.EnumColor;
import calclavia.lib.utility.LinkUtility;
import calclavia.lib.utility.WrenchUtility;
import calclavia.lib.utility.inventory.InventoryUtility;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.multipart.TMultiPart;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartLevitator extends PartFace
{
	private int pushDelay;

	private AxisAlignedBB operationBounds;
	private AxisAlignedBB suckBounds;

	/**
	 * true = suck, false = push
	 */
	public boolean input = true;

	/**
	 * Pathfinding
	 */
	private ThreadLevitatorPathfinding thread;
	private PathfinderLevitator pathfinder;
	private WeakReference<PartLevitator> linked;
	private int lastCalcTime = 0;

	/** Color of beam */
	private int dyeID = TileTesla.DEFAULT_COLOR;

	/**
	 * Linking
	 */
	private byte saveLinkSide;
	private VectorWorld saveLinkVector;

	/**
	 * Client Side Only
	 */
	public float renderRotation = 0;

	@Override
	public boolean activate(EntityPlayer player, MovingObjectPosition part, ItemStack itemStack)
	{
		if (WrenchUtility.isWrench(itemStack))
		{
			if (tryLink(LinkUtility.getLink(itemStack), LinkUtility.getSide(itemStack)))
			{
				if (world().isRemote)
					player.addChatMessage("Successfully linked devices.");
				LinkUtility.clearLink(itemStack);
			}
			else
			{
				if (world().isRemote)
					player.addChatMessage("Marked link for device.");

				LinkUtility.setLink(itemStack, new VectorWorld(world(), x(), y(), z()));
				LinkUtility.setSide(itemStack, (byte) placementSide.ordinal());
			}

			return true;
		}

		if (player.getCurrentEquippedItem() != null)
		{
			if (player.getCurrentEquippedItem().itemID == Item.dyePowder.itemID)
			{
				setDye(player.getCurrentEquippedItem().getItemDamage());

				if (!player.capabilities.isCreativeMode)
				{
					player.inventory.decrStackSize(player.inventory.currentItem, 1);
				}

				return true;
			}
		}

		if (player.isSneaking())
			input = !input;

		updateBounds();
		updatePath();

		return true;
	}

	/**
	 * Link methods
	 */
	public boolean tryLink(VectorWorld linkVector, byte side)
	{
		if (linkVector != null)
		{
			TMultiPart part = MultipartUtility.getMultipart(world(), linkVector, side);

			if (part instanceof PartLevitator)
			{
				setLink((PartLevitator) part, true);
			}

			return true;
		}

		return false;
	}

	public PartLevitator getLink()
	{
		return linked != null && linked.get() != null && linked.get().world() != null ? linked.get() : null;
	}

	@Override
	public void onEntityCollision(Entity entity)
	{
		/**
		 * Attempt to pull items in.
		 */
		if (!world().isRemote && input && canFunction() && entity instanceof EntityItem)
		{
			EntityItem item = (EntityItem) entity;
			IInventory inventory = (IInventory) getLatched();

			ItemStack remains = InventoryUtility.putStackInInventory(inventory, item.getEntityItem(), placementSide.getOpposite().getOpposite().ordinal(), false);

			if (remains == null)
			{
				item.setDead();
			}
			else
			{
				item.setEntityItemStack(remains);
			}

			// TODO: Add redstone pulse and reaction?
		}
	}

	@Override
	public void update()
	{
		if (ticks % 60 == 0)
			updateBounds();

		super.update();

		pushDelay = Math.max(0, pushDelay - 1);

		/**
		 * Try to use temp link vectors from save/loads to link.
		 */
		if (saveLinkVector != null)
		{
			tryLink(saveLinkVector, saveLinkSide);
			saveLinkVector = null;
		}

		if (canFunction())
		{
			IInventory inventory = (IInventory) getLatched();

			/**
			 * Place items or take items from the inventory into the world.
			 */
			if (!input)
			{
				renderRotation = Math.min(20, renderRotation + 0.8f);

				/**
				 * Attempt to push items out.
				 */
				if (pushDelay == 0)
				{
					ItemStack retrieved = InventoryUtility.takeTopItemFromInventory(inventory, placementSide.getOpposite().getOpposite().ordinal());

					if (retrieved != null)
					{
						EntityItem item = getItemWithPosition(retrieved);

						if (!world().isRemote)
						{
							world().spawnEntityInWorld(item);
						}

						pushDelay = Settings.LEVITATOR_PUSH_DELAY;
					}
				}
			}
			else if (input)
			{
				renderRotation = Math.max(0, renderRotation - 0.8f);
			}

			final int renderPeriod = 1;
			final boolean renderBeam = ticks % renderPeriod == 0 && hasLink() && getLink().input != input;

			if (!input)
			{
				if (hasLink())
				{
					if (getLink().input)
					{
						/**
						 * Linked usage.
						 */
						if (thread != null)
						{
							PathfinderLevitator newPath = thread.getPath();

							if (newPath != null)
							{
								pathfinder = newPath;
								pathfinder.results.add(getPosition());
								thread = null;
							}
						}

						// Push entity along path.
						if (pathfinder != null)
						{
							List<Vector3> results = pathfinder.results;

							/**
							 * Draw default beams.
							 */
							if (renderBeam)
							{
								Electrical.proxy.renderElectricShock(world(), getBeamSpawnPosition(), getPosition().translate(0.5), EnumColor.DYES[dyeID].toColor(), world().rand.nextFloat() > 0.9);
								Electrical.proxy.renderElectricShock(world(), getLink().getPosition().translate(0.5), getLink().getBeamSpawnPosition(), EnumColor.DYES[dyeID].toColor(), world().rand.nextFloat() > 0.9);
							}

							for (int i = 0; i < results.size(); i++)
							{
								Vector3 result = results.get(i).clone();

								if (canBeMovePath(world(), result))
								{
									if (i - 1 >= 0)
									{
										Vector3 prevResult = results.get(i - 1).clone();

										Vector3 difference = prevResult.clone().difference(result);
										final ForgeDirection direction = difference.toForgeDirection();

										if (renderBeam)
											Electrical.proxy.renderElectricShock(world(), prevResult.clone().translate(0.5), result.clone().translate(0.5), EnumColor.DYES[dyeID].toColor(), world().rand.nextFloat() > 0.9);

										AxisAlignedBB bounds = AxisAlignedBB.getAABBPool().getAABB(result.x, result.y, result.z, result.x + 1, result.y + 1, result.z + 1);
										List<EntityItem> entities = world().getEntitiesWithinAABB(EntityItem.class, bounds);

										for (EntityItem entityItem : entities)
										{
											moveEntity(entityItem, direction, result);
										}
									}

								}
								else
								{
									updatePath();
									break;
								}
							}
						}
						else
						{
							updatePath();
						}
					}
				}
				else if (operationBounds != null)
				{
					/**
					 * Non-linked usage.
					 */
					for (EntityItem entityItem : (List<EntityItem>) world().getEntitiesWithinAABB(EntityItem.class, operationBounds))
					{
						moveEntity(entityItem, placementSide.getOpposite(), getPosition());
					}

					if (ticks % renderPeriod == 0)
						Electrical.proxy.renderElectricShock(world(), getBeamSpawnPosition(), new Vector3(operationBounds.maxX - 0.5 - placementSide.offsetX / 3f, operationBounds.maxY - 0.5 - placementSide.offsetY / 3f, operationBounds.maxZ - 0.5 - placementSide.offsetZ / 3f), EnumColor.DYES[dyeID].toColor(), world().rand.nextFloat() > 0.9);
				}
			}

			lastCalcTime--;
		}
	}

	public boolean canBeMovePath(World world, Vector3 position)
	{
		TMultiPart partSelf = MultipartUtility.getMultipart(new VectorWorld(world, position), placementSide.ordinal());
		if (partSelf == this)
			return true;

		TMultiPart partLink = MultipartUtility.getMultipart(new VectorWorld(world, position), getLink().placementSide.ordinal());
		if (partLink == getLink())
			return true;

		return canBePath(world, position);
	}

	public static boolean canBePath(World world, Vector3 position)
	{
		Block block = Block.blocksList[position.getBlockID(world)];
		return block == null || (block instanceof BlockSnow || block instanceof BlockVine || block instanceof BlockLadder || ((block instanceof BlockFluid || block instanceof IFluidBlock) && block.blockID != Block.lavaMoving.blockID && block.blockID != Block.lavaStill.blockID));
	}

	private boolean hasLink()
	{
		return getLink() != null && getLink().getLink() == this;
	}

	private void moveEntity(EntityItem entityItem, ForgeDirection direction, Vector3 lockVector)
	{
		switch (direction)
		{
			case DOWN:
				entityItem.setPosition(lockVector.x + 0.5, entityItem.posY, lockVector.z + 0.5);

				entityItem.motionX = 0;
				entityItem.motionZ = 0;

				if (!input)
				{
					entityItem.motionY = Math.max(-Settings.LEVITATOR_MAX_SPEED, entityItem.motionY - Settings.LEVITATOR_ACCELERATION);
				}
				else
				{
					entityItem.motionY = Math.min(Settings.LEVITATOR_MAX_SPEED, entityItem.motionY + .04 + Settings.LEVITATOR_ACCELERATION);
				}

				break;
			case UP:

				entityItem.setPosition(lockVector.x + 0.5, entityItem.posY, lockVector.z + 0.5);

				entityItem.motionX = 0;
				entityItem.motionZ = 0;

				if (!input)
				{
					entityItem.motionY = Math.min(Settings.LEVITATOR_MAX_SPEED, entityItem.motionY + .04 + Settings.LEVITATOR_ACCELERATION);
				}
				else
				{
					entityItem.motionY = Math.max(-Settings.LEVITATOR_MAX_SPEED, entityItem.motionY - Settings.LEVITATOR_ACCELERATION);
				}

				break;
			case NORTH:

				entityItem.setPosition(lockVector.x + 0.5, lockVector.y + 0.5, entityItem.posZ);

				entityItem.motionX = 0;
				entityItem.motionY = 0;

				if (!input)
				{
					entityItem.motionZ = Math.max(-Settings.LEVITATOR_MAX_SPEED, entityItem.motionZ - Settings.LEVITATOR_ACCELERATION);
				}
				else
				{
					entityItem.motionZ = Math.min(Settings.LEVITATOR_MAX_SPEED, entityItem.motionZ + Settings.LEVITATOR_ACCELERATION);
				}

				break;
			case SOUTH:

				entityItem.setPosition(lockVector.x + 0.5, lockVector.y + 0.5, entityItem.posZ);

				entityItem.motionX = 0;
				entityItem.motionY = 0;

				if (!input)
				{
					entityItem.motionZ = Math.min(Settings.LEVITATOR_MAX_SPEED, entityItem.motionZ + Settings.LEVITATOR_ACCELERATION);
				}
				else
				{
					entityItem.motionZ = Math.max(-Settings.LEVITATOR_MAX_SPEED, entityItem.motionZ - Settings.LEVITATOR_ACCELERATION);
				}

				break;
			case WEST:

				entityItem.setPosition(entityItem.posX, lockVector.y + 0.5, lockVector.z + 0.5);

				entityItem.motionY = 0;
				entityItem.motionZ = 0;

				if (!input)
				{
					entityItem.motionX = Math.max(-Settings.LEVITATOR_MAX_SPEED, entityItem.motionX - Settings.LEVITATOR_ACCELERATION);
				}
				else
				{
					entityItem.motionX = Math.min(Settings.LEVITATOR_MAX_SPEED, entityItem.motionX + Settings.LEVITATOR_ACCELERATION);
				}

				break;
			case EAST:
				entityItem.setPosition(entityItem.posX, lockVector.y + 0.5, lockVector.z + 0.5);

				entityItem.motionY = 0;
				entityItem.motionZ = 0;

				if (!input)
				{
					entityItem.motionX = Math.min(Settings.LEVITATOR_MAX_SPEED, entityItem.motionX + Settings.LEVITATOR_ACCELERATION);
				}
				else
				{
					entityItem.motionX = Math.max(-Settings.LEVITATOR_MAX_SPEED, entityItem.motionX - Settings.LEVITATOR_ACCELERATION);
				}

				break;
			default:
				break;
		}

		entityItem.ticksExisted = 1;
		entityItem.isAirBorne = true;
		entityItem.delayBeforeCanPickup = 1;
		entityItem.age = Math.max(entityItem.age - 1, 0);
	}

	private EntityItem getItemWithPosition(ItemStack toSend)
	{
		EntityItem item = new EntityItem(world(), x() + 0.5, y() + 0.5, z() + 0.5, toSend);
		item.motionX = 0;
		item.motionY = 0;
		item.motionZ = 0;
		return item;
	}

	public void updateBounds()
	{
		suckBounds = operationBounds = null;

		ForgeDirection dir = placementSide.getOpposite();
		MovingObjectPosition mop = world().clip(getPosition().translate(dir).toVec3(), getPosition().translate(dir, Settings.LEVITATOR_MAX_REACH).toVec3());

		int reach = Settings.LEVITATOR_MAX_REACH;

		if (mop != null)
		{
			if (MultipartUtility.getMultipart(world(), mop.blockX, mop.blockY, mop.blockZ, placementSide.getOpposite().ordinal()) instanceof PartLevitator)
			{
				reach = (int) Math.min(getPosition().distance(new Vector3(mop.hitVec)), reach);

				if (dir.offsetX + dir.offsetY + dir.offsetZ < 0)
				{
					operationBounds = AxisAlignedBB.getBoundingBox(x() + dir.offsetX * reach, y() + dir.offsetY * reach, z() + dir.offsetZ * reach, x() + 1, y() + 1, z() + 1);
					suckBounds = AxisAlignedBB.getBoundingBox(x() + dir.offsetX, y() + dir.offsetY, z() + dir.offsetZ, x() + 1, y() + 1, z() + 1);
				}
				else
				{
					operationBounds = AxisAlignedBB.getBoundingBox(x(), y(), z(), x() + 1 + dir.offsetX * reach, y() + 1 + dir.offsetY * reach, z() + 1 + dir.offsetZ * reach);
					suckBounds = AxisAlignedBB.getBoundingBox(x(), y(), z(), x() + 1 + dir.offsetX, y() + 1 + dir.offsetY, z() + 1 + dir.offsetZ);
				}
			}
		}
	}

	@Override
	public void onNeighborChanged()
	{
		super.onNeighborChanged();
		updateBounds();
	}

	public boolean canFunction()
	{
		return isLatched() && !world().isBlockIndirectlyGettingPowered(x(), y(), z());
	}

	public boolean isLatched()
	{
		return getLatched() != null;
	}

	public TileEntity getLatched()
	{
		ForgeDirection side = placementSide;

		TileEntity tile = world().getBlockTileEntity(x() + side.offsetX, y() + side.offsetY, z() + side.offsetZ);

		if (tile instanceof IInventory)
		{
			return tile;
		}

		return null;
	}

	@Override
	public void readDesc(MCDataInput packet)
	{
		super.readDesc(packet);
		input = packet.readBoolean();
		dyeID = packet.readByte();

		if (packet.readBoolean())
		{
			saveLinkVector = new VectorWorld(packet.readNBTTagCompound());
			saveLinkSide = packet.readByte();
		}
	}

	@Override
	public void writeDesc(MCDataOutput packet)
	{
		super.writeDesc(packet);
		packet.writeBoolean(input);
		packet.writeByte(dyeID);

		if (getLink() != null)
		{
			packet.writeBoolean(true);
			NBTTagCompound nbt = new NBTTagCompound();
			new VectorWorld(getLink().world(), getLink().x(), getLink().y(), getLink().z()).writeToNBT(nbt);
			packet.writeNBTTagCompound(nbt);
			packet.writeByte(getLink().placementSide.ordinal());
		}
		else
		{
			packet.writeBoolean(false);
		}

	}

	@Override
	public void load(NBTTagCompound nbt)
	{
		super.load(nbt);

		this.input = nbt.getBoolean("suck");
		this.dyeID = nbt.getInteger("dyeID");

		if (nbt.hasKey("link"))
		{
			saveLinkVector = new VectorWorld(nbt.getCompoundTag("link"));
			saveLinkSide = nbt.getByte("linkSide");
		}
	}

	@Override
	public void save(NBTTagCompound nbt)
	{
		super.save(nbt);

		nbt.setBoolean("suck", input);
		nbt.setInteger("dyeID", dyeID);

		if (getLink() != null && getLink().world() != null)
		{
			nbt.setCompoundTag("link", new VectorWorld(getLink().world(), getLink().x(), getLink().y(), getLink().z()).writeToNBT(new NBTTagCompound()));
			nbt.setByte("linkSide", (byte) getLink().placementSide.ordinal());
		}
	}

	/**
	 * Link between two TileEntities, do pathfinding operation.
	 */
	public void setLink(PartLevitator levitator, boolean setOpponent)
	{
		if (getLink() != null && setOpponent)
		{
			getLink().setLink(null, false);
		}

		linked = new WeakReference(levitator);

		if (setOpponent)
		{
			getLink().setLink(this, false);
		}

		updatePath();
	}

	public void updatePath()
	{
		if (thread == null && getLink() != null && lastCalcTime <= 0)
		{
			pathfinder = null;

			Vector3 start = getPosition();
			Vector3 target = new Vector3(getLink().x(), getLink().y(), getLink().z());

			if (start.distance(target) < Settings.MAX_LEVITATOR_DISTANCE)
			{
				if (canBeMovePath(world(), start) && canBeMovePath(world(), target))
				{
					thread = new ThreadLevitatorPathfinding(new PathfinderLevitator(world(), target), start);
					thread.start();
					lastCalcTime = 40;
				}
			}
		}
	}

	public void setDye(int dye)
	{
		dyeID = dye;
		world().markBlockForUpdate(x(), y(), z());
	}

	public Vector3 getPosition()
	{
		return new Vector3(x(), y(), z());
	}

	public Vector3 getBeamSpawnPosition()
	{
		return new Vector3(x() + 0.5 + placementSide.offsetX / 3f, y() + 0.5 + placementSide.offsetY / 3f, z() + 0.5 + placementSide.offsetZ / 3f);
	}

	@Override
	protected ItemStack getItem()
	{
		return new ItemStack(Electrical.itemLevitator);
	}

	@Override
	public String getType()
	{
		return "resonant_induction_levitator";
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderDynamic(codechicken.lib.vec.Vector3 pos, float frame, int pass)
	{
		if (pass == 0)
		{
			RenderLevitator.INSTANCE.render(this, pos.x, pos.y, pos.z);
		}
	}
}
