package resonantinduction.blocks;

import resonantinduction.AssemblyLine;
import resonantinduction.ResonantInductionTabs;
import net.minecraft.block.material.Material;

import com.builtbroken.minecraft.prefab.BlockMachine;

public abstract class BlockHydraulic extends BlockMachine
{

    public BlockHydraulic(String name, Material material)
    {
        super(ResonantInductionTransport.CONFIGURATION, name, material);
        this.setCreativeTab(ResonantInductionTabs.tabHydraulic());
    }
}
