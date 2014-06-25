package resonantinduction.atomic.items;

import resonantinduction.core.ResonantInduction;
import net.minecraft.client.renderer.texture.IconRegister;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;


/** Strange matter cell */
public class ItemDarkMatter extends ItemCell
{
    public ItemDarkMatter(int itemID)
    {
        super(itemID);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IconRegister iconRegister)
    {
        // Animated Icon
        this.itemIcon = iconRegister.registerIcon(this.getUnlocalizedName().replace("item.", ""));
    }
}
