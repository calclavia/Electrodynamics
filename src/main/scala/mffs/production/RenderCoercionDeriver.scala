package mffs.production

import mffs.Reference

@SideOnly(Side.CLIENT)
final object RenderCoercionDeriver
{
  val textureOn: ResourceLocation = new ResourceLocation(Reference.domain, Reference.modelPath + "coercionDeriver_on.png")
  val textureOff: ResourceLocation = new ResourceLocation(Reference.domain, Reference.modelPath + "coercionDeriver_off.png")
  val model = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "coercionDeriver.tcn"))

	def render(tileEntity: BlockCoercionDeriver, x: Double, y: Double, z: Double, frame: Float, isActive: Boolean, isItem: Boolean)
  {
    if (isActive)
    {
      FMLClientHandler.instance.getClient.renderEngine.bindTexture(textureOn)
    }
    else
    {
      FMLClientHandler.instance.getClient.renderEngine.bindTexture(textureOff)
    }

    glPushMatrix()
    glTranslated(x + 0.5, y + 0.5, z + 0.5)
    model.renderAllExcept("crystal")

    glPushMatrix()
    glTranslated(0, (0.3 + Math.sin(Math.toRadians(tileEntity.animation)) * 0.08) * tileEntity.animationTween - 0.1, 0)
    glRotated(tileEntity.animation, 0, 1, 0)
    RenderUtility.enableBlending()
    model.renderOnly("crystal")
    RenderUtility.disableBlending()
    glPopMatrix()

    glPopMatrix()
  }
}