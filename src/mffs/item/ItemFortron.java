package mffs.item;

import mffs.base.ItemBase;
import mffs.fortron.FortronHelper;
import net.minecraft.client.renderer.texture.IconRegister;

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
