package resonantinduction.electrical.laser.focus.mirror

import cpw.mods.fml.client.FMLClientHandler
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.AdvancedModelLoader
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11._
import resonantinduction.core.Reference

/**
 * @author Calclavia
 */
@SideOnly(Side.CLIENT)
object RenderMirror extends TileEntitySpecialRenderer
{
  val model = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "mirror.tcn"))
  val texture = new ResourceLocation(Reference.domain, Reference.modelPath + "mirror.png")

  def renderTileEntityAt(tileEntity: TileEntity, x: Double, y: Double, z: Double, f: Float)
  {
    glPushMatrix()
    glTranslated(x + 0.5, y + 0.5, z + 0.5)

    FMLClientHandler.instance.getClient.renderEngine.bindTexture(texture)

    val tile = tileEntity.asInstanceOf[TileMirror]

    val angle = tile.normal.toEulerAngle
    glRotated(angle.yaw, 0, 1, 0)
    glRotated(angle.pitch, 1, 0, 0)
    glRotated(90, 1, 0, 0)
    model.renderOnly("mirror", "mirrorBacking", "standConnector")

    GL11.glPopMatrix()
  }

  def renderItem()
  {
    glPushMatrix()

    FMLClientHandler.instance.getClient.renderEngine.bindTexture(texture)
    model.renderAll()

    GL11.glPopMatrix()
  }
}
