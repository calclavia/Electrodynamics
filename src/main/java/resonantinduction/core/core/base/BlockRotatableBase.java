/**
 * 
 */
package resonantinduction.core.core.base;

import calclavia.lib.prefab.block.BlockRotatable;
import net.minecraft.block.material.Material;
import net.minecraftforge.common.Configuration;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.TabRI;
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
		this.setCreativeTab(TabRI.INSTANCE);
		this.setUnlocalizedName(ResonantInduction.PREFIX + name);
		this.setTextureName(ResonantInduction.PREFIX + name);
		this.setHardness(1f);
	}
}