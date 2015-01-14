package edx.quantum.blocks

import java.util.Random

import cpw.mods.fml.relauncher.{Side, SideOnly}
import edx.core.{Reference, Settings}
import net.minecraft.block.material.Material
import net.minecraft.world.World

/**
 * Uranium ore block
 */
class BlockUraniumOre extends BlockRadioactive(Material.rock)
{
  //Constructor
  this.setBlockName(Reference.prefix + "oreUranium")
  this.setStepSound(net.minecraft.block.Block.soundTypeStone)
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

  override def quantityDropped(par1Random: Random): Int =
  {
    return 1
  }
}