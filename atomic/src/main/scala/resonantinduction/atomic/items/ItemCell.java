package resonantinduction.atomic.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import resonant.api.IReactor;
import resonant.lib.prefab.item.ItemTooltip;
import resonant.lib.utility.LanguageUtility;
import resonantinduction.atomic.Atomic;

public class ItemCell extends ItemTooltip
{
    public ItemCell(int itemID)
    {
        super(itemID);
        setContainerItem(Atomic.itemCell);
    }

    @Override
    public boolean shouldPassSneakingClickToBlock(World world, int x, int y, int z)
    {
        return world.getBlockTileEntity(x, y, z) instanceof IReactor;
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IconRegister iconRegister)
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
