package mffs.mobilize

import cpw.mods.fml.client.FMLClientHandler
import cpw.mods.fml.relauncher.{Side, SideOnly}
import mffs.Reference
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.AdvancedModelLoader
import net.minecraftforge.common.util.ForgeDirection
import org.lwjgl.opengl.GL11
import resonant.lib.render.RenderUtility

@SideOnly(Side.CLIENT)
final object RenderForceMobilizer
{
  val textureOn: ResourceLocation = new ResourceLocation(Reference.domain, Reference.modelPath + "forceMobilizer_on.png")
  val textureOff: ResourceLocation = new ResourceLocation(Reference.domain, Reference.modelPath + "forceMobilizer_off.png")
  val model = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "forceMobilizer.tcn"))

  def render(tileEntity: TileForceMobilizer, x: Double, y: Double, z: Double, frame: Float, isActive: Boolean)
  {
    if (isActive)
    {
      FMLClientHandler.instance.getClient.renderEngine.bindTexture(textureOn)
    }
    else
    {
      FMLClientHandler.instance.getClient.renderEngine.bindTexture(textureOff)
    }

    GL11.glPushMatrix
    GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5)

    if (tileEntity.world != null)
    {
      RenderUtility.rotateBlockBasedOnDirection(tileEntity.getDirection)
    }

    model.renderAll
    GL11.glPopMatrix
  }
}