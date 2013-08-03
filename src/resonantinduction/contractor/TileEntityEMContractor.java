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
	public static double MAX_SPEED = .1;
	public static double ACCELERATION = .01;
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

		if (isLatched() && canFunction())
		{
			TileEntity inventoryTile = getLatched();
			IInventory inventory = (IInventory) inventoryTile;

			if (!suck && pushDelay == 0)
			{
				if (!(inventoryTile instanceof ISidedInventory))
				{
					for (int i = inventory.getSizeInventory() - 1; i >= 0; i--)
					{
						if (inventory.getStackInSlot(i) != null)
						{
							ItemStack toSend = inventory.getStackInSlot(i).copy();
							toSend.stackSize = 1;

							EntityItem item = getItemWithPosition(toSend);

							if (!worldObj.isRemote)
							{
								worldObj.spawnEntityInWorld(item);
							}

							inventory.decrStackSize(i, 1);
							pushDelay = PUSH_DELAY;

							break;
						}
					}
				}
				else
				{
					ISidedInventory sidedInventory = (ISidedInventory) inventoryTile;
					int[] slots = sidedInventory.getAccessibleSlotsFromSide(facing.ordinal());

					if (slots != null)
					{
						for (int get = slots.length - 1; get >= 0; get--)
						{
							int slotID = slots[get];

							if (sidedInventory.getStackInSlot(slotID) != null)
							{
								ItemStack toSend = sidedInventory.getStackInSlot(slotID);
								toSend.stackSize = 1;

								if (sidedInventory.canExtractItem(slotID, toSend, facing.ordinal()))
								{
									EntityItem item = getItemWithPosition(toSend);

									if (!worldObj.isRemote)
									{
										worldObj.spawnEntityInWorld(item);
									}

									sidedInventory.decrStackSize(slotID, 1);
									pushDelay = PUSH_DELAY;

									break;
								}
							}
						}
					}
				}
			}
			else if (suck)
			{
				if (suckBounds != null)
				{
					List<EntityItem> list = worldObj.getEntitiesWithinAABB(EntityItem.class, suckBounds);

					for (EntityItem item : list)
					{
						ItemStack itemStack = item.getEntityItem();

						if (!(inventoryTile instanceof ISidedInventory))
						{
							for (int i = 0; i <= inventory.getSizeInventory() - 1; i++)
							{
								if (inventory.isItemValidForSlot(i, itemStack))
								{
									ItemStack inSlot = inventory.getStackInSlot(i);

									if (inSlot == null)
									{
										inventory.setInventorySlotContents(i, itemStack);
										item.setDead();
										break;
									}
									else if (inSlot.isItemEqual(itemStack) && inSlot.stackSize < inSlot.getMaxStackSize())
									{
										if (inSlot.stackSize + itemStack.stackSize <= inSlot.getMaxStackSize())
										{
											ItemStack toSet = itemStack.copy();
											toSet.stackSize += inSlot.stackSize;

											inventory.setInventorySlotContents(i, toSet);
											item.setDead();
											break;
										}
										else
										{
											int rejects = (inSlot.stackSize + itemStack.stackSize) - inSlot.getMaxStackSize();

											ItemStack toSet = itemStack.copy();
											toSet.stackSize = inSlot.getMaxStackSize();

											ItemStack remains = itemStack.copy();
											remains.stackSize = rejects;

											inventory.setInventorySlotContents(i, toSet);
											item.setEntityItemStack(remains);
										}
									}
								}
							}
						}
						else
						{
							ISidedInventory sidedInventory = (ISidedInventory) inventoryTile;
							int[] slots = sidedInventory.getAccessibleSlotsFromSide(facing.ordinal());

							for (int get = 0; get <= slots.length - 1; get++)
							{
								int slotID = slots[get];

								if (sidedInventory.isItemValidForSlot(slotID, itemStack) && sidedInventory.canInsertItem(slotID, itemStack, facing.ordinal()))
								{
									ItemStack inSlot = inventory.getStackInSlot(slotID);

									if (inSlot == null)
									{
										inventory.setInventorySlotContents(slotID, itemStack);
										item.setDead();
										break;
									}
									else if (inSlot.isItemEqual(itemStack) && inSlot.stackSize < inSlot.getMaxStackSize())
									{
										if (inSlot.stackSize + itemStack.stackSize <= inSlot.getMaxStackSize())
										{
											ItemStack toSet = itemStack.copy();
											toSet.stackSize += inSlot.stackSize;

											inventory.setInventorySlotContents(slotID, toSet);
											item.setDead();
											break;
										}
										else
										{
											int rejects = (inSlot.stackSize + itemStack.stackSize) - inSlot.getMaxStackSize();

											ItemStack toSet = itemStack.copy();
											toSet.stackSize = inSlot.getMaxStackSize();

											ItemStack remains = itemStack.copy();
											remains.stackSize = rejects;

											inventory.setInventorySlotContents(slotID, toSet);
											item.setEntityItemStack(remains);
										}
									}
								}
							}
						}
					}
				}
			}
		}

		if (operationBounds != null && canFunction())
		{
			List<Entity> list = worldObj.getEntitiesWithinAABB(Entity.class, operationBounds);
			
			energyStored -= ENERGY_USAGE;

			for (Entity entity : list)
			{
				if (entity instanceof EntityItem)
				{
					EntityItem entityItem = (EntityItem) entity;

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
								entityItem.motionY = Math.min((MAX_SPEED * 4), entityItem.motionY + (ACCELERATION * 5));
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
								entityItem.motionY = Math.min((MAX_SPEED * 4), entityItem.motionY + (ACCELERATION * 5));
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
	}

	private EntityItem getItemWithPosition(ItemStack toSend)
	{
		EntityItem item = null;

		switch (facing)
		{
			case DOWN:
				item = new EntityItem(worldObj, xCoord + 0.5, yCoord, zCoord + 0.5, toSend);
				break;
			case UP:
				item = new EntityItem(worldObj, xCoord + 0.5, yCoord + 1, zCoord + 0.5, toSend);
				break;
			case NORTH:
				item = new EntityItem(worldObj, xCoord + 0.5, yCoord + 0.5, zCoord, toSend);
				break;
			case SOUTH:
				item = new EntityItem(worldObj, xCoord + 0.5, yCoord + 0.5, zCoord + 1, toSend);
				break;
			case WEST:
				item = new EntityItem(worldObj, xCoord, yCoord + 0.5, zCoord + 0.5, toSend);
				break;
			case EAST:
				item = new EntityItem(worldObj, xCoord + 1, yCoord + 0.5, zCoord + 0.5, toSend);
				break;
		}

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
		return worldObj.getBlockPowerInput(xCoord, yCoord, zCoord) > 0;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);

		facing = ForgeDirection.getOrientation(nbtTags.getInteger("facing"));
		suck = nbtTags.getBoolean("suck");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		nbtTags.setInteger("facing", facing.ordinal());
		nbtTags.setBoolean("suck", suck);
	}

	@Override
	public void handle(ByteArrayDataInput input)
	{
		try
		{
			facing = ForgeDirection.getOrientation(input.readInt());
			suck = input.readBoolean();
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
		return data;
	}

	@Override
	public float transfer(float transferEnergy, boolean doTransfer) 
	{
		float energyToUse = Math.min(transferEnergy, ENERGY_USAGE-energyStored);
		
		if(doTransfer)
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
