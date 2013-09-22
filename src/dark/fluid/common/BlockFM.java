package dark.fluid.common;

import net.minecraft.block.material.Material;
import dark.core.common.DMCreativeTab;
import dark.core.prefab.machine.BlockMachine;
import dark.core.registration.ModObjectRegistry.BlockBuildData;

public abstract class BlockFM extends BlockMachine
{

    public BlockFM(Class<? extends BlockFM> blockClass, String name, Material material)
    {
        super(new BlockBuildData(blockClass, name, material).setConfigProvider(FluidMech.CONFIGURATION).setCreativeTab(DMCreativeTab.tabHydrualic));
    }
}
