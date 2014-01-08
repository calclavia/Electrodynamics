package com.builtbroken.assemblyline.machine;

import net.minecraft.block.material.Material;
import net.minecraftforge.common.Configuration;
import universalelectricity.api.UniversalElectricity;

import com.builtbroken.assemblyline.AssemblyLine;
import com.builtbroken.assemblyline.IndustryTabs;
import com.builtbroken.minecraft.prefab.BlockMachine;

public class BlockTransformer extends BlockMachine
{
    public BlockTransformer(Configuration config, String blockName, Material material)
    {
        super(AssemblyLine.CONFIGURATION, "Transformer", UniversalElectricity.machine);
        this.setCreativeTab(IndustryTabs.tabIndustrial());
    }
}
