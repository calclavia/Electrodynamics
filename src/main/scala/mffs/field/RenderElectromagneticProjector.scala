package mffs.field

import cpw.mods.fml.client.FMLClientHandler
import cpw.mods.fml.relauncher.{Side, SideOnly}
import mffs.{Reference, Settings}
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.{RenderHelper, Tessellator}
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.AdvancedModelLoader
import org.lwjgl.opengl.GL11._
import resonant.lib.render.RenderUtility

@SideOnly(Side.CLIENT)
final object RenderElectromagneticProjector
{
  val textureOn = new ResourceLocation(Reference.domain, Reference.modelPath + "electromagneticProjector_on.png")
  val textureOff = new ResourceLocation(Reference.domain, Reference.modelPath + "electromagneticProjector_off.png")

  val model = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "electromagneticProjector.tcn"))

  def render(tileEntity: TileElectromagneticProjector, x: Double, y: Double, z: Double, frame: Float, isActive: Boolean)
  {
    glPushMatrix
    glTranslated(x + 0.5, y + 0.5, z + 0.5)

    if (isActive)
    {
      FMLClientHandler.instance.getClient.renderEngine.bindTexture(textureOn)
    }
    else
    {
      FMLClientHandler.instance.getClient.renderEngine.bindTexture(textureOff)
    }

    model.renderAll
    //.render(tileEntity.animation, 0.0625F)
    glPopMatrix

    if (tileEntity.getMode != null)
    {
      val tessellator: Tessellator = Tessellator.instance
      RenderHelper.disableStandardItemLighting
      glPushMatrix
      glTranslated(x + 0.5, y + 0.5, z + 0.5)
      val xDifference: Double = Minecraft.getMinecraft.thePlayer.posX - (tileEntity.xCoord + 0.5)
      val zDifference: Double = Minecraft.getMinecraft.thePlayer.posZ - (tileEntity.zCoord + 0.5)
      val rotatation: Float = Math.toDegrees(Math.atan2(zDifference, xDifference)).asInstanceOf[Float]
      glRotatef(-rotatation + 27, 0.0F, 1.0F, 0.0F)
      glDisable(GL_TEXTURE_2D)
      glShadeModel(GL_SMOOTH)
      glEnable(GL_BLEND)
      glBlendFunc(GL_SRC_ALPHA, GL_ONE)
      glDisable(GL_ALPHA_TEST)
      glEnable(GL_CULL_FACE)
      glDepthMask(false)
      glPushMatrix
      tessellator.startDrawing(6)
      val height: Float = 2
      val width: Float = 2
      tessellator.setColorRGBA(72, 198, 255, 255)
      tessellator.addVertex(0.0D, 0.0D, 0.0D)
      tessellator.setColorRGBA_I(0, 0)
      tessellator.addVertex(-0.866D * width, height, -0.5F * width)
      tessellator.addVertex(0.866D * width, height, -0.5F * width)
      tessellator.addVertex(0.0D, height, 1.0F * width)
      tessellator.addVertex(-0.866D * width, height, -0.5F * width)
      tessellator.draw
      glPopMatrix
      glDepthMask(true)
      glDisable(GL_CULL_FACE)
      glDisable(GL_BLEND)
      glShadeModel(GL_FLAT)
      glColor4f(1.0F, 1.0F, 1.0F, 1.0F)
      glEnable(GL_TEXTURE_2D)
      glEnable(GL_ALPHA_TEST)
      RenderHelper.enableStandardItemLighting
      glPopMatrix

      if (Settings.highGraphics)
      {
        glPushMatrix
        glTranslated(x + 0.5, y + 1.35, z + 0.5)
        FMLClientHandler.instance.getClient.renderEngine.bindTexture(Reference.hologramTexture)
        RenderUtility.enableBlending
        RenderUtility.disableLighting
        glPushMatrix
        glColor4f(1, 1, 1, Math.sin(tileEntity.getTicks.asInstanceOf[Double] / 10).asInstanceOf[Float] / 2 + 1)
        glTranslatef(0, Math.sin(Math.toRadians(tileEntity.getTicks * 3)).asInstanceOf[Float] / 7, 0)
        glRotatef(tileEntity.getTicks * 4, 0, 1, 0)
        glRotatef(36f + tileEntity.getTicks * 4, 0, 1, 1)
        tileEntity.getMode.render(tileEntity, x, y, z, frame, tileEntity.getTicks)
        glPopMatrix
        RenderUtility.enableLighting
        RenderUtility.disableBlending
        glPopMatrix
      }
    }

  }
}