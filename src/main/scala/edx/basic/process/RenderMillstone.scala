package edx.basic.process

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.tileentity.TileEntity
import resonant.lib.render.RenderItemOverlayUtility

@SideOnly(Side.CLIENT)
class RenderMillstone extends TileEntitySpecialRenderer
{
  private final val renderBlocks: RenderBlocks = new RenderBlocks

  def renderTileEntityAt(tileEntity: TileEntity, x: Double, y: Double, z: Double, var8: Float)
  {
    if (tileEntity.isInstanceOf[TileSieve])
    {
      val tile: TileSieve = tileEntity.asInstanceOf[TileSieve]
      RenderItemOverlayUtility.renderItemOnSides(tileEntity, tile.getStackInSlot(0), x, y, z, "")
    }
  }

}