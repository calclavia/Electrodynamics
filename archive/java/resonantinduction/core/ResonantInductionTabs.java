/**
 * 
 */
package resonantinduction.core;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

/**
 * @author Calclavia
 * 
 */
public class ResonantInductionTabs extends CreativeTabs
{
	public static final ResonantInductionTabs CORE = new ResonantInductionTabs(CreativeTabs.getNextID(), Reference.DOMAIN);
	public static ItemStack ITEMSTACK;

	public ResonantInductionTabs(int par1, String par2Str)
	{
		super(par1, par2Str);

	}

	@Override
	public ItemStack getIconItemStack()
	{
		return ITEMSTACK;
	}

}
