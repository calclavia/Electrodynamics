package com.calclavia.edx.electrical.circuit.component.tesla

import cpw.mods.fml.relauncher.{Side, SideOnly}
import edx.core.Reference
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.{AdvancedModelLoader, IModelCustom}
import org.lwjgl.opengl.GL11

/**
 * @author Calclavia
 *
 */
@SideOnly(Side.CLIENT) object RenderTesla
{
  final val TEXTURE_BOTTOM: ResourceLocation = new ResourceLocation(Reference.domain, Reference.modelPath + "tesla_bottom.png")
  final val TEXTURE_MIDDLE: ResourceLocation = new ResourceLocation(Reference.domain, Reference.modelPath + "tesla_middle.png")
  final val TEXTURE_TOP: ResourceLocation = new ResourceLocation(Reference.domain, Reference.modelPath + "tesla_top.png")
  final val MODEL_BOTTOM: IModelCustom = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "teslaBottom.tcn"))
  final val MODEL_MIDDLE: IModelCustom = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "teslaMiddle.tcn"))
  final val MODEL_TOP: IModelCustom = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "teslaTop.tcn"))
}

@SideOnly(Side.CLIENT)
class RenderTesla extends TileEntitySpecialRenderer
{
  def renderTileEntityAt(t: TileEntity, x: Double, y: Double, z: Double, f: Float)
  {
    GL11.glPushMatrix
    GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5)
    val meta: Int = if (t.getBlockType != null) t.getBlockMetadata else 0

    if (meta == 1)
    {
      bindTexture(RenderTesla.TEXTURE_MIDDLE)
      RenderTesla.MODEL_MIDDLE.renderAll
    }
    else if (meta == 2)
    {
      bindTexture(RenderTesla.TEXTURE_TOP)
      RenderTesla.MODEL_TOP.renderAll
    }
    else
    {
      bindTexture(RenderTesla.TEXTURE_BOTTOM)
      RenderTesla.MODEL_BOTTOM.renderAll
    }
    GL11.glPopMatrix
  }
}