package com.builtbroken.minecraft.interfaces;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;

public interface IExternalInv
{
	public IInvBox getInventory();

	public boolean canStore(ItemStack stack, int slot, ForgeDirection side);

	public boolean canRemove(ItemStack stack, int slot, ForgeDirection side);
}
