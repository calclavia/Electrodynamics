package resonantinduction.assemblyline.blocks;

import resonantinduction.assemblyline.AssemblyLine;
import resonantinduction.assemblyline.IndustryTabs;
import net.minecraft.block.material.Material;

import com.builtbroken.minecraft.prefab.BlockMachine;

public abstract class BlockHydraulic extends BlockMachine
{

    public BlockHydraulic(String name, Material material)
    {
        super(AssemblyLine.CONFIGURATION, name, material);
        this.setCreativeTab(IndustryTabs.tabHydraulic());
    }
}
