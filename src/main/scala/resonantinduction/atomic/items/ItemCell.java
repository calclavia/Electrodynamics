package resonantinduction.atomic.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import resonant.content.prefab.itemblock.ItemTooltip;
import resonant.lib.utility.LanguageUtility;
import resonantinduction.atomic.AtomicContent;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantTab;

public class ItemCell extends ItemTooltip
{
	public ItemCell()
	{
		setContainerItem(AtomicContent.itemCell());
	}

    public ItemCell(String name)
    {
        if(!name.equalsIgnoreCase("cellEmpty"))
            this.setContainerItem(AtomicContent.itemCell());
        this.setUnlocalizedName(Reference.prefix() + name);
        this.setTextureName(Reference.prefix() + name);
        setCreativeTab(ResonantTab.tab());
    }

	@Override
	public String getUnlocalizedName(ItemStack itemstack)
	{
		String localized = LanguageUtility.getLocal(getUnlocalizedName() + "." + itemstack.getItemDamage() + ".name");
		if (localized != null && !localized.isEmpty())
		{
			return getUnlocalizedName() + "." + itemstack.getItemDamage();
		}

		return getUnlocalizedName();
	}
}
