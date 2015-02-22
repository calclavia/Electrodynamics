package mffs.production

import mffs.Reference

@SideOnly(Side.CLIENT)
final object RenderFortronCapacitor
{
  val textureOn = new ResourceLocation(Reference.domain, Reference.modelPath + "fortronCapacitor_on.png")
  val textureOff = new ResourceLocation(Reference.domain, Reference.modelPath + "fortronCapacitor_off.png")
  val model = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "fortronCapacitor.tcn"))

	def render(tileEntity: BlockFortronCapacitor, x: Double, y: Double, z: Double, frame: Float, isActive: Boolean, isItem: Boolean)
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
    GL11.glTranslated(x + 0.5, y + 0.5 + 0.3 / 2, z + 0.5)
    GL11.glScalef(1.3f, 1.3f, 1.3f)
    model.renderAll
    GL11.glPopMatrix
  }
}