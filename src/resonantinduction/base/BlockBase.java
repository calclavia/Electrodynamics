/**
 * 
 */
package resonantinduction.base;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraftforge.common.Configuration;
import resonantinduction.ResonantInduction;
import resonantinduction.TabRI;

/**
 * @author Calclavia
 * 
 */
public class BlockBase extends Block
{
	public BlockBase(String name, int id, Material material)
	{
		super(ResonantInduction.CONFIGURATION.get(Configuration.CATEGORY_BLOCK, name, id).getInt(id), material);
		this.setCreativeTab(TabRI.INSTANCE);
		this.setUnlocalizedName(ResonantInduction.PREFIX + name);
		this.func_111022_d(ResonantInduction.PREFIX + name);
	}
}
