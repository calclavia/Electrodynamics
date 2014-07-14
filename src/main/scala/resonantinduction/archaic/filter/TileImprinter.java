package resonantinduction.archaic.filter;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.packet.Packet;
import resonant.lib.network.IPacketReceiver;
import resonant.lib.network.PacketHandler;
import resonant.lib.prefab.tile.TileAdvanced;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.prefab.imprint.ItemImprint;

import com.google.common.io.ByteArrayDataInput;

public class TileImprinter extends TileAdvanced implements ISidedInventory, IPacketReceiver
{
	public ItemStack[] inventory = new ItemStack[10];

	@Override
	public Packet getDescriptionPacket()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		this.writeToNBT(nbt);
		return ResonantInduction.PACKET_TILE.getPacket(this, nbt);
	}

	@Override
	public void onReceivePacket(ByteArrayDataInput data, EntityPlayer player, Object... extra)
	{
		try
		{
			this.readFromNBT(PacketHandler.readNBTTagCompound(data));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Inventory methods.
	 */
	@Override
	public boolean canUpdate()
	{
		return false;
	}

	@Override
	public int getSizeInventory()
	{
		return this.inventory.length;
	}

	/**
	 * Sets the given item stack to the specified slot in the inventory (can be crafting or armor
	 * sections).
	 */
	@Override
	public void setInventorySlotContents(int slot, ItemStack itemStack)
	{
		if (slot < this.getSizeInventory())
		{
			inventory[slot] = itemStack;
		}
	}

	@Override
	public ItemStack decrStackSize(int i, int amount)
	{
		if (this.getStackInSlot(i) != null)
		{
			ItemStack stack;

			if (this.getStackInSlot(i).stackSize <= amount)
			{
				stack = this.getStackInSlot(i);
				this.setInventorySlotContents(i, null);
				return stack;
			}
			else
			{
				stack = this.getStackInSlot(i).splitStack(amount);

				if (this.getStackInSlot(i).stackSize == 0)
				{
					this.setInventorySlotContents(i, null);
				}

				return stack;
			}
		}
		else
		{
			return null;
		}
	}

	@Override
	public ItemStack getStackInSlot(int slot)
	{
		return this.inventory[slot];
	}

	/**
	 * When some containers are closed they call this on each slot, then drop whatever it returns as
	 * an EntityItem - like when you close a workbench GUI.
	 */
	@Override
	public ItemStack getStackInSlotOnClosing(int slot)
	{
		if (this.getStackInSlot(slot) != null)
		{
			ItemStack var2 = this.getStackInSlot(slot);
			this.setInventorySlotContents(slot, null);
			return var2;
		}
		else
		{
			return null;
		}
	}

	@Override
	public void openChest()
	{
		this.onInventoryChanged();
	}

	@Override
	public void closeChest()
	{
		this.onInventoryChanged();
	}

	/** Updates all the output slots. Call this to update the Imprinter. */
	@Override
	public void onInventoryChanged()
	{
		if (!this.worldObj.isRemote)
		{
			/** Makes the stamping recipe for filters */
			ItemStack fitlerStack = this.inventory[9];

			if (fitlerStack != null && fitlerStack.getItem() instanceof ItemImprint)
			{
				ItemStack outputStack = fitlerStack.copy();
				Set<ItemStack> filters = ItemImprint.getFilters(outputStack);
				Set<ItemStack> toAdd = new HashSet<ItemStack>();

				/** A hashset of to be imprinted items containing NO repeats. */
				Set<ItemStack> toBeImprinted = new HashSet<ItemStack>();

				check:
				for (int i = 0; i < 9; i++)
				{
					ItemStack stackInInventory = inventory[i];

					if (stackInInventory != null)
					{
						for (ItemStack check : toBeImprinted)
						{
							if (check.isItemEqual(stackInInventory))
								continue check;
						}

						toBeImprinted.add(stackInInventory);
					}
				}

				for (ItemStack stackInInventory : toBeImprinted)
				{
					Iterator<ItemStack> it = filters.iterator();

					boolean removed = false;

					while (it.hasNext())
					{
						ItemStack filteredStack = it.next();

						if (filteredStack.isItemEqual(stackInInventory))
						{
							it.remove();
							removed = true;
						}
					}

					if (!removed)
						toAdd.add(stackInInventory);
				}

				filters.addAll(toAdd);

				ItemImprint.setFilters(outputStack, filters);
				this.inventory[9] = outputStack;
			}
		}
	}

	// ///////////////////////////////////////
	// // Save And Data processing //////
	// ///////////////////////////////////////
	/** NBT Data */
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);

		NBTTagList var2 = nbt.getTagList("Items");
		this.inventory = new ItemStack[10];

		for (int i = 0; i < var2.tagCount(); ++i)
		{
			NBTTagCompound var4 = (NBTTagCompound) var2.tagAt(i);
			byte var5 = var4.getByte("Slot");

			if (var5 >= 0 && var5 < this.getSizeInventory())
			{
				this.setInventorySlotContents(var5, ItemStack.loadItemStackFromNBT(var4));
			}
		}
	}

	/** Writes a tile entity to NBT. */
	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);

		NBTTagList var2 = new NBTTagList();

		for (int i = 0; i < this.getSizeInventory(); ++i)
		{
			if (this.getStackInSlot(i) != null)
			{
				NBTTagCompound var4 = new NBTTagCompound();
				var4.setByte("Slot", (byte) i);
				this.getStackInSlot(i).writeToNBT(var4);
				var2.appendTag(var4);
			}
		}

		nbt.setTag("Items", var2);
	}

	// ///////////////////////////////////////
	// // Inventory Access side Methods //////
	// ///////////////////////////////////////
	@Override
	public boolean isInvNameLocalized()
	{
		return false;
	}

	@Override
	public String getInvName()
	{
		return getBlockType().getLocalizedName();
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		return true;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		return this.worldObj.getBlockTileEntity(this.xCoord, this.yCoord, this.zCoord) != this ? false : entityplayer.getDistanceSq(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D) <= 64.0D;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		return side == 1 ? new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8 } : new int[10];
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack itemstack, int side)
	{
		return this.isItemValidForSlot(slot, itemstack);
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack itemstack, int side)
	{
		return this.isItemValidForSlot(slot, itemstack);
	}
}
