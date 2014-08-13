package resonantinduction.electrical.laser.focus.mirror

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.block.material.Material
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import resonantinduction.core.{Reference, ResonantTab}
import resonantinduction.electrical.laser.BlockRenderingHandler
import resonantinduction.electrical.laser.focus.BlockFocusBase

/**
 * @author Calclavia
 */
class BlockMirror extends BlockFocusBase(Material.rock)
{
  setBlockName(Reference.prefix + "mirror")
  setBlockTextureName("stone")
  setCreativeTab(ResonantTab)


  override def createNewTileEntity(world: World, metadata: Int): TileEntity =
  {
    return new TileMirror()
  }

  @SideOnly(Side.CLIENT)
  override def getRenderType = BlockRenderingHandler.getRenderId

  override def renderAsNormalBlock = false

  override def isOpaqueCube = false
}

