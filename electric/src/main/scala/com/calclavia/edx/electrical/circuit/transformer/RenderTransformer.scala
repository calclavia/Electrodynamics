package com.calclavia.edx.electrical.circuit.transformer

import cpw.mods.fml.client.FMLClientHandler
import cpw.mods.fml.relauncher.{Side, SideOnly}
import edx.core.Reference
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.IItemRenderer
import net.minecraftforge.client.model.AdvancedModelLoader
import net.minecraftforge.client.model.obj.WavefrontObject
import org.lwjgl.opengl.GL11
import resonantengine.api.item.ISimpleItemRenderer
import resonantengine.lib.render.RenderUtility

@SideOnly(Side.CLIENT)
object RenderTransformer extends ISimpleItemRenderer
{
  val model: WavefrontObject = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "transformer.obj")).asInstanceOf[WavefrontObject]
  val textureCoil: ResourceLocation = new ResourceLocation(Reference.domain, Reference.modelPath + "transformer_winding.png")
  val textureStone: ResourceLocation = new ResourceLocation(Reference.blockTextureDirectory + "stone.png")
  val textureIron: ResourceLocation = new ResourceLocation(Reference.blockTextureDirectory + "iron_block.png")

  def renderInventoryItem(`type`: IItemRenderer.ItemRenderType, itemStack: ItemStack, data: AnyRef*)
  {
    GL11.glTranslated(0, -0.2f, 0)
    doRender
  }

  def doRender
  {
    GL11.glScalef(0.5f, 0.5f, 0.5f)
    FMLClientHandler.instance.getClient.renderEngine.bindTexture(RenderTransformer.textureCoil)
    RenderTransformer.model.renderAllExcept("core", "base")
    FMLClientHandler.instance.getClient.renderEngine.bindTexture(RenderTransformer.textureIron)
    RenderTransformer.model.renderOnly("core")
    FMLClientHandler.instance.getClient.renderEngine.bindTexture(RenderTransformer.textureStone)
    RenderTransformer.model.renderOnly("base")
  }

  def render(part: PartElectricTransformer, x: Double, y: Double, z: Double)
  {
    GL11.glPushMatrix
    GL11.glTranslatef(x.asInstanceOf[Float] + 0.5F, y.asInstanceOf[Float] + 0.5F, z.asInstanceOf[Float] + 0.5F)
    RenderUtility.rotateFaceBlockToSide(part.placementSide)
    RenderUtility.rotateBlockBasedOnDirection(part.getFacing)
    GL11.glRotatef(90, 0, 1, 0)
    GL11.glScalef(0.5f, 0.5f, 0.5f)
    FMLClientHandler.instance.getClient.renderEngine.bindTexture(RenderTransformer.textureCoil)
    if (part.multiplier == 0)
    {
      RenderTransformer.model.renderOnly("InsulatorLayerLow", "OuterWindingLowBox", "InnerWindingLowBox")
    }
    else if (part.multiplier == 1)
    {
      RenderTransformer.model.renderOnly("InsulatorLayerMed", "OuterWindingMedBox", "InnerWindingMedBox")
    }
    else if (part.multiplier == 2)
    {
      RenderTransformer.model.renderOnly("InnerWindingHighBox", "InsulatorLayerHigh", "OuterWindingHighBox")
    }
    RenderTransformer.model.renderOnly("OuterWindingConnector", "InnerWindingConnector")
    FMLClientHandler.instance.getClient.renderEngine.bindTexture(RenderTransformer.textureIron)
    RenderTransformer.model.renderOnly("core")
    FMLClientHandler.instance.getClient.renderEngine.bindTexture(RenderTransformer.textureStone)
    RenderTransformer.model.renderOnly("base")
    GL11.glPopMatrix
  }
}