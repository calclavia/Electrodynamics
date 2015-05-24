package com.calclavia.edx.quantum.machine.plasma

import cpw.mods.fml.relauncher.{Side, SideOnly}
import edx.core.Reference
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.{AdvancedModelLoader, IModelCustom}
import org.lwjgl.opengl.GL11
import resonantengine.lib.render.RenderUtility

@SideOnly(Side.CLIENT)
object RenderPlasmaHeater
{
  final val MODEL: IModelCustom = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "fusionReactor.tcn"))
  final val TEXTURE: ResourceLocation = new ResourceLocation(Reference.domain, Reference.modelPath + "fusionReactor.png")
}

@SideOnly(Side.CLIENT)
class RenderPlasmaHeater
{
  def renderTileEntityAt(t: TileEntity, x: Double, y: Double, z: Double, f: Float)
  {
    val tileEntity: TilePlasmaHeater = t.asInstanceOf[TilePlasmaHeater]

    GL11.glPushMatrix
    GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5)
    RenderUtility.bind(RenderPlasmaHeater.TEXTURE)
    GL11.glPushMatrix
    GL11.glRotated(Math.toDegrees(tileEntity.rotation), 0, 1, 0)
    RenderPlasmaHeater.MODEL.renderOnly(Array("rrot", "srot"): _*)
    GL11.glPopMatrix
    RenderPlasmaHeater.MODEL.renderAllExcept(Array("rrot", "srot"): _*)
    GL11.glPopMatrix
  }
}