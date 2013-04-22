package mffs.item;

import net.minecraft.client.renderer.texture.IconRegister;
import mffs.base.ItemBase;
import mffs.fortron.FortronHelper;

public class ItemFortron extends ItemBase
{
	public ItemFortron(int id)
	{
		super(id, "fortron");
		this.setCreativeTab(null);
	}

	@Override
	public void registerIcons(IconRegister par1IconRegister)
	{
		super.registerIcons(par1IconRegister);
		FortronHelper.LIQUID_FORTRON.setRenderingIcon(this.itemIcon);
	}
}
