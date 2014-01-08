package com.builtbroken.minecraft.prefab.invgui;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

import com.builtbroken.minecraft.interfaces.IExternalInv;
import com.builtbroken.minecraft.interfaces.IInvBox;

public class InvChest implements IInvBox
{
	/** Access able slots side all */
	protected int[] openSlots;
	/** Items contained in this inv */
	protected ItemStack[] containedItems;
	/** Host tileEntity */
	protected TileEntity hostTile;
	/** Host tileEntity as external inv */
	protected IExternalInv inv;
	/** Default slot max count */
	protected final int slots;

	public InvChest(TileEntity chest, IExternalInv inv, int slots)
	{
		this.hostTile = chest;
		this.slots = slots;
		this.inv = inv;
	}

	public InvChest(TileEntity chest, int slots)
	{
		this(chest, ((IExternalInv) chest), slots);
	}

	public InvChest(Entity entity, int i)
	{
		this.slots = i;
		this.inv = (IExternalInv) entity;
	}

	@Override
	public int getSizeInventory()
	{
		return slots;
	}

	@Override
	public ItemStack getStackInSlot(int slot)
	{
		return this.getContainedItems()[slot];
	}

	@Override
	public ItemStack decrStackSize(int slot, int ammount)
	{
		if (this.getContainedItems()[slot] != null)
		{
			ItemStack var3;

			if (this.getContainedItems()[slot].stackSize <= ammount)
			{
				var3 = this.getContainedItems()[slot];
				this.getContainedItems()[slot] = null;
				this.onInventoryChanged();
				return var3;
			}
			else
			{
				var3 = this.getContainedItems()[slot].splitStack(ammount);

				if (this.getContainedItems()[slot].stackSize == 0)
				{
					this.getContainedItems()[slot] = null;
				}

				this.onInventoryChanged();
				return var3;
			}
		}
		else
		{
			return null;
		}
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int par1)
	{
		if (this.getContainedItems()[par1] != null)
		{
			ItemStack var2 = this.getContainedItems()[par1];
			this.getContainedItems()[par1] = null;
			return var2;
		}
		else
		{
			return null;
		}
	}

	@Override
	public void setInventorySlotContents(int par1, ItemStack par2ItemStack)
	{
		this.getContainedItems()[par1] = par2ItemStack;

		if (par2ItemStack != null && par2ItemStack.stackSize > this.getInventoryStackLimit())
		{
			par2ItemStack.stackSize = this.getInventoryStackLimit();
		}

		this.onInventoryChanged();
	}

	@Override
	public String getInvName()
	{
		return "container.chest";
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
	public boolean isInvNameLocalized()
	{
		return false;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		if (i >= this.getSizeInventory())
		{
			return false;
		}
		return true;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int var1)
	{
		if (openSlots == null || openSlots.length != this.getSizeInventory())
		{
			this.openSlots = new int[this.getSizeInventory()];
			for (int i = 0; i < this.openSlots.length; i++)
			{
				openSlots[i] = i;
			}
		}
		return this.openSlots;
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemstack, int j)
	{
		return this.isItemValidForSlot(i, itemstack) && this.inv.canStore(itemstack, i, ForgeDirection.getOrientation(j));
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j)
	{
		return this.inv.canRemove(itemstack, i, ForgeDirection.getOrientation(j));
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public void onInventoryChanged()
	{
		if (this.hostTile != null)
		{
			this.hostTile.onInventoryChanged();
		}
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer par1EntityPlayer)
	{
		if (this.hostTile != null)
		{
			return this.hostTile.worldObj.getBlockTileEntity(this.hostTile.xCoord, this.hostTile.yCoord, this.hostTile.zCoord) != this.hostTile ? false : par1EntityPlayer.getDistanceSq(this.hostTile.xCoord + 0.5D, this.hostTile.yCoord + 0.5D, this.hostTile.zCoord + 0.5D) <= 64.0D;
		}
		return true;
	}

	@Override
	public ItemStack[] getContainedItems()
	{
		if (this.containedItems == null)
		{
			this.containedItems = new ItemStack[this.getSizeInventory()];
		}
		return this.containedItems;
	}

	@Override
	public NBTTagCompound saveInv(NBTTagCompound nbt)
	{
		NBTTagList itemList = new NBTTagList();
		for (int s = 0; s < this.getContainedItems().length; ++s)
		{
			if (this.getContainedItems()[s] != null)
			{
				NBTTagCompound tag = new NBTTagCompound();
				tag.setByte("Slot", (byte) s);
				this.getContainedItems()[s].writeToNBT(tag);
				itemList.appendTag(tag);
			}
		}
		nbt.setTag("Items", itemList);
		return nbt;
	}

	@Override
	public void loadInv(NBTTagCompound nbt)
	{
		// chest inv reading
		NBTTagList itemList = nbt.getTagList("Items");

		for (int s = 0; s < itemList.tagCount(); ++s)
		{
			NBTTagCompound tag = (NBTTagCompound) itemList.tagAt(s);
			int slotID = tag.getByte("Slot") & 255;

			if (slotID >= 0 && slotID < this.getContainedItems().length)
			{
				this.getContainedItems()[slotID] = ItemStack.loadItemStackFromNBT(tag);
			}
		}

	}

	@Override
	public void clear()
	{
		this.containedItems = null;
		this.getContainedItems();
	}

}
