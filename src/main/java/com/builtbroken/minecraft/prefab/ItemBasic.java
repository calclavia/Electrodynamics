package com.builtbroken.minecraft.prefab;

import net.minecraft.item.Item;
import net.minecraftforge.common.Configuration;

import com.builtbroken.minecraft.DarkCore;

public class ItemBasic extends Item
{
    public ItemBasic(int itemID, String name, Configuration config)
    {
        super(config.getItem(name, itemID).getInt());
        this.setUnlocalizedName(name);
    }

    public ItemBasic(String name, Configuration config)
    {
        this(DarkCore.getNextID(), name, config);
    }
}
