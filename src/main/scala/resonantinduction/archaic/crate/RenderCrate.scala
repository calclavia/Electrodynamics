package resonantinduction.archaic.crate

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.tileentity.TileEntity
import org.lwjgl.opengl.GL11
import resonant.lib.render.RenderItemOverlayUtility
import resonant.lib.utility.LanguageUtility
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT) class RenderCrate extends TileEntitySpecialRenderer {
  def renderTileEntityAt(tileEntity: TileEntity, x: Double, y: Double, z: Double, var8: Float)
  {
    if (tileEntity.isInstanceOf[TileCrate])
    {
      GL11.glPushMatrix
      val tile: TileCrate = tileEntity.asInstanceOf[TileCrate]
      RenderItemOverlayUtility.renderItemOnSides(tileEntity, tile.getSampleStack, x, y, z, LanguageUtility.getLocal("tooltip.empty"))
      GL11.glPopMatrix
    }
  }
}