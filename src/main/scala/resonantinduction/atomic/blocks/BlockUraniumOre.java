package resonantinduction.atomic.blocks;

import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import resonantinduction.core.Reference;
import resonantinduction.core.ResonantTab;
import resonantinduction.core.Settings;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Uranium ore block */
public class BlockUraniumOre extends BlockRadioactive
{
    public BlockUraniumOre()
    {
        super(Material.rock);
        this.setBlockName(Reference.prefix() + "oreUranium");
        this.setStepSound(soundTypeStone);
        this.setCreativeTab(ResonantTab.tab());
        this.setHardness(2f);
        this.setBlockTextureName(Reference.prefix() + "oreUranium");

        this.isRandomlyRadioactive = Settings.allowRadioactiveOres();
        this.canWalkPoison = Settings.allowRadioactiveOres();
        this.canSpread = false;
        this.radius = 1f;
        this.amplifier = 0;
        this.spawnParticle = true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(World world, int x, int y, int z, Random par5Random)
    {
        if (Settings.allowRadioactiveOres())
        {
            super.randomDisplayTick(world, x, y, z, par5Random);
        }
    }

    @Override
    public IIcon getIcon(int side, int metadata)
    {
        return this.blockIcon;
    }

    @Override
    public int quantityDropped(Random par1Random)
    {
        return 1;
    }
}
