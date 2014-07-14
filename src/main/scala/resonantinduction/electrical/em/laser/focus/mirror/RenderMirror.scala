package resonantinduction.electrical.em.laser.focus.mirror

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraftforge.client.model.AdvancedModelLoader
import net.minecraft.util.ResourceLocation
import resonantinduction.electrical.em.ElectromagneticCoherence
import net.minecraft.tileentity.TileEntity
import org.lwjgl.opengl.GL11._
import cpw.mods.fml.client.FMLClientHandler
import org.lwjgl.opengl.GL11
import cpw.mods.fml.relauncher.{Side, SideOnly}

/**
 * @author Calclavia
 */
@SideOnly(Side.CLIENT)
object RenderMirror extends TileEntitySpecialRenderer
{
  val model = AdvancedModelLoader.loadModel(new ResourceLocation(ElectromagneticCoherence.DOMAIN, ElectromagneticCoherence.MODEL_PATH_NAME + "mirror.tcn"))
  val texture = new ResourceLocation(ElectromagneticCoherence.DOMAIN, ElectromagneticCoherence.MODEL_PATH_NAME + "mirror.png")

  def renderTileEntityAt(tileEntity: TileEntity, x: Double, y: Double, z: Double, f: Float)
  {
    glPushMatrix()
    glTranslated(x + 0.5, y + 0.5, z + 0.5)

    FMLClientHandler.instance.getClient.renderEngine.bindTexture(texture)

    val tile = tileEntity.asInstanceOf[TileMirror]

    val angle = tile.normal.eulerAngles
    glRotated(angle.x, 0, 1, 0)
    glRotated(angle.y, 1, 0, 0)
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
