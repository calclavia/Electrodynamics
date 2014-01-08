package com.builtbroken.assemblyline.blocks;

import net.minecraft.block.material.Material;

import com.builtbroken.assemblyline.AssemblyLine;
import com.builtbroken.assemblyline.IndustryTabs;
import com.builtbroken.minecraft.prefab.BlockMachine;

public abstract class BlockHydraulic extends BlockMachine
{

    public BlockHydraulic(String name, Material material)
    {
        super(AssemblyLine.CONFIGURATION, name, material);
        this.setCreativeTab(IndustryTabs.tabHydraulic());
    }
}
