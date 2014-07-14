package resonantinduction.electrical.em.laser.emitter

import net.minecraft.block.{BlockPistonBase, BlockContainer}
import net.minecraft.block.material.Material
import net.minecraft.world.{IBlockAccess, World}
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.tileentity.TileEntity
import resonantinduction.electrical.em.{TabEC, ElectromagneticCoherence}
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemStack
import resonantinduction.electrical.em.laser.{Laser, BlockRenderingHandler}
import cpw.mods.fml.relauncher.{Side, SideOnly}

/**
 * @author Calclavia
 */
class BlockLaserEmitter extends BlockContainer(Material.rock)
{
  setBlockName(ElectromagneticCoherence.PREFIX + "laserEmitter")
  setBlockTextureName("stone")
  setCreativeTab(TabEC)

  /**
   * Called when the block is placed in the world.
   */
  override def onBlockPlacedBy(world: World, x: Int, y: Int, z: Int, entity: EntityLivingBase, itemStack: ItemStack)
  {
    val l = BlockPistonBase.determineOrientation(world, x, y, z, entity)
    world.setBlockMetadataWithNotify(x, y, z, l, 2)
  }

  override def getMixedBrightnessForBlock(access: IBlockAccess, x: Int, y: Int, z: Int): Int =
  {
    return ((access.getTileEntity(x, y, z).asInstanceOf[TileLaserEmitter].energy / Laser.maxEnergy) * 15).toInt
  }

  override def createNewTileEntity(world: World, metadata: Int): TileEntity =
  {
    return new TileLaserEmitter()
  }

  @SideOnly(Side.CLIENT)
  override def getRenderType = BlockRenderingHandler.getRenderId

  override def renderAsNormalBlock = false

  override def isOpaqueCube = false
}
