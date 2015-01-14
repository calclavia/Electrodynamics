package edx.quantum.machine.thermometer

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.tileentity.TileEntity
import org.lwjgl.opengl.GL11
import resonant.lib.render.RenderUtility

@SideOnly(Side.CLIENT) class RenderThermometer extends TileEntitySpecialRenderer
{
  def renderTileEntityAt(tileEntity: TileEntity, x: Double, y: Double, z: Double, var8: Float)
  {
    val tile: TileThermometer = tileEntity.asInstanceOf[TileThermometer]
    GL11.glPushMatrix
    RenderUtility.enableLightmap

    for (side <- 2 to 6)
    {
      RenderUtility.renderText((if (tile.isOverThreshold) "\u00a74" else "") + Math.round(tile.detectedTemperature) + " K", side, 0.8f, x, y + 0.1, z)
      RenderUtility.renderText((if (tile.isOverThreshold) "\u00a74" else "\u00a71") + "Threshold: " + (tile.getThershold) + " K", side, 1, x, y - 0.1, z)
      if (tile.trackCoordinate != null)
      {
        RenderUtility.renderText(tile.trackCoordinate.xi + ", " + tile.trackCoordinate.yi + ", " + tile.trackCoordinate.zi, side, 0.5f, x, y - 0.3, z)
      }
    }
    GL11.glPopMatrix
  }
}