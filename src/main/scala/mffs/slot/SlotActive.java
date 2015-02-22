package mffs.slot;

import mffs.base.TileMFFSInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;

public class SlotActive extends SlotBase
{
	public SlotActive(TileMFFSInventory tileEntity, int id, int par4, int par5)
	{
		super(tileEntity, id, par4, par5);
	}

	@Override
	public boolean isItemValid(Item Item)
	{
		return super.isItemValid(Item) && !this.tileEntity.isActive();
	}

	@Override
	public boolean canTakeStack(EntityPlayer par1EntityPlayer)
	{
		return !this.tileEntity.isActive();
	}
}
