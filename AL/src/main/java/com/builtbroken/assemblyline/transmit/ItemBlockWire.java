package com.builtbroken.assemblyline.transmit;

import net.minecraft.util.Icon;

import com.builtbroken.assemblyline.ALRecipeLoader;
import com.builtbroken.minecraft.prefab.ItemBlockHolder;

public class ItemBlockWire extends ItemBlockHolder
{

    public ItemBlockWire(int id)
    {
        super(id);
    }

    @Override
    public Icon getIconFromDamage(int par1)
    {
        return ALRecipeLoader.blockWire instanceof BlockWire ? ((BlockWire) ALRecipeLoader.blockWire).wireIcon : ALRecipeLoader.blockWire.getIcon(0, par1);
    }

}
