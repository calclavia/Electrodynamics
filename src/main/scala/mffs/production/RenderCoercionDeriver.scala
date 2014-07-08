package mffs.production

import cpw.mods.fml.client.FMLClientHandler
import cpw.mods.fml.relauncher.{Side, SideOnly}
import mffs.Reference
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.AdvancedModelLoader
import org.lwjgl.opengl.GL11

@SideOnly(Side.CLIENT)
final object RenderCoercionDeriver
{
  val textureOn: ResourceLocation = new ResourceLocation(Reference.domain, Reference.modelPath + "coercionDeriver_on.png")
  val textureOff: ResourceLocation = new ResourceLocation(Reference.domain, Reference.modelPath + "coercionDeriver_off.png")
  val model = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "coercionDeriver.tcn"))

  def render(tileEntity: TileCoercionDeriver, x: Double, y: Double, z: Double, frame: Float, isActive:Boolean)
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
    GL11.glScalef(1.3f, 1.3f, 1.3f)
    //model.render(tileEntity.animation, 0.0625F)
    model.renderAll
    GL11.glPopMatrix
  }
}