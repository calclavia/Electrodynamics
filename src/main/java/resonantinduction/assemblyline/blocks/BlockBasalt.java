package resonantinduction.assemblyline.blocks;

import java.util.Random;

import resonantinduction.assemblyline.AssemblyLine;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.Icon;

import com.builtbroken.minecraft.DarkCore;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockBasalt extends Block
{
    private Icon[] icons;
    public final static String[] blockNames = new String[] { "basalt", "basaltcobble", "basaltbrick", "basaltchiseled", "basaltcracked", "basaltmossy" };

    public enum block
    {
        STONE("basalt"),
        COBBLE("basaltcobble"),
        BRICK("basaltbrick"),
        CHISILED("basaltchiseled"),
        CRACKED("basaltcracked"),
        MOSSY("basaltmossy");
        public String name;

        private block(String name)
        {
            this.name = name;
        }

    }

    public BlockBasalt()
    {
        super(AssemblyLine.CONFIGURATION.getBlock("basalt", DarkCore.getNextID()).getInt(), Material.rock);
        this.setCreativeTab(CreativeTabs.tabDecorations);
        this.setHardness(2f);
        this.setResistance(2f);
        this.setUnlocalizedName("basalt");
        this.setStepSound(soundStoneFootstep);
        this.setLightValue(1);
    }

    @Override
    public int quantityDropped(Random par1Random)
    {
        return 1;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean renderAsNormalBlock()
    {
        return true;
    }

    @Override
    protected boolean canSilkHarvest()
    {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Icon getIcon(int side, int meta)
    {
        if (meta < this.icons.length)
        {
            return this.icons[meta];
        }
        return blockIcon;

    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IconRegister iconReg)
    {
        this.icons = new Icon[16];

        for (int i = 0; i < BlockBasalt.blockNames.length; ++i)
        {
            this.icons[i] = iconReg.registerIcon(AssemblyLine.PREFIX + BlockBasalt.blockNames[i]);
        }
    }

    @Override
    public int damageDropped(int par1)
    {
        return par1;
    }
}
