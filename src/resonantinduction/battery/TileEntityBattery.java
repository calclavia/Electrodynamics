/**
 * 
 */
package resonantinduction.battery;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.ResonantInduction;
import resonantinduction.api.ICapacitor;
import resonantinduction.base.ListUtil;
import universalelectricity.api.item.IElectricalItem;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.network.IPacketReceiver;
import calclavia.lib.network.IPacketSender;
import calclavia.lib.tile.TileEntityElectrical;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

/**
 * A modular battery with no GUI.
 * 
 * @author Calclavia, AidanBrady
 */
public class TileEntityBattery extends TileEntityElectrical implements IPacketSender, IPacketReceiver, IInventory
{
	public Set<EntityPlayer> playersUsing = new HashSet<EntityPlayer>();

	public SynchronizedBatteryData structure = SynchronizedBatteryData.getBase(this);
	public SynchronizedBatteryData prevStructure;

	public float clientEnergy;
	public int clientCells;
	public float clientMaxEnergy;

	private EnumSet inputSides = EnumSet.allOf(ForgeDirection.class);

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (!this.worldObj.isRemote)
		{
			if (this.ticks == 5 && !this.structure.isMultiblock)
			{
				this.update();
			}

			if (this.structure.visibleInventory[0] != null)
			{
				if (structure.inventory.size() < structure.getMaxCells())
				{
					if (structure.visibleInventory[0].getItem() instanceof ICapacitor)
					{
						structure.inventory.add(structure.visibleInventory[0]);
						structure.visibleInventory[0] = null;
						structure.sortInventory();
						updateAllClients();
					}
				}
			}

			/**
			 * Attempt to charge entities above it.
			 
			ItemStack chargeItem = null;

			if (this.worldObj.isBlockIndirectlyGettingPowered(this.xCoord, this.yCoord, this.zCoord))
			{
				List<Entity> entities = this.worldObj.getEntitiesWithinAABB(Entity.class, AxisAlignedBB.getBoundingBox(this.xCoord, this.yCoord + 1, this.zCoord, this.xCoord + 1, this.yCoord + 2, this.zCoord + 1));

				electricItemLoop:
				for (Entity entity : entities)
				{
					if (entity instanceof EntityPlayer)
					{
						IInventory inventory = ((EntityPlayer) entity).inventory;
						for (int i = 0; i < inventory.getSizeInventory(); i++)
						{
							ItemStack checkStack = inventory.getStackInSlot(i);

							if (checkStack != null)
							{
								if (checkStack.getItem() instanceof IElectricalItem)
								{
									if (((IElectricalItem) checkStack.getItem()).recharge(checkStack, this.energy.extractEnergy((this.getTransferThreshhold(), false).getWatts(), false) > 0)
									{
										chargeItem = checkStack;
										break electricItemLoop;
									}
								}
							}
						}
					}
					else if (entity instanceof EntityItem)
					{
						ItemStack checkStack = ((EntityItem) entity).getEntityItem();

						if (checkStack != null)
						{
							if (checkStack.getItem() instanceof IElectricalItem)
							{
								if (((IElectricalItem) checkStack.getItem()).recharge(checkStack, this.energy.extractEnergy((this.getTransferThreshhold(), false).getWatts(), false) > 0)
								{
									chargeItem = checkStack;
									break electricItemLoop;
								}
							}
						}
					}
				}
			}

			if (chargeItem == null)
			{
				chargeItem = this.structure.visibleInventory[1];
			}

			if (chargeItem != null)
			{
				ItemStack itemStack = chargeItem;
				IElectricalItem battery = (IElectricalItem) itemStack.getItem();

				float energyStored = getMaxEnergyStored();
				float batteryNeeded = battery.recharge(itemStack, this.energy.extractEnergy((this.getTransferThreshhold(), false).getWatts(), false);
				float toGive = Math.min(energyStored, Math.min(battery.getTransfer(itemStack), batteryNeeded));
				battery.recharge(itemStack, this.energy.extractEnergy((toGive, true).getWatts(), true);
			}

			if (structure.visibleInventory[2] != null)
			{
				ItemStack itemStack = structure.visibleInventory[2];
				IElectricalItem battery = (IElectricalItem) itemStack.getItem();

				float energyNeeded = getMaxEnergyStored() - getEnergyStored();
				float batteryStored = battery.getElectricityStored(itemStack);
				float toReceive = Math.min(energyNeeded, Math.min(this.getTransferThreshhold(), Math.min(battery.getTransfer(itemStack), batteryStored)));
				battery.discharge(itemStack, receiveElectricity(toReceive, true), true);
			}*/

			if (prevStructure != structure)
			{
				for (EntityPlayer player : playersUsing)
				{
					player.closeScreen();
				}

				updateClient();
			}

			this.prevStructure = structure;

			this.structure.wroteInventory = false;
			this.structure.didTick = false;

			if (this.playersUsing.size() > 0)
			{
				updateClient();
			}

			for (EntityPlayer player : this.playersUsing)
			{
				PacketDispatcher.sendPacketToPlayer(ResonantInduction.PACKET_TILE.getPacket(this, this.getPacketData(0).toArray()), (Player) player);
			}

			this.produce();
		}
	}

	public float getTransferThreshhold()
	{
		return this.structure.getVolume() * 50;
	}

	public void updateClient()
	{
		PacketDispatcher.sendPacketToAllPlayers(ResonantInduction.PACKET_TILE.getPacket(this, getPacketData(0).toArray()));
	}

	public void updateAllClients()
	{
		for (Vector3 vec : structure.locations)
		{
			TileEntityBattery battery = (TileEntityBattery) vec.getTileEntity(worldObj);
			PacketDispatcher.sendPacketToAllPlayers(ResonantInduction.PACKET_TILE.getPacket(battery, battery.getPacketData(0).toArray()));
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);

		// Main inventory
		if (nbtTags.hasKey("Items"))
		{
			NBTTagList tagList = nbtTags.getTagList("Items");
			structure.inventory = new ArrayList<ItemStack>();

			for (int tagCount = 0; tagCount < tagList.tagCount(); tagCount++)
			{
				NBTTagCompound tagCompound = (NBTTagCompound) tagList.tagAt(tagCount);
				int slotID = tagCompound.getInteger("Slot");
				structure.inventory.add(slotID, ItemStack.loadItemStackFromNBT(tagCompound));
			}
		}

		// Visible inventory
		if (nbtTags.hasKey("VisibleItems"))
		{
			NBTTagList tagList = nbtTags.getTagList("VisibleItems");
			structure.visibleInventory = new ItemStack[3];

			for (int tagCount = 0; tagCount < tagList.tagCount(); tagCount++)
			{
				NBTTagCompound tagCompound = (NBTTagCompound) tagList.tagAt(tagCount);
				byte slotID = tagCompound.getByte("Slot");

				if (slotID >= 0 && slotID < structure.visibleInventory.length)
				{
					if (slotID == 0)
					{
						setInventorySlotContents(slotID, ItemStack.loadItemStackFromNBT(tagCompound));
					}
					else
					{
						setInventorySlotContents(slotID + 1, ItemStack.loadItemStackFromNBT(tagCompound));
					}
				}
			}
		}

		this.inputSides = EnumSet.noneOf(ForgeDirection.class);

		NBTTagList tagList = nbtTags.getTagList("inputSides");

		for (int tagCount = 0; tagCount < tagList.tagCount(); tagCount++)
		{
			NBTTagCompound tagCompound = (NBTTagCompound) tagList.tagAt(tagCount);
			byte side = tagCompound.getByte("side");
			this.inputSides.add(ForgeDirection.getOrientation(side));
		}

		this.inputSides.remove(ForgeDirection.UNKNOWN);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);

		if (!structure.wroteInventory)
		{
			// Inventory
			if (structure.inventory != null)
			{
				NBTTagList tagList = new NBTTagList();

				for (int slotCount = 0; slotCount < structure.inventory.size(); slotCount++)
				{
					if (structure.inventory.get(slotCount) != null)
					{
						NBTTagCompound tagCompound = new NBTTagCompound();
						tagCompound.setInteger("Slot", slotCount);
						structure.inventory.get(slotCount).writeToNBT(tagCompound);
						tagList.appendTag(tagCompound);
					}
				}

				nbt.setTag("Items", tagList);
			}

			// Visible inventory
			if (structure.visibleInventory != null)
			{
				NBTTagList tagList = new NBTTagList();

				for (int slotCount = 0; slotCount < structure.visibleInventory.length; slotCount++)
				{
					if (slotCount > 0)
					{
						slotCount++;
					}

					if (getStackInSlot(slotCount) != null)
					{
						NBTTagCompound tagCompound = new NBTTagCompound();
						tagCompound.setByte("Slot", (byte) slotCount);
						getStackInSlot(slotCount).writeToNBT(tagCompound);
						tagList.appendTag(tagCompound);
					}
				}

				nbt.setTag("VisibleItems", tagList);
			}

			structure.wroteInventory = true;

			/**
			 * Save the input sides.
			 */
			NBTTagList tagList = new NBTTagList();
			Iterator<ForgeDirection> it = this.inputSides.iterator();

			while (it.hasNext())
			{
				ForgeDirection dir = it.next();

				if (dir != ForgeDirection.UNKNOWN)
				{
					NBTTagCompound tagCompound = new NBTTagCompound();
					tagCompound.setByte("side", (byte) dir.ordinal());
					tagList.appendTag(tagCompound);
				}
			}

			nbt.setTag("inputSides", tagList);
		}
	}

	public void update()
	{
		if (!worldObj.isRemote && (structure == null || !structure.didTick))
		{
			new BatteryUpdateProtocol(this).updateBatteries();

			if (structure != null)
			{
				structure.didTick = true;
			}
		}
	}

	@Override
	public void onReceivePacket(ByteArrayDataInput data, EntityPlayer player)
	{
		structure.isMultiblock = data.readBoolean();

		clientEnergy = data.readFloat();
		clientCells = data.readInt();
		clientMaxEnergy = data.readFloat();

		structure.height = data.readInt();
		structure.length = data.readInt();
		structure.width = data.readInt();
	}

	@Override
	public ArrayList getPacketData(int type)
	{
		ArrayList data = new ArrayList();
		data.add(structure.isMultiblock);

		data.add(structure.inventory.size());

		data.add(structure.height);
		data.add(structure.length);
		data.add(structure.width);

		return data;
	}

	@Override
	public int getSizeInventory()
	{
		return 4;
	}

	@Override
	public ItemStack getStackInSlot(int i)
	{
		if (i == 0)
		{
			return structure.visibleInventory[0];
		}
		else if (i == 1)
		{
			if (!worldObj.isRemote)
			{
				return ListUtil.getTop(structure.inventory);
			}
			else
			{
				return structure.tempStack;
			}
		}
		else
		{
			return structure.visibleInventory[i - 1];
		}
	}

	@Override
	public ItemStack decrStackSize(int slotID, int amount)
	{
		if (getStackInSlot(slotID) != null)
		{
			ItemStack tempStack;

			if (getStackInSlot(slotID).stackSize <= amount)
			{
				tempStack = getStackInSlot(slotID);
				setInventorySlotContents(slotID, null);
				return tempStack;
			}
			else
			{
				tempStack = getStackInSlot(slotID).splitStack(amount);

				if (getStackInSlot(slotID).stackSize == 0)
				{
					setInventorySlotContents(slotID, null);
				}

				return tempStack;
			}
		}
		else
		{
			return null;
		}
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i)
	{
		return getStackInSlot(i);
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack)
	{
		if (i == 0)
		{
			structure.visibleInventory[0] = itemstack;
		}
		else if (i == 1)
		{
			if (itemstack == null)
			{
				if (!worldObj.isRemote)
				{
					structure.inventory.remove(ListUtil.getTop(structure.inventory));
				}
				else
				{
					structure.tempStack = null;
				}
			}
			else
			{
				if (worldObj.isRemote)
				{
					structure.tempStack = itemstack;
				}
			}
		}
		else
		{
			structure.visibleInventory[i - 1] = itemstack;
		}
	}

	@Override
	public String getInvName()
	{
		return "Battery";
	}

	@Override
	public boolean isInvNameLocalized()
	{
		return false;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		return true;
	}

	@Override
	public void openChest()
	{
	}

	@Override
	public void closeChest()
	{
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemsSack)
	{
		return itemsSack.getItem() instanceof IElectricalItem;
	}

	@Override
	public EnumSet<ForgeDirection> getInputDirections()
	{
		return this.inputSides;
	}

	@Override
	public EnumSet<ForgeDirection> getOutputDirections()
	{
		return EnumSet.complementOf(this.inputSides);
	}

	/**
	 * Toggles the input/output sides of the battery.
	 */
	public boolean toggleSide(ForgeDirection orientation)
	{
		if (this.inputSides.contains(orientation))
		{
			this.inputSides.remove(orientation);
			return false;
		}
		else
		{
			this.inputSides.add(orientation);
			return true;
		}
	}
}
