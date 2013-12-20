package com.dark.prefab.invgui;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/** Slot that can only allow one itemStack into it
 * 
 * @author DarkGuardsman */
public class SlotRestricted extends Slot
{
    private ItemStack[] itemStacks;

    public SlotRestricted(IInventory par1iInventory, int par2, int par3, int par4, ItemStack... itemStacks)
    {
        super(par1iInventory, par2, par3, par4);
        this.itemStacks = itemStacks;
    }

    @Override
    public boolean isItemValid(ItemStack itemStack)
    {
        if (itemStack != null && this.itemStacks != null)
        {
            for (int i = 0; i < itemStacks.length; i++)
            {
                ItemStack stack = itemStacks[i];
                if (stack != null && itemStack.isItemEqual(stack))
                {
                    return true;
                }

            }
        }
        return false;
    }
}
