package mffs.production

import cpw.mods.fml.client.FMLClientHandler
import cpw.mods.fml.relauncher.{Side, SideOnly}
import mffs.Reference
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.AdvancedModelLoader
import org.lwjgl.opengl.GL11

@SideOnly(Side.CLIENT)
final object RenderFortronCapacitor
{
  val textureOn = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_DIRECTORY + "fortronCapacitor_on.png")
  val textureOff = new ResourceLocation(Reference.DOMAIN, Reference.MODEL_DIRECTORY + "fortronCapacitor_off.png")
  val model = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.DOMAIN, Reference.MODEL_DIRECTORY + "fortronCapacitor.tcn"))

  def render(tileEntity: TileFortronCapacitor, x: Double, y: Double, z: Double, frame: Float)
  {
    if (tileEntity.isActive)
    {
      FMLClientHandler.instance.getClient.renderEngine.bindTexture(textureOn)
    }
    else
    {
      FMLClientHandler.instance.getClient.renderEngine.bindTexture(textureOff)
    }

    GL11.glPushMatrix
    GL11.glTranslated(x + 0.5, y + 1.95, z + 0.5)
    GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F)
    GL11.glScalef(1.3f, 1.3f, 1.3f)
    model.renderAll
    GL11.glPopMatrix
  }
}