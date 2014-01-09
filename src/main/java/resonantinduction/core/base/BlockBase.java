/**
 * 
 */
package resonantinduction.core.base;

import net.minecraftforge.common.Configuration;
import resonantinduction.Reference;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.ResonantInductionTabs;
import resonantinduction.core.Settings;
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
		super(Settings.CONFIGURATION.get(Configuration.CATEGORY_BLOCK, name, id).getInt(id), UniversalElectricity.machine);
		this.setCreativeTab(ResonantInductionTabs.CORE);
		this.setUnlocalizedName(Reference.PREFIX + name);
		this.setTextureName(Reference.PREFIX + name);
		this.setHardness(1f);
	}
}
