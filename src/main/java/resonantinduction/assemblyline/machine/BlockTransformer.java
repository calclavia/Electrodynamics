package resonantinduction.assemblyline.machine;

import resonantinduction.assemblyline.AssemblyLine;
import resonantinduction.assemblyline.IndustryTabs;
import net.minecraft.block.material.Material;
import net.minecraftforge.common.Configuration;
import universalelectricity.api.UniversalElectricity;

import com.builtbroken.minecraft.prefab.BlockMachine;

public class BlockTransformer extends BlockMachine
{
    public BlockTransformer(Configuration config, String blockName, Material material)
    {
        super(AssemblyLine.CONFIGURATION, "Transformer", UniversalElectricity.machine);
        this.setCreativeTab(IndustryTabs.tabIndustrial());
    }
}
