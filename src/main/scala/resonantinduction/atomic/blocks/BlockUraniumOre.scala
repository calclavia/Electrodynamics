package resonantinduction.atomic.blocks

import java.util.Random

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.block.material.Material
import net.minecraft.util.IIcon
import net.minecraft.world.World
import resonantinduction.core.{Reference, ResonantTab, Settings}

/**
 * Uranium ore block
 */
class BlockUraniumOre extends BlockRadioactive(Material.rock)
{
    //Constructor
    this.setBlockName(Reference.prefix + "oreUranium")
    this.setStepSound(net.minecraft.block.Block.soundTypeStone)
    this.setCreativeTab(ResonantTab.tab)
    this.setHardness(2f)
    this.setBlockTextureName(Reference.prefix + "oreUranium")
    this.isRandomlyRadioactive = Settings.allowRadioactiveOres
    this.canWalkPoison = Settings.allowRadioactiveOres
    this.canSpread = false
    this.radius = 1f
    this.amplifier = 0
    this.spawnParticle = true

    @SideOnly(Side.CLIENT) override def randomDisplayTick(world: World, x: Int, y: Int, z: Int, par5Random: Random)
    {
        if (Settings.allowRadioactiveOres)
        {
            super.randomDisplayTick(world, x, y, z, par5Random)
        }
    }

    override def getIcon(side: Int, metadata: Int): IIcon =
    {
        return this.blockIcon
    }

    override def quantityDropped(par1Random: Random): Int =
    {
        return 1
    }
}