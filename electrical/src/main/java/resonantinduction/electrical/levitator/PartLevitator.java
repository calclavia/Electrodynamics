package resonantinduction.electrical.levitator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFluid;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.BlockVine;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.IFluidBlock;
import resonantinduction.core.MultipartUtility;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.Settings;
import resonantinduction.core.prefab.part.PartFace;
import resonantinduction.electrical.Electrical;
import resonantinduction.electrical.tesla.TileTesla;
import resonantinduction.electrical.transformer.RenderTransformer;
import universalelectricity.api.vector.Vector3;
import universalelectricity.api.vector.VectorWorld;
import calclavia.components.tool.ToolModeLink;
import calclavia.lib.prefab.block.ILinkable;
import calclavia.lib.render.EnumColor;
import calclavia.lib.utility.WrenchUtility;
import calclavia.lib.utility.inventory.InventoryUtility;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.multipart.TMultiPart;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartLevitator extends PartFace implements ILinkable
{
	private int pushDelay;

	private AxisAlignedBB operationBounds;
	private AxisAlignedBB suckBounds;

	/**
	 * true = suck, false = push
	 */
	public boolean suck = true;

	/**
	 * Pathfinding
	 */
	private ThreadEMPathfinding thread;
	private PathfinderEMContractor pathfinder;
	private Set<EntityItem> pathfindingTrackers = new HashSet<EntityItem>();
	// TODO: WeakReference
	private PartLevitator linked;
	private int lastCalcTime = 0;

	/** Color of beam */
	private int dyeID = TileTesla.DEFAULT_COLOR;

	/**
	 * Linking
	 */
	private byte linkSide;
	private Vector3 tempLinkVector;

	/**
	 * Client Side Only
	 */
	public float renderRotation = 0;

	private int ticks;

	@Override
	public boolean activate(EntityPlayer player, MovingObjectPosition part, ItemStack itemStack)
	{
		if (WrenchUtility.isWrench(itemStack))
		{
			if (onLink(player, ToolModeLink.getLink(itemStack)))
			{
				ToolModeLink.clearLink(itemStack);
			}
			else
			{
				ToolModeLink.setLink(itemStack, new VectorWorld(world(), x(), y(), z()));
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

		suck = !suck;
		updatePath();

		return true;
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

	public void initiate()
	{
		updateBounds();
	}

	@Override
	public void update()
	{
		super.update();

		if (ticks++ == 0)
		{
			initiate();
		}

		pushDelay = Math.max(0, pushDelay - 1);

		if (tempLinkVector != null)
		{
			TMultiPart part = MultipartUtility.getMultipart(world(), tempLinkVector, linkSide);

			if (part instanceof PartLevitator)
			{
				setLink((PartLevitator) part, true);
			}

			tempLinkVector = null;
		}

		if (canFunction())
		{
			TileEntity inventoryTile = getLatched();
			IInventory inventory = (IInventory) inventoryTile;

			if (!suck)
			{
				renderRotation = Math.max(0, renderRotation - 0.8f);
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
			else if (suck)
			{
				renderRotation = Math.min(20, renderRotation + 0.8f);
				if (suckBounds != null)
				{
					if (!world().isRemote)
					{
						for (EntityItem item : (List<EntityItem>) world().getEntitiesWithinAABB(EntityItem.class, suckBounds))
						{
							ItemStack remains = InventoryUtility.putStackInInventory(inventory, item.getEntityItem(), placementSide.getOpposite().getOpposite().ordinal(), false);

							if (remains == null)
							{
								item.setDead();
							}
							else
							{
								item.setEntityItemStack(remains);
							}

							// TODO: Add redstone pulse?
						}
					}
				}
			}

			if (thread != null)
			{
				PathfinderEMContractor newPath = thread.getPath();

				if (newPath != null)
				{
					pathfinder = newPath;
					thread = null;
				}
			}

			final int renderFrequency = 1;
			final boolean renderBeam = ticks % renderFrequency == 0 && hasLink() && linked.suck != suck;

			if (hasLink())
			{
				if (!suck)
				{
					if (renderBeam)
						Electrical.proxy.renderElectricShock(world(), getPosition().translate(0.5), getPosition().translate(new Vector3(placementSide.getOpposite())).translate(0.5), EnumColor.DYES[dyeID].toColor(), false);

					// Push entity along path.
					if (pathfinder != null)
					{
						List<Vector3> results = pathfinder.results;

						for (int i = 0; i < results.size(); i++)
						{
							Vector3 result = results.get(i).clone();

							if (PartLevitator.canBePath(world(), result))
							{
								if (i - 1 >= 0)
								{
									Vector3 prevResult = results.get(i - 1).clone();

									Vector3 difference = prevResult.clone().difference(result);
									final ForgeDirection direction = difference.toForgeDirection();

									if (renderBeam)
									{
										Electrical.proxy.renderElectricShock(world(), prevResult.clone().translate(0.5), result.clone().translate(0.5), EnumColor.DYES[dyeID].toColor(), false);
									}

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
				else
				{
					if (renderBeam)
					{
						Electrical.proxy.renderElectricShock(world(), getPosition().translate(0.5), getPosition().translate(new Vector3(placementSide.getOpposite())).translate(0.5), EnumColor.DYES[dyeID].toColor(), false);
					}

					pathfinder = null;

					Vector3 searchVec = getPosition().translate(placementSide.getOpposite());
					AxisAlignedBB searchBounds = AxisAlignedBB.getAABBPool().getAABB(searchVec.x, searchVec.y, searchVec.z, searchVec.x + 1, searchVec.y + 1, searchVec.z + 1);

					if (searchBounds != null)
					{
						for (EntityItem entityItem : (List<EntityItem>) world().getEntitiesWithinAABB(EntityItem.class, searchBounds))
						{
							moveEntity(entityItem, placementSide.getOpposite(), getPosition());
						}
					}
				}
			}
			else if (!hasLink())
			{
				for (EntityItem entityItem : (List<EntityItem>) world().getEntitiesWithinAABB(EntityItem.class, operationBounds))
				{
					if (ticks % renderFrequency == 0)
						Electrical.proxy.renderElectricShock(world(), getPosition().translate(0.5), new Vector3(entityItem), EnumColor.DYES[dyeID].toColor(), false);
					moveEntity(entityItem, placementSide.getOpposite(), getPosition());
				}
			}

			if (linked != null)
			{
				linked = null;
			}

			lastCalcTime--;
		}
	}

	public static boolean canBePath(World world, Vector3 position)
	{
		Block block = Block.blocksList[position.getBlockID(world)];
		return block == null || (block instanceof BlockSnow || block instanceof BlockVine || block instanceof BlockLadder || ((block instanceof BlockFluid || block instanceof IFluidBlock) && block.blockID != Block.lavaMoving.blockID && block.blockID != Block.lavaStill.blockID));
	}

	private boolean hasLink()
	{
		return linked != null && linked.linked == this;
	}

	private void moveEntity(EntityItem entityItem, ForgeDirection direction, Vector3 lockVector)
	{
		switch (direction)
		{
			case DOWN:
				entityItem.setPosition(lockVector.x + 0.5, entityItem.posY, lockVector.z + 0.5);

				entityItem.motionX = 0;
				entityItem.motionZ = 0;

				if (!suck)
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

				if (!suck)
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

				if (!suck)
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

				if (!suck)
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

				if (!suck)
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

				if (!suck)
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
		EntityItem item = null;

		switch (placementSide.getOpposite())
		{
			case DOWN:
				item = new EntityItem(world(), x() + 0.5, y() - 0.2, z() + 0.5, toSend);
				break;
			case UP:
				item = new EntityItem(world(), x() + 0.5, y() + 1.2, z() + 0.5, toSend);
				break;
			case NORTH:
				item = new EntityItem(world(), x() + 0.5, y() + 0.5, z() - 0.2, toSend);
				break;
			case SOUTH:
				item = new EntityItem(world(), x() + 0.5, y() + 0.5, z() + 1.2, toSend);
				break;
			case WEST:
				item = new EntityItem(world(), x() - 0.2, y() + 0.5, z() + 0.5, toSend);
				break;
			case EAST:
				item = new EntityItem(world(), x() + 1.2, y() + 0.5, z() + 0.5, toSend);
				break;
			default:
				break;
		}

		item.motionX = 0;
		item.motionY = 0;
		item.motionZ = 0;

		return item;
	}

	public void updateBounds()
	{
		ForgeDirection dir = placementSide;
		MovingObjectPosition mop = world().clip(getPosition().translate(dir).toVec3(), getPosition().translate(dir, Settings.LEVITATOR_MAX_REACH).toVec3());

		int reach = Settings.LEVITATOR_MAX_REACH;

		if (mop != null)
		{
			reach = (int) Math.min(getPosition().distance(new Vector3(mop.hitVec)), reach);
		}

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
		suck = packet.readBoolean();
		dyeID = packet.readByte();

		if (packet.readBoolean())
		{
			tempLinkVector = new Vector3(packet.readInt(), packet.readInt(), packet.readInt());
		}
	}

	@Override
	public void writeDesc(MCDataOutput packet)
	{
		super.writeDesc(packet);
		packet.writeBoolean(suck);
		packet.writeByte(dyeID);

		if (linked != null)
		{
			packet.writeBoolean(true);
			packet.writeInt(linked.x());
			packet.writeInt(linked.y());
			packet.writeInt(linked.z());
		}
		else
		{
			packet.writeBoolean(false);
		}

	}

	public boolean canFunction()
	{
		return isLatched() && !world().isBlockIndirectlyGettingPowered(x(), y(), z());
	}

	@Override
	public void load(NBTTagCompound nbt)
	{
		super.load(nbt);

		this.suck = nbt.getBoolean("suck");
		this.dyeID = nbt.getInteger("dyeID");

		if (nbt.hasKey("link"))
		{
			tempLinkVector = new Vector3(nbt.getCompoundTag("link"));
		}
	}

	@Override
	public void save(NBTTagCompound nbt)
	{
		super.save(nbt);

		nbt.setBoolean("suck", suck);
		nbt.setInteger("dyeID", dyeID);

		if (linked != null)
		{
			nbt.setCompoundTag("link", new Vector3(linked.x(), linked.y(), linked.z()).writeToNBT(new NBTTagCompound()));
		}
	}

	/**
	 * Link between two TileEntities, do pathfinding operation.
	 */
	public void setLink(PartLevitator tileEntity, boolean setOpponent)
	{
		if (linked != null && setOpponent)
		{
			linked.setLink(null, false);
		}

		linked = tileEntity;

		if (setOpponent)
		{
			linked.setLink(this, false);
		}

		updatePath();
	}

	public void updatePath()
	{
		if (thread == null && linked != null && lastCalcTime <= 0)
		{
			pathfinder = null;

			Vector3 start = getPosition().translate(placementSide.getOpposite());
			Vector3 target = new Vector3(linked.x(), linked.y(), linked.z()).translate(linked.placementSide.getOpposite());

			if (start.distance(target) < Settings.MAX_CONTRACTOR_DISTANCE)
			{
				if (PartLevitator.canBePath(world(), start) && PartLevitator.canBePath(world(), target))
				{
					thread = new ThreadEMPathfinding(new PathfinderEMContractor(world(), target), start);
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

	@Override
	public boolean onLink(EntityPlayer player, VectorWorld vector)
	{
		tempLinkVector = vector;
		return false;
	}

	public Vector3 getPosition()
	{
		return new Vector3(x(), y(), z());
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
