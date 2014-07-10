package mffs.security

import cpw.mods.fml.client.FMLClientHandler
import cpw.mods.fml.relauncher.{Side, SideOnly}
import mffs.Reference
import net.minecraft.client.Minecraft
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.AdvancedModelLoader
import org.lwjgl.opengl.GL11._
import resonant.lib.render.RenderUtility
import universalelectricity.core.transform.vector.Vector3

@SideOnly(Side.CLIENT)
final object RenderBiometricIdentifier
{
  val textureOn = new ResourceLocation(Reference.domain, Reference.modelPath + "biometricIdentifier_on.png")
  val textureOff = new ResourceLocation(Reference.domain, Reference.modelPath + "biometricIdentifier_off.png")
  val model = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "biometricIdentifier.tcn"))

  def render(tile: TileBiometricIdentifier, x: Double, y: Double, z: Double, frame: Float, isActive: Boolean)
  {
    if (isActive)
    {
      FMLClientHandler.instance.getClient.renderEngine.bindTexture(textureOn)
    }
    else
    {
      FMLClientHandler.instance.getClient.renderEngine.bindTexture(textureOff)
    }

    glPushMatrix
    glTranslated(x + 0.5, y + 0.5, z + 0.5)

    if (tile.world != null)
    {
      glRotatef(-90, 0, 1, 0)
      RenderUtility.rotateBlockBasedOnDirection(tile.getDirection)
    }

    model.renderAllExcept("holoScreen")

    if (tile.world != null)
    {
      /**
       * Simulate flicker and, hovering
       */
      val t = System.currentTimeMillis()

      val look = Minecraft.getMinecraft.thePlayer.rayTrace(8, 1)

      if (look != null && tile.position.toVector3.equals(new Vector3(look).floor))
      {
        if (Math.random() > 0.05 || (tile.lastFlicker - t) > 200)
        {
          glPushMatrix()
          glTranslated(0, Math.sin(Math.toRadians(tile.animation)) * 0.05, 0)
          RenderUtility.enableBlending()
          model.renderOnly("holoScreen")
          RenderUtility.disableBlending()
          glPopMatrix()
          tile.lastFlicker = t
        }
      }
    }

    glPopMatrix
  }
}