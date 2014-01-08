/**
 * 
 */
package resonantinduction.core.base;

import net.minecraft.block.material.Material;
import net.minecraftforge.common.Configuration;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.ResonantInductionTabs;
import universalelectricity.api.UniversalElectricity;
import calclavia.lib.prefab.block.BlockAdvanced;

/**
 * @author Calclavia
 * 
 */
public class BlockBase extends BlockAdvanced
{
	public BlockBase(String name, int id)
	{
		super(ResonantInduction.CONFIGURATION.get(Configuration.CATEGORY_BLOCK, name, id).getInt(id), UniversalElectricity.machine);
		this.setCreativeTab(ResonantInductionTabs.CORE);
		this.setUnlocalizedName(ResonantInduction.PREFIX + name);
		this.setTextureName(ResonantInduction.PREFIX + name);
		this.setHardness(1f);
	}
}
