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
public class TabRI extends CreativeTabs
{
	public static final TabRI DEFAULT = new TabRI(CreativeTabs.getNextID(), Reference.DOMAIN);
	public static final TabRI CORE = new TabRI(CreativeTabs.getNextID(), Reference.DOMAIN + ".core");
	public static ItemStack ITEMSTACK;

	public TabRI(int par1, String par2Str)
	{
		super(par1, par2Str);

	}

	@Override
	public ItemStack getIconItemStack()
	{
		return ITEMSTACK;
	}

}
