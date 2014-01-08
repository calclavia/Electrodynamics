/**
 * 
 */
package resonantinduction.core.base;

import calclavia.lib.prefab.block.BlockRotatable;
import net.minecraft.block.material.Material;
import net.minecraftforge.common.Configuration;
import resonantinduction.Reference;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.ResonantInductionTabs;
import universalelectricity.api.UniversalElectricity;

/**
 * @author Calclavia
 * 
 */
public class BlockRotatableBase extends BlockRotatable
{
	public BlockRotatableBase(String name, int id)
	{
		super(ResonantInduction.CONFIGURATION.get(Configuration.CATEGORY_BLOCK, name, id).getInt(id), UniversalElectricity.machine);
		this.setCreativeTab(ResonantInductionTabs.CORE);
		this.setUnlocalizedName(Reference.PREFIX + name);
		this.setTextureName(Reference.PREFIX + name);
		this.setHardness(1f);
	}
}
