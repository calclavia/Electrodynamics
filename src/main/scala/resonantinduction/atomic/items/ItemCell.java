package resonantinduction.atomic.items;

import cpw.mods.fml.relauncher.SideOnly;
import resonant.content.prefab.itemblock.ItemTooltip;
import resonantinduction.atomic.Atomic;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import resonant.lib.utility.LanguageUtility;

public class ItemCell extends ItemTooltip
{
    public ItemCell()
    {
        setContainerItem(Atomic.itemCell);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister iconRegister)
    {
        this.itemIcon = iconRegister.registerIcon(this.getUnlocalizedName().replace("item.", ""));
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
