package resonantinduction.electrical.laser.focus.mirror

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.block.material.Material
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import resonantinduction.core.ResonantTab
import resonantinduction.electrical.em.ElectromagneticCoherence
import resonantinduction.electrical.laser.BlockRenderingHandler
import resonantinduction.electrical.laser.focus.BlockFocusBase

/**
 * @author Calclavia
 */
class BlockMirror extends BlockFocusBase(Material.rock)
{
  setBlockName(ElectromagneticCoherence.PREFIX + "mirror")
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

