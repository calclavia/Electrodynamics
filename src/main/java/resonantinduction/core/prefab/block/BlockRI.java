package resonantinduction.core.prefab.block;

import net.minecraft.block.material.Material;
import net.minecraftforge.common.Configuration;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantInductionTabs;
import resonantinduction.core.Settings;
import universalelectricity.api.UniversalElectricity;
import calclavia.lib.prefab.block.BlockTile;

/**
 * Basic prefab for machine
 * 
 * @author DarkGuardsman
 * 
 */
public class BlockRI extends BlockTile
{
	public BlockRI(String name)
	{
		this(name, UniversalElectricity.machine);
	}

	public BlockRI(String name, Material material)
	{
		this(Settings.getNextBlockID(), name, material);
	}

	public BlockRI(int id, String name, Material material)
	{
		super(Settings.CONFIGURATION.get(Configuration.CATEGORY_BLOCK, name, id).getInt(id), material);
		this.setCreativeTab(ResonantInductionTabs.CORE);
		this.setUnlocalizedName(Reference.PREFIX + name);
		this.setTextureName(Reference.PREFIX + name);
		this.setHardness(1f);
	}

}
