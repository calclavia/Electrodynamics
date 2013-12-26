package resonantinduction.levitator;

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
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.IFluidBlock;
import resonantinduction.ResonantInduction;
import resonantinduction.base.ListUtil;
import resonantinduction.tesla.TileTesla;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.network.IPacketReceiver;
import calclavia.lib.network.IPacketSender;
import calclavia.lib.prefab.tile.TileAdvanced;
import calclavia.lib.utility.InventoryUtility;

import com.google.common.io.ByteArrayDataInput;

/**
 * 
 * @author Calclavia
 * 
 */
public class TileEMLevitator extends TileAdvanced implements IPacketReceiver, IPacketSender
{
	public static int MAX_REACH = 40;
	public static int PUSH_DELAY = 5;
	public static double MAX_SPEED = .2;
	public static double ACCELERATION = .02;

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
	private TileEMLevitator linked;
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
			if (tempLinkVector.getTileEntity(worldObj) instanceof TileEMLevitator)
			{
				setLink((TileEMLevitator) tempLinkVector.getTileEntity(worldObj), true);
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

					pushDelay = PUSH_DELAY;
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

			final int renderFrequency = ResonantInduction.proxy.isFancy() ? 1 + worldObj.rand.nextInt(2) : 10 + worldObj.rand.nextInt(2);
			final boolean renderBeam = ticks % renderFrequency == 0 && hasLink() && linked.suck != suck;

			if (hasLink())
			{
				if (!suck)
				{
					if (renderBeam)
					{
						ResonantInduction.proxy.renderElectricShock(worldObj, new Vector3(this).translate(0.5), new Vector3(this).translate(new Vector3(getDirection())).translate(0.5), ResonantInduction.DYE_COLORS[dyeID], false);
					}

					// Push entity along path.
					if (pathfinder != null)
					{
						List<Vector3> results = this.pathfinder.results;
						for (int i = 0; i < results.size(); i++)
						{
							Vector3 result = results.get(i).clone();

							if (TileEMLevitator.canBePath(worldObj, result))
							{
								if (i - 1 >= 0)
								{
									Vector3 prevResult = results.get(i - 1).clone();

									Vector3 difference = prevResult.clone().difference(result);
									final ForgeDirection direction = difference.toForgeDirection();

									if (renderBeam)
									{
										ResonantInduction.proxy.renderElectricShock(worldObj, prevResult.clone().translate(0.5), result.clone().translate(0.5), ResonantInduction.DYE_COLORS[dyeID], false);
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
						ResonantInduction.proxy.renderElectricShock(worldObj, new Vector3(this).translate(0.5), new Vector3(this).translate(new Vector3(getDirection())).translate(0.5), ResonantInduction.DYE_COLORS[dyeID], false);
					}

					pathfinder = null;

					Vector3 searchVec = new Vector3(this).modifyPositionFromSide(getDirection());
					AxisAlignedBB searchBounds = AxisAlignedBB.getAABBPool().getAABB(searchVec.x, searchVec.y, searchVec.z, searchVec.x + 1, searchVec.y + 1, searchVec.z + 1);

					if (searchBounds != null)
					{
						for (EntityItem entityItem : (List<EntityItem>) worldObj.getEntitiesWithinAABB(EntityItem.class, searchBounds))
						{
							if (renderBeam)
							{
								ResonantInduction.proxy.renderElectricShock(worldObj, new Vector3(this).translate(0.5), new Vector3(entityItem), ResonantInduction.DYE_COLORS[dyeID], false);
							}

							moveEntity(entityItem, getDirection(), new Vector3(this));
						}
					}
				}
			}
			else if (!hasLink())
			{
				for (EntityItem entityItem : (List<EntityItem>) worldObj.getEntitiesWithinAABB(EntityItem.class, operationBounds))
				{
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
					entityItem.motionY = Math.max(-MAX_SPEED, entityItem.motionY - ACCELERATION);
				}
				else
				{
					entityItem.motionY = Math.min(MAX_SPEED, entityItem.motionY + .04 + ACCELERATION);
				}

				break;
			case UP:

				entityItem.setPosition(lockVector.x + 0.5, entityItem.posY, lockVector.z + 0.5);

				entityItem.motionX = 0;
				entityItem.motionZ = 0;

				if (!suck)
				{
					entityItem.motionY = Math.min(MAX_SPEED, entityItem.motionY + .04 + ACCELERATION);
				}
				else
				{
					entityItem.motionY = Math.max(-MAX_SPEED, entityItem.motionY - ACCELERATION);
				}

				break;
			case NORTH:

				entityItem.setPosition(lockVector.x + 0.5, lockVector.y + 0.5, entityItem.posZ);

				entityItem.motionX = 0;
				entityItem.motionY = 0;

				if (!suck)
				{
					entityItem.motionZ = Math.max(-MAX_SPEED, entityItem.motionZ - ACCELERATION);
				}
				else
				{
					entityItem.motionZ = Math.min(MAX_SPEED, entityItem.motionZ + ACCELERATION);
				}

				break;
			case SOUTH:

				entityItem.setPosition(lockVector.x + 0.5, lockVector.y + 0.5, entityItem.posZ);

				entityItem.motionX = 0;
				entityItem.motionY = 0;

				if (!suck)
				{
					entityItem.motionZ = Math.min(MAX_SPEED, entityItem.motionZ + ACCELERATION);
				}
				else
				{
					entityItem.motionZ = Math.max(-MAX_SPEED, entityItem.motionZ - ACCELERATION);
				}

				break;
			case WEST:

				entityItem.setPosition(entityItem.posX, lockVector.y + 0.5, lockVector.z + 0.5);

				entityItem.motionY = 0;
				entityItem.motionZ = 0;

				if (!suck)
				{
					entityItem.motionX = Math.max(-MAX_SPEED, entityItem.motionX - ACCELERATION);
				}
				else
				{
					entityItem.motionX = Math.min(MAX_SPEED, entityItem.motionX + ACCELERATION);
				}

				break;
			case EAST:
				entityItem.setPosition(entityItem.posX, lockVector.y + 0.5, lockVector.z + 0.5);

				entityItem.motionY = 0;
				entityItem.motionZ = 0;

				if (!suck)
				{
					entityItem.motionX = Math.min(MAX_SPEED, entityItem.motionX + ACCELERATION);
				}
				else
				{
					entityItem.motionX = Math.max(-MAX_SPEED, entityItem.motionX - ACCELERATION);
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
		switch (getDirection())
		{
			case DOWN:
				operationBounds = AxisAlignedBB.getBoundingBox(xCoord, Math.max(yCoord - MAX_REACH, 1), zCoord, xCoord + 1, yCoord, zCoord + 1);
				suckBounds = AxisAlignedBB.getBoundingBox(xCoord, yCoord - 0.1, zCoord, xCoord + 1, yCoord, zCoord + 1);
				break;
			case UP:
				operationBounds = AxisAlignedBB.getBoundingBox(xCoord, yCoord + 1, zCoord, xCoord + 1, Math.min(yCoord + 1 + MAX_REACH, 255), zCoord + 1);
				suckBounds = AxisAlignedBB.getBoundingBox(xCoord, yCoord + 1, zCoord, xCoord + 1, yCoord + 1.1, zCoord + 1);
				break;
			case NORTH:
				operationBounds = AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord - MAX_REACH, xCoord + 1, yCoord + 1, zCoord);
				suckBounds = AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord - 0.1, xCoord + 1, yCoord + 1, zCoord);
				break;
			case SOUTH:
				operationBounds = AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord + 1, xCoord + 1, yCoord + 1, zCoord + 1 + MAX_REACH);
				suckBounds = AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord + 1, xCoord + 1, yCoord + 1, zCoord + 1.1);
				break;
			case WEST:
				operationBounds = AxisAlignedBB.getBoundingBox(xCoord - MAX_REACH, yCoord, zCoord, xCoord, yCoord + 1, zCoord + 1);
				suckBounds = AxisAlignedBB.getBoundingBox(xCoord - 0.1, yCoord, zCoord, xCoord, yCoord + 1, zCoord + 1);
				break;
			case EAST:
				operationBounds = AxisAlignedBB.getBoundingBox(xCoord + 1, yCoord, zCoord, xCoord + 1 + MAX_REACH, yCoord + 1, zCoord + 1);
				suckBounds = AxisAlignedBB.getBoundingBox(xCoord + 1, yCoord, zCoord, xCoord + 1.1, yCoord + 1, zCoord + 1);
				break;
			default:
				break;
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

	public void incrementFacing()
	{
		int newOrdinal = getDirection().ordinal() < 5 ? getDirection().ordinal() + 1 : 0;
		setDirection(ForgeDirection.getOrientation(newOrdinal));
	}

	public ForgeDirection getDirection()
	{
		return ForgeDirection.getOrientation(this.getBlockMetadata());
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
	public void onReceivePacket(ByteArrayDataInput data, EntityPlayer player)
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
	public void setLink(TileEMLevitator tileEntity, boolean setOpponent)
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

			Vector3 start = new Vector3(this).modifyPositionFromSide(getDirection());
			Vector3 target = new Vector3(linked).modifyPositionFromSide(linked.getDirection());

			if (start.distance(target) < ResonantInduction.MAX_CONTRACTOR_DISTANCE)
			{
				if (TileEMLevitator.canBePath(worldObj, start) && TileEMLevitator.canBePath(worldObj, target))
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

}
