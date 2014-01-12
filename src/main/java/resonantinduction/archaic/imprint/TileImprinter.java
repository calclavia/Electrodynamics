package resonantinduction.archaic.imprint;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.packet.Packet;
import resonantinduction.api.IArmbot;
import resonantinduction.api.IArmbotUseable;
import resonantinduction.api.events.AutoCraftEvent;
import resonantinduction.core.ResonantInduction;
import resonantinduction.electrical.encoder.coding.args.ArgumentData;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.network.IPacketReceiver;
import calclavia.lib.network.PacketHandler;
import calclavia.lib.prefab.slot.ISlotPickResult;
import calclavia.lib.prefab.tile.TileAdvanced;

import com.builtbroken.common.Pair;
import com.google.common.io.ByteArrayDataInput;

public class TileImprinter extends TileAdvanced implements ISidedInventory, IPacketReceiver
{
	public ItemStack[] inventory = new ItemStack[10];

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

			if (fitlerStack != null && fitlerStack.getItem() instanceof ItemBlockFilter)
			{
				ItemStack outputStack = fitlerStack.copy();
				outputStack.stackSize = 1;
				ArrayList<ItemStack> filters = ItemBlockFilter.getFilters(outputStack);
				boolean filteringItemExists = false;

				for (ItemStack filteredStack : filters)
				{
					if (filteredStack.isItemEqual(fitlerStack))
					{
						filters.remove(filteredStack);
						filteringItemExists = true;
						break;
					}
				}

				if (!filteringItemExists)
				{
					filters.add(fitlerStack);
				}

				ItemBlockFilter.setFilters(outputStack, filters);
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
