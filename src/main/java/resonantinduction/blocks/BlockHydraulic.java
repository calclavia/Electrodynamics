package resonantinduction.blocks;

import resonantinduction.AssemblyLine;
import resonantinduction.IndustryTabs;
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
