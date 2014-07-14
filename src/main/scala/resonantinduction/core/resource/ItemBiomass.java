package resonantinduction.core.resource;

import net.minecraft.client.renderer.texture.IIconRegister;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import resonant.lib.prefab.item.ItemTooltip;

/** Bio mass item used as a crafting part and fuel
 * 
 * @author Darkguardsman */
public class ItemBiomass extends ItemTooltip
{
    public ItemBiomass(int id)
    {
        super(id);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister iconRegister)
    {
        this.itemIcon = iconRegister.registerIcon(this.getUnlocalizedName().replace("item.", ""));
    }
}
