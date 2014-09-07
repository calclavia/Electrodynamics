package resonantinduction.atomic.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;

/**
 * Strange matter cell
 */
public class ItemDarkMatter extends ItemCell
{
	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IIconRegister iconRegister)
	{
		// Animated IIcon
		this.itemIcon = iconRegister.registerIcon(this.getUnlocalizedName().replace("item.", ""));
	}
}
