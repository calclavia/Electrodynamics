package mffs.field.mobilize

import mffs.Reference

@SideOnly(Side.CLIENT)
final object RenderForceMobilizer
{
  val textureOn: ResourceLocation = new ResourceLocation(Reference.domain, Reference.modelPath + "forceMobilizer_on.png")
  val textureOff: ResourceLocation = new ResourceLocation(Reference.domain, Reference.modelPath + "forceMobilizer_off.png")
  val model = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "forceMobilizer.tcn"))

	def render(tileEntity: BlockMobilizer, x: Double, y: Double, z: Double, frame: Float, isActive: Boolean, isItem: Boolean)
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

    if (!isItem)
    {
      glRotatef(-90, 0, 1, 0)
      RenderUtility.rotateBlockBasedOnDirection(tileEntity.getDirection)
    }

    model.renderAll
    glPopMatrix
  }
}