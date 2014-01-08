package com.builtbroken.assemblyline.redstone;

import net.minecraft.block.material.Material;

import com.builtbroken.assemblyline.AssemblyLine;
import com.builtbroken.minecraft.prefab.BlockMachine;

/** This will be a piston that can extend from 1 - 20 depending on teir and user settings
 * 
 * @author Guardsman */
public class BlockPistonPlus extends BlockMachine
{

    public BlockPistonPlus()
    {
        super(AssemblyLine.CONFIGURATION, "DMPiston", Material.piston);
    }

}
