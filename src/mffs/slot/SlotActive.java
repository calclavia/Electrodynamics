package mffs.slot;

import mffs.base.TileEntityInventory;
import net.minecraft.entity.player.EntityPlayer;

public class SlotActive extends SlotBase
{
	public SlotActive(TileEntityInventory tileEntity, int id, int par4, int par5)
	{
		super(tileEntity, id, par4, par5);
	}

	public boolean canTakeStack(EntityPlayer par1EntityPlayer)
	{
		return !this.tileEntity.isActive();
	}
}
