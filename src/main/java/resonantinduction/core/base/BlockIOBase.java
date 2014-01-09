/**
 * 
 */
package resonantinduction.core.base;

import net.minecraft.block.material.Material;
import net.minecraftforge.common.Configuration;
import resonantinduction.Reference;
import resonantinduction.core.ResonantInductionTabs;
import resonantinduction.core.Settings;
import calclavia.lib.prefab.block.BlockSidedIO;

/**
 * @author Calclavia
 * 
 */
public class BlockIOBase extends BlockSidedIO
{
	public BlockIOBase(String name, int id)
	{
		super(Settings.CONFIGURATION.get(Configuration.CATEGORY_BLOCK, name, id).getInt(id), Material.piston);
		this.setCreativeTab(ResonantInductionTabs.CORE);
		this.setUnlocalizedName(Reference.PREFIX + name);
		this.setTextureName(Reference.PREFIX + name);
		this.setHardness(1f);
	}
}
