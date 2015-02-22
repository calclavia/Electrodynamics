package mffs.slot;

import mffs.base.TileFrequency;
import net.minecraft.item.Item;
import resonantengine.api.item.IItemFrequency;
import resonantengine.api.tile.IBlockFrequency;

public class SlotCard extends SlotBase
{
	public SlotCard(TileFrequency tileEntity, int id, int par4, int par5)
	{
		super(tileEntity, id, par4, par5);
	}

	@Override
	public void onSlotChanged()
	{
		super.onSlotChanged();
		Item Item = this.getStack();

		if (Item != null)
		{
			if (Item.getItem() instanceof IItemFrequency)
			{
				((IItemFrequency) Item.getItem()).setFrequency(((IBlockFrequency) tileEntity).getFrequency(), Item);
			}
		}
	}
}
