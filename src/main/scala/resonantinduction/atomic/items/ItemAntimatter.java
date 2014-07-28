package resonantinduction.atomic.items;

import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/* Antimatter Cell */
public class ItemAntimatter extends ItemCell
{
    private IIcon iconGram;

    public ItemAntimatter()
    {
        super();
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister iconRegister)
    {
        // Animated Icons
        //ResonantInduction.LOGGER.info(this.getUnlocalizedName().replace("item.", "") + "_milligram");
        this.itemIcon = iconRegister.registerIcon(this.getUnlocalizedName().replace("item.", "") + "_milligram");
        this.iconGram = iconRegister.registerIcon(this.getUnlocalizedName().replace("item.", "") + "_gram");
    }

    @Override
    public IIcon getIconFromDamage(int metadata)
    {
        if (metadata >= 1)
        {
            return this.iconGram;
        }
        else
        {
            return this.itemIcon;
        }
    }

    @Override
    public void getSubItems(Item item, CreativeTabs par2CreativeTabs, List par3List)
    {
        par3List.add(new ItemStack(item, 1, 0));
        par3List.add(new ItemStack(item, 1, 1));
    }

    @Override
    public int getEntityLifespan(ItemStack itemStack, World world)
    {
        return 160;
    }
}
