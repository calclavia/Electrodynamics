package resonantinduction.blocks;

import net.minecraft.block.material.Material;
import resonantinduction.core.ResonantInductionTabs;
import resonantinduction.transport.ResonantInductionTransport;

import com.builtbroken.minecraft.prefab.BlockMachine;

public abstract class BlockHydraulic extends BlockMachine
{

	public BlockHydraulic(String name, Material material)
	{
		super(ResonantInductionTransport.CONFIGURATION, name, material);
		this.setCreativeTab(ResonantInductionTabs.tabHydraulic());
	}
}
