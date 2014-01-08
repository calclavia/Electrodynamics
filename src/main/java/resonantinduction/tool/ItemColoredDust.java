package resonantinduction.tool;

import java.util.List;

import resonantinduction.AssemblyLine;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;

import com.builtbroken.minecraft.DarkCore;
import com.builtbroken.minecraft.helpers.ColorCode;
import com.builtbroken.minecraft.prefab.ItemBasic;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemColoredDust extends ItemBasic
{
    @SideOnly(Side.CLIENT)
    private Icon theIcon;

    public ItemColoredDust()
    {
        super(DarkCore.getNextItemId(), "GlowRefinedSand", ResonantInductionTransport.CONFIGURATION);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
        this.setCreativeTab(CreativeTabs.tabMaterials);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getColorFromItemStack(ItemStack par1ItemStack, int par2)
    {
        return ColorCode.get(par1ItemStack.getItemDamage() % 16).color.getRGB();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean requiresMultipleRenderPasses()
    {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IconRegister par1IconRegister)
    {
        this.itemIcon = par1IconRegister.registerIcon(ResonantInductionTransport.PREFIX + "dust");
        this.theIcon = par1IconRegister.registerIcon("glowingPowder_overlay");
    }

    @Override
    public int getMetadata(int meta)
    {
        return meta;
    }

    @Override
    public final String getUnlocalizedName(ItemStack par1ItemStack)
    {
        return this.getUnlocalizedName() + "." + ColorCode.get(par1ItemStack.getItemDamage()).name;
    }

    @Override
    public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List)
    {
        for (int i = 0; i < ColorCode.values().length - 1; i++)
        {
            par3List.add(new ItemStack(par1, 1, i));
        }
    }
}
