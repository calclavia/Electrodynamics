package resonantinduction.blocks;

import java.util.Random;

import resonantinduction.AssemblyLine;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

import com.builtbroken.minecraft.DarkCore;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockColorGlass extends BlockColored
{

    public BlockColorGlass(String name)
    {
        super(name, AssemblyLine.CONFIGURATION.getBlock(name, DarkCore.getNextID()).getInt(), Material.glass);
        this.setCreativeTab(CreativeTabs.tabDecorations);
        this.setHardness(.5f);
        this.setResistance(.5f);
        this.setStepSound(soundGlassFootstep);
    }

    public BlockColorGlass()
    {
        this("StainedGlass");
    }

    @Override
    public int quantityDropped(Random par1Random)
    {
        return 0;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getRenderBlockPass()
    {
        return 1;
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean renderAsNormalBlock()
    {
        return false;
    }

    @Override
    protected boolean canSilkHarvest()
    {
        return true;
    }
}
