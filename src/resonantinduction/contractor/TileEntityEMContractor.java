package resonantinduction.contractor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.PacketHandler;
import resonantinduction.ResonantInduction;
import resonantinduction.api.ITesla;
import resonantinduction.base.IPacketReceiver;
import resonantinduction.base.InventoryUtil;
import resonantinduction.base.TileEntityBase;
import resonantinduction.base.Vector3;
import resonantinduction.tesla.TileEntityTesla;

import com.google.common.io.ByteArrayDataInput;

/**
 * 
 * @author AidanBrady
 * 
 */
public class TileEntityEMContractor extends TileEntityBase implements IPacketReceiver, ITesla
{
	public static int MAX_REACH = 40;
	public static int PUSH_DELAY = 5;
	public static double MAX_SPEED = .2;
	public static double ACCELERATION = .02;
	public static float ENERGY_USAGE = .005F;

	private ForgeDirection facing = ForgeDirection.UP;

	private int pushDelay;

	private float energyStored;

	private AxisAlignedBB operationBounds;
	private AxisAlignedBB suckBounds;

	/**
	 * true = suck, false = push
	 */
	public boolean suck = true;

	private PathfinderEMContractor pathfinder;
	private Set<EntityItem> pathfindingTrackers = new HashSet<EntityItem>();
	private TileEntityEMContractor linked;

	/** Color of beam */
	private int dyeID = 13;
	private Vector3 tempLinkVector;

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		this.pushDelay = Math.max(0, this.pushDelay - 1);

		if (this.tempLinkVector != null)
		{
			if (this.tempLinkVector.getTileEntity(this.worldObj) instanceof TileEntityEMContractor)
			{
				this.setLink((TileEntityEMContractor) this.tempLinkVector.getTileEntity(this.worldObj), true);
				System.out.println("TEST" + this.linked);
			}

			this.tempLinkVector = null;
		}

		if (canFunction())
		{
			TileEntity inventoryTile = getLatched();
			IInventory inventory = (IInventory) inventoryTile;

			if (!suck && pushDelay == 0)
			{
				ItemStack retrieved = InventoryUtil.takeTopItemFromInventory(inventory, this.facing.ordinal());

				if (retrieved != null)
				{
					EntityItem item = getItemWithPosition(retrieved);

					if (!worldObj.isRemote)
					{
						this.worldObj.spawnEntityInWorld(item);
					}

					pushDelay = PUSH_DELAY;
				}
			}
			else if (suck)
			{
				if (suckBounds != null)
				{
					for (EntityItem item : (List<EntityItem>) worldObj.getEntitiesWithinAABB(EntityItem.class, suckBounds))
					{
						ItemStack remains = InventoryUtil.putStackInInventory(inventory, item.getEntityItem(), facing.ordinal());

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

			if (!this.suck && this.linked != null && !this.linked.isInvalid())
			{
				if (this.pathfinder != null)
				{
					for (int i = 0; i < this.pathfinder.results.size(); i++)
					{
						Vector3 result = this.pathfinder.results.get(i);

						if (this.canBePath(this.worldObj, result))
						{
							if (i - 1 >= 0)
							{
								Vector3 prevResult = this.pathfinder.results.get(i - 1);
								ResonantInduction.proxy.renderElectricShock(this.worldObj, prevResult.translate(0.5), result.translate(0.5), TileEntityTesla.dyeColors[dyeID]);

								Vector3 difference = prevResult.difference(result);
								final ForgeDirection direction = difference.toForgeDirection();

								AxisAlignedBB bounds = AxisAlignedBB.getAABBPool().getAABB(result.x, result.y, result.z, result.x + 1, result.y + 1, result.z + 1);
								List<EntityItem> entities = this.worldObj.getEntitiesWithinAABB(EntityItem.class, bounds);

								for (EntityItem entityItem : entities)
								{
									this.moveEntity(entityItem, direction, result);
								}
							}
						}
						else
						{
							this.updatePath();
							break;
						}
					}
				}
			}
			else
			{
				this.pathfinder = null;

				if (operationBounds != null)
				{
					energyStored -= ENERGY_USAGE;

					for (EntityItem entityItem : (List<EntityItem>) worldObj.getEntitiesWithinAABB(EntityItem.class, operationBounds))
					{
						if (this.worldObj.isRemote && this.ticks % 5 == 0)
						{
							ResonantInduction.proxy.renderElectricShock(this.worldObj, new Vector3(this).translate(0.5), new Vector3(entityItem), TileEntityTesla.dyeColors[dyeID]);
						}

						this.moveEntity(entityItem, this.getDirection(), new Vector3(this));
					}
				}
			}
		}
	}

	public static boolean canBePath(World world, Vector3 position)
	{
		return position.getBlockID(world) == 0 || position.getTileEntity(world) instanceof TileEntityEMContractor;
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
		}

		entityItem.isAirBorne = true;
		entityItem.delayBeforeCanPickup = 1;
	}

	private EntityItem getItemWithPosition(ItemStack toSend)
	{
		EntityItem item = null;

		switch (facing)
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
		}

		item.setVelocity(0, 0, 0);

		return item;
	}

