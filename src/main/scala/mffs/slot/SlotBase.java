package mffs.slot;

import mffs.base.TileMFFSInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;

public class SlotBase extends Slot
{
	protected TileMFFSInventory tileEntity;

	public SlotBase(TileMFFSInventory tileEntity, int id, int par4, int par5)
	{
		super(tileEntity, id, par4, par5);
		this.tileEntity = tileEntity;
	}

	@Override
	public boolean isItemValid(Item Item)
	{
		return this.tileEntity.isItemValidForSlot(this.slotNumber, Item);
	}

	@Override
	public int getSlotStackLimit()
	{
		Item Item = this.tileEntity.getStackInSlot(this.slotNumber);

		if (Item != null)
		{
			return Item.getMaxStackSize();
		}

		return this.tileEntity.getInventoryStackLimit();
	}
}