package resonantinduction.contractor;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.PacketHandler;
import resonantinduction.api.ITesla;
import resonantinduction.base.IPacketReceiver;
import resonantinduction.base.InventoryUtil;

import com.google.common.io.ByteArrayDataInput;

/**
 * 
 * @author AidanBrady
 *
 */
public class TileEntityEMContractor extends TileEntity implements IPacketReceiver, ITesla
{
	public static int MAX_REACH = 40;
	public static int PUSH_DELAY = 5;
	public static double MAX_SPEED = .2;
	public static double ACCELERATION = .02;
	public static float ENERGY_USAGE = .005F;

	private ForgeDirection facing = ForgeDirection.UP;

	public int pushDelay;
	
	public float energyStored;

	public AxisAlignedBB operationBounds;
	public AxisAlignedBB suckBounds;

	/**
	 * true = suck, false = push
	 */
	public boolean suck = true;

	@Override
	public void updateEntity()
	{
		pushDelay = Math.max(0, pushDelay - 1);

		if (canFunction())
		{
			TileEntity inventoryTile = getLatched();
			IInventory inventory = (IInventory) inventoryTile;

			if (!suck && pushDelay == 0)
			{
				ItemStack retrieved = InventoryUtil.takeTopItemFromInventory(inventory, facing.ordinal());
				
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
		}

		if (operationBounds != null && canFunction())
		{
			energyStored -= ENERGY_USAGE;

			for (EntityItem entityItem : (List<EntityItem>) worldObj.getEntitiesWithinAABB(EntityItem.class, operationBounds))
			{
				switch (facing)
				{
					case DOWN:
						if (!worldObj.isRemote)
						{
							entityItem.setPosition(xCoord + 0.5, entityItem.posY, zCoord + 0.5);
						}

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

						entityItem.isAirBorne = true;
						break;
					case UP:
						if (!worldObj.isRemote)
						{
							entityItem.setPosition(xCoord + 0.5, entityItem.posY, zCoord + 0.5);
						}

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

						entityItem.isAirBorne = true;
						break;
					case NORTH:
						if (!worldObj.isRemote)
						{
							entityItem.setPosition(xCoord + 0.5, yCoord + 0.5, entityItem.posZ);
						}

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

						entityItem.isAirBorne = true;
						break;
					case SOUTH:
						if (!worldObj.isRemote)
						{
							entityItem.setPosition(xCoord + 0.5, yCoord + 0.5, entityItem.posZ);
						}

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

						entityItem.isAirBorne = true;
						break;
					case WEST:
						if (!worldObj.isRemote)
						{
							entityItem.setPosition(entityItem.posX, yCoord + 0.5, zCoord + 0.5);
						}

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

						entityItem.isAirBorne = true;
						break;
					case EAST:
						if (!worldObj.isRemote)
						{
							entityItem.setPosition(entityItem.posX, yCoord + 0.5, zCoord + 0.5);
						}

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

						entityItem.isAirBorne = true;
						break;
				}
			}
		}
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

	public ForgeDirection getFacing()
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
		return isLatched() && worldObj.getBlockPowerInput(xCoord, yCoord, zCoord) > 0;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);

		facing = ForgeDirection.getOrientation(nbtTags.getInteger("facing"));
		suck = nbtTags.getBoolean("suck");
		energyStored = nbtTags.getFloat("energyStored");
		
		updateBounds();
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		nbtTags.setInteger("facing", facing.ordinal());
		nbtTags.setBoolean("suck", suck);
		nbtTags.setFloat("energyStored", energyStored);
	}

	@Override
	public void handle(ByteArrayDataInput input)
	{
		try
		{
			facing = ForgeDirection.getOrientation(input.readInt());
			suck = input.readBoolean();
			energyStored = input.readFloat();
			
			worldObj.markBlockForRenderUpdate(xCoord, yCoord, zCoord);
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
		
		return data;
	}

	@Override
	public float transfer(float transferEnergy, boolean doTransfer) 
	{
		float energyToUse = Math.min(transferEnergy, ENERGY_USAGE-energyStored);
		
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
}
