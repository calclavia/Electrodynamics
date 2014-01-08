/**
 * 
 */
package resonantinduction.core.base;

import net.minecraft.block.material.Material;
import net.minecraftforge.common.Configuration;
import resonantinduction.core.ResonantInduction;
import resonantinduction.core.ResonantInductionTabs;
import calclavia.lib.prefab.block.BlockSidedIO;

/**
 * @author Calclavia
 * 
 */
public class BlockIOBase extends BlockSidedIO
{
	public BlockIOBase(String name, int id)
	{
		super(ResonantInduction.CONFIGURATION.get(Configuration.CATEGORY_BLOCK, name, id).getInt(id), Material.piston);
		this.setCreativeTab(ResonantInductionTabs.CORE);
		this.setUnlocalizedName(ResonantInduction.PREFIX + name);
		this.setTextureName(ResonantInduction.PREFIX + name);
		this.setHardness(1f);
	}
}
