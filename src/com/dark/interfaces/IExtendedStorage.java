package com.dark.interfaces;

import net.minecraft.item.ItemStack;

/** Applied to blocks that store items in stacks above 64 and as one large collective of items
 * 
 * @author DarkGuardsman */
public interface IExtendedStorage
{
    public ItemStack addStackToStorage(ItemStack stack);
}
