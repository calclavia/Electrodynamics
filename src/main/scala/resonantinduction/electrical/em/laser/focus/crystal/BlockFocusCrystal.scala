package resonantinduction.electrical.em.laser.focus.crystal

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.block.material.Material
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import resonantinduction.electrical.em.ElectromagneticCoherence
import resonantinduction.electrical.em.laser.BlockRenderingHandler
import resonantinduction.electrical.em.laser.focus.BlockFocusBase

/**
 * @author Calclavia
 */
class BlockFocusCrystal extends BlockFocusBase(Material.rock)
{
  setBlockName(ElectromagneticCoherence.PREFIX + "focusCrystal")
  setBlockTextureName("glass")
  setCreativeTab(TabEC)

  override def createNewTileEntity(world: World, metadata: Int): TileEntity =
  {
    return new TileFocusCrystal()
  }

  @SideOnly(Side.CLIENT)
  override def getRenderType = BlockRenderingHandler.getRenderId

  override def renderAsNormalBlock = false

  override def isOpaqueCube = false
}