	@Override
	public void validate()
	{
		super.validate();

		if (worldObj.isRemote)
		{
			PacketHandler.sendDataRequest(this);
		}
	}

	public void updateBounds()
	{
		switch (facing)
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
		}
	}

	public boolean isLatched()
	{
		return getLatched() != null;
	}

	public TileEntity getLatched()
	{
		ForgeDirection side = facing.getOpposite();

		TileEntity tile = worldObj.getBlockTileEntity(xCoord + side.offsetX, yCoord + side.offsetY, zCoord + side.offsetZ);

		if (tile instanceof IInventory)
		{
			return tile;
		}

		return null;
	}

	public void incrementFacing()
	{
		int newOrdinal = facing.ordinal() < 5 ? facing.ordinal() + 1 : 0;
		setFacing(ForgeDirection.getOrientation(newOrdinal));
	}

	public ForgeDirection getDirection()
	{
		return facing;
	}

	public void setFacing(ForgeDirection side)
	{
		facing = side;

		if (!worldObj.isRemote)
		{
			PacketHandler.sendTileEntityPacketToClients(this, getNetworkedData(new ArrayList()).toArray());
		}

		updateBounds();
	}

	public boolean canFunction()
	{
		return isLatched() && !this.worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);

		this.facing = ForgeDirection.getOrientation(nbt.getInteger("facing"));
		this.suck = nbt.getBoolean("suck");
		this.energyStored = nbt.getFloat("energyStored");
		this.dyeID = nbt.getInteger("dyeID");
		this.tempLinkVector = new Vector3(nbt.getInteger("link_x"), nbt.getInteger("link_y"), nbt.getInteger("link_z"));
		updateBounds();
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);

		nbt.setInteger("facing", facing.ordinal());
		nbt.setBoolean("suck", suck);
		nbt.setFloat("energyStored", energyStored);
		nbt.setInteger("dyeID", this.dyeID);

		if (this.linked != null)
		{
			nbt.setInteger("link_x", this.linked.xCoord);
			nbt.setInteger("link_y", this.linked.yCoord);
			nbt.setInteger("link_z", this.linked.zCoord);
		}
	}

	@Override
	public void handle(ByteArrayDataInput input)
	{
		try
		{
			facing = ForgeDirection.getOrientation(input.readInt());
			suck = input.readBoolean();
			energyStored = input.readFloat();
			this.dyeID = input.readInt();

			if (input.readBoolean())
			{
				this.tempLinkVector = new Vector3(input.readInt(), input.readInt(), input.readInt());
			}

			this.worldObj.markBlockForRenderUpdate(xCoord, yCoord, zCoord);
			updateBounds();
		}
		catch (Exception e)
		{
		}
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		data.add(facing.ordinal());
		data.add(suck);
		data.add(energyStored);
		data.add(this.dyeID);

		if (this.linked != null)
		{
			data.add(true);
			data.add(this.linked.xCoord);
			data.add(this.linked.yCoord);
			data.add(this.linked.zCoord);
		}
		else
		{
			data.add(false);
		}

		return data;
	}

	@Override
	public float transfer(float transferEnergy, boolean doTransfer)
	{
		float energyToUse = Math.min(transferEnergy, ENERGY_USAGE - energyStored);

		if (doTransfer)
		{
			energyStored += energyToUse;
		}

		return energyToUse;
	}

	@Override
	public boolean canReceive(TileEntity transferTile)
	{
		return true;
	}

	/**
	 * Link between two TileEntities, do pathfinding operation.
	 */
	public void setLink(TileEntityEMContractor tileEntity, boolean setOpponent)
	{
		if (this.linked != null && setOpponent)
		{
			this.linked.setLink(null, false);
		}

		this.linked = tileEntity;

		if (setOpponent)
		{
			this.linked.setLink(this, false);
		}

		this.updatePath();
	}

	public void updatePath()
	{
		this.pathfinder = null;

		if (this.linked != null)
		{
			this.pathfinder = new PathfinderEMContractor(this.worldObj, new Vector3(this.linked).translate(new Vector3(this.linked.getDirection())));
			this.pathfinder.find(new Vector3(this).translate(new Vector3(this.getDirection())));
		}
	}

	/**
	 * @param itemDamage
	 */
	public void setDye(int dyeID)
	{
		this.dyeID = dyeID;
		this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
	}
}
