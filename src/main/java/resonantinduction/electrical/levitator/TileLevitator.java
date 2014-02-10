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
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.IFluidBlock;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.Settings;
import resonantinduction.electrical.Electrical;
import resonantinduction.electrical.tesla.TileTesla;
import universalelectricity.api.vector.Vector3;
import universalelectricity.api.vector.VectorWorld;
import calclavia.lib.network.IPacketReceiver;
import calclavia.lib.network.IPacketSender;
import calclavia.lib.prefab.block.ILinkable;
import calclavia.lib.prefab.tile.TileAdvanced;
import calclavia.lib.render.EnumColor;
import calclavia.lib.utility.inventory.InventoryUtility;

import com.google.common.io.ByteArrayDataInput;

/**
 * 
 * @author Calclavia
 * 
 */
public class TileLevitator extends TileAdvanced implements IPacketReceiver, IPacketSender, ILinkable
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
	private TileLevitator linked;
	private int lastCalcTime = 0;

	/** Color of beam */
	private int dyeID = TileTesla.DEFAULT_COLOR;
	private Vector3 tempLinkVector;

	@Override
	public void initiate()
	{
		super.initiate();
		updateBounds();
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		pushDelay = Math.max(0, pushDelay - 1);

		if (tempLinkVector != null)
		{
			if (tempLinkVector.getTileEntity(worldObj) instanceof TileLevitator)
			{
				setLink((TileLevitator) tempLinkVector.getTileEntity(worldObj), true);
			}

			tempLinkVector = null;
		}

		if (canFunction())
		{
			TileEntity inventoryTile = getLatched();
			IInventory inventory = (IInventory) inventoryTile;

			if (!suck && pushDelay == 0)
			{
				ItemStack retrieved = InventoryUtility.takeTopItemFromInventory(inventory, getDirection().getOpposite().ordinal());

				if (retrieved != null)
				{
					EntityItem item = getItemWithPosition(retrieved);

					if (!worldObj.isRemote)
					{
						worldObj.spawnEntityInWorld(item);
					}

					pushDelay = Settings.LEVITATOR_PUSH_DELAY;
				}
			}
			else if (suck)
			{
				if (suckBounds != null)
				{
					if (!worldObj.isRemote)
					{
						for (EntityItem item : (List<EntityItem>) worldObj.getEntitiesWithinAABB(EntityItem.class, suckBounds))
						{
							ItemStack remains = InventoryUtility.putStackInInventory(inventory, item.getEntityItem(), getDirection().getOpposite().ordinal(), false);

							if (remains == null)
							{
								item.setDead();
							}
							else
							{
								item.setEntityItemStack(remains);
							}
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
						Electrical.proxy.renderElectricShock(worldObj, new Vector3(this).translate(0.5), new Vector3(this).translate(new Vector3(getDirection())).translate(0.5), EnumColor.DYES[dyeID].toColor(), false);

					// Push entity along path.
					if (pathfinder != null)
					{
						List<Vector3> results = pathfinder.results;

						for (int i = 0; i < results.size(); i++)
						{
							Vector3 result = results.get(i).clone();

							if (TileLevitator.canBePath(worldObj, result))
							{
								if (i - 1 >= 0)
								{
									Vector3 prevResult = results.get(i - 1).clone();

									Vector3 difference = prevResult.clone().difference(result);
									final ForgeDirection direction = difference.toForgeDirection();

									if (renderBeam)
									{
										Electrical.proxy.renderElectricShock(worldObj, prevResult.clone().translate(0.5), result.clone().translate(0.5), EnumColor.DYES[dyeID].toColor(), false);
									}

									AxisAlignedBB bounds = AxisAlignedBB.getAABBPool().getAABB(result.x, result.y, result.z, result.x + 1, result.y + 1, result.z + 1);
									List<EntityItem> entities = worldObj.getEntitiesWithinAABB(EntityItem.class, bounds);

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
						Electrical.proxy.renderElectricShock(worldObj, new Vector3(this).translate(0.5), new Vector3(this).translate(new Vector3(getDirection())).translate(0.5), EnumColor.DYES[dyeID].toColor(), false);
					}

					pathfinder = null;

					Vector3 searchVec = new Vector3(this).translate(getDirection());
					AxisAlignedBB searchBounds = AxisAlignedBB.getAABBPool().getAABB(searchVec.x, searchVec.y, searchVec.z, searchVec.x + 1, searchVec.y + 1, searchVec.z + 1);

					if (searchBounds != null)
					{
						for (EntityItem entityItem : (List<EntityItem>) worldObj.getEntitiesWithinAABB(EntityItem.class, searchBounds))
						{
							moveEntity(entityItem, getDirection(), new Vector3(this));
						}
					}
				}
			}
			else if (!hasLink())
			{
				for (EntityItem entityItem : (List<EntityItem>) worldObj.getEntitiesWithinAABB(EntityItem.class, operationBounds))
				{
					if (ticks % renderFrequency == 0)
						Electrical.proxy.renderElectricShock(worldObj, new Vector3(this).translate(0.5), new Vector3(entityItem), EnumColor.DYES[dyeID].toColor(), false);
					moveEntity(entityItem, getDirection(), new Vector3(this));
				}
			}

			if (linked != null && linked.isInvalid())
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
		return linked != null && !linked.isInvalid() && linked.linked == this;
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

		switch (getDirection())
		{
			case DOWN:
				item = new EntityItem(worldObj, xCoord + 0.5, yCoord - 0.2, zCoord + 0.5, toSend);
				break;
			case UP:
				item = new EntityItem(worldObj, xCoord + 0.5, yCoord + 1.2, zCoord + 0.5, toSend);
				break;
			case NORTH:
				item = new EntityItem(worldObj, xCoord + 0.5, yCoord + 0.5, zCoord - 0.2, toSend);
				break;
			case SOUTH:
				item = new EntityItem(worldObj, xCoord + 0.5, yCoord + 0.5, zCoord + 1.2, toSend);
				break;
			case WEST:
				item = new EntityItem(worldObj, xCoord - 0.2, yCoord + 0.5, zCoord + 0.5, toSend);
				break;
			case EAST:
				item = new EntityItem(worldObj, xCoord + 1.2, yCoord + 0.5, zCoord + 0.5, toSend);
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
		ForgeDirection dir = getDirection();
		MovingObjectPosition mop = worldObj.clip(new Vector3(this).translate(dir).toVec3(), new Vector3(this).translate(dir, Settings.LEVITATOR_MAX_REACH).toVec3());

		int reach = Settings.LEVITATOR_MAX_REACH;

		if (mop != null)
		{
			reach = (int) Math.min(new Vector3(this).distance(new Vector3(mop.hitVec)), reach);
		}

		if (dir.offsetX + dir.offsetY + dir.offsetZ < 0)
		{
			operationBounds = AxisAlignedBB.getBoundingBox(xCoord + dir.offsetX * reach, yCoord + dir.offsetY * reach, zCoord + dir.offsetZ * reach, xCoord + 1, yCoord + 1, zCoord + 1);
			suckBounds = AxisAlignedBB.getBoundingBox(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ, xCoord + 1, yCoord + 1, zCoord + 1);
		}
		else
		{
			operationBounds = AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1 + dir.offsetX * reach, yCoord + 1 + dir.offsetY * reach, zCoord + 1 + dir.offsetZ * reach);
			suckBounds = AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1 + dir.offsetX, yCoord + 1 + dir.offsetY, zCoord + 1 + dir.offsetZ);
		}

	}

	public boolean isLatched()
	{
		return getLatched() != null;
	}

	public TileEntity getLatched()
	{
		ForgeDirection side = getDirection().getOpposite();

		TileEntity tile = worldObj.getBlockTileEntity(xCoord + side.offsetX, yCoord + side.offsetY, zCoord + side.offsetZ);

		if (tile instanceof IInventory)
		{
			return tile;
		}

		return null;
	}

	public ForgeDirection getDirection()
	{
		return ForgeDirection.getOrientation(getBlockType() != null ? getBlockMetadata() : 0);
	}

	public void setDirection(ForgeDirection side)
	{
		this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, side.ordinal(), 3);
		this.updateBounds();
	}

	@Override
	public ArrayList getPacketData(int type)
	{
		ArrayList data = new ArrayList();
		data.add(suck);
		data.add(dyeID);

		if (linked != null)
		{
			data.add(true);

			data.add(linked.xCoord);
			data.add(linked.yCoord);
			data.add(linked.zCoord);
		}
		else
		{
			data.add(false);
		}

		return data;
	}

	@Override
	public Packet getDescriptionPacket()
	{
		return ResonantInduction.PACKET_TILE.getPacket(this, getPacketData(0).toArray());
	}

	@Override
	public void onReceivePacket(ByteArrayDataInput data, EntityPlayer player, Object... extra)
	{
		suck = data.readBoolean();
		dyeID = data.readInt();

		if (data.readBoolean())
		{
			tempLinkVector = new Vector3(data.readInt(), data.readInt(), data.readInt());
		}

		worldObj.markBlockForRenderUpdate(xCoord, yCoord, zCoord);
		updateBounds();
	}

	public boolean canFunction()
	{
		return isLatched() && !worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);

		this.suck = nbt.getBoolean("suck");
		this.dyeID = nbt.getInteger("dyeID");

		if (nbt.hasKey("link"))
		{
			tempLinkVector = new Vector3(nbt.getCompoundTag("link"));
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);

		nbt.setBoolean("suck", suck);
		nbt.setInteger("dyeID", dyeID);

		if (linked != null)
		{
			nbt.setCompoundTag("link", new Vector3(linked).writeToNBT(new NBTTagCompound()));
		}
	}

	/**
	 * Link between two TileEntities, do pathfinding operation.
	 */
	public void setLink(TileLevitator tileEntity, boolean setOpponent)
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

			Vector3 start = new Vector3(this).translate(getDirection());
			Vector3 target = new Vector3(linked).translate(linked.getDirection());

			if (start.distance(target) < Settings.MAX_CONTRACTOR_DISTANCE)
			{
				if (TileLevitator.canBePath(worldObj, start) && TileLevitator.canBePath(worldObj, target))
				{
					thread = new ThreadEMPathfinding(new PathfinderEMContractor(worldObj, target), start);
					thread.start();
					lastCalcTime = 40;
				}
			}
		}
	}

	public void setDye(int dye)
	{
		dyeID = dye;
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	@Override
	public boolean onLink(EntityPlayer player, VectorWorld vector)
	{
		if (vector != null)
		{
			if (vector.getTileEntity(this.worldObj) instanceof TileLevitator)
			{
				this.setLink((TileLevitator) vector.getTileEntity(this.worldObj), true);

				if (this.worldObj.isRemote)
				{
					player.addChatMessage("Linked " + this.getBlockType().getLocalizedName() + " with " + " [" + (int) vector.x + ", " + (int) vector.y + ", " + (int) vector.z + "]");
				}

				return true;
			}
		}

		return false;
	}

}
