package resonantinduction.electrical.laser.focus.crystal

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.block.material.Material
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import resonantinduction.core.Reference
import resonantinduction.electrical.laser.BlockRenderingHandler
import resonantinduction.electrical.laser.focus.BlockFocusBase

/**
 * @author Calclavia
 */
class BlockFocusCrystal extends BlockFocusBase(Material.rock)
{
  setBlockName(Reference.prefix + "focusCrystal")
  setBlockTextureName("glass")

  override def createNewTileEntity(world: World, metadata: Int): TileEntity =
  {
    return new TileFocusCrystal()
  }

  @SideOnly(Side.CLIENT)
  override def getRenderType = BlockRenderingHandler.getRenderId

  override def renderAsNormalBlock = false

  override def isOpaqueCube = false
}

