package com.builtbroken.assemblyline.api;

import net.minecraft.item.ItemStack;

/** Applied to TileEntities that can accept a filter.z
 * 
 * @author Calclavia */
public interface IFilterable
{
    public void setFilter(ItemStack filter);

    public ItemStack getFilter();
}
