package com.calclavia.edx.quantum.machine.boiler

import cpw.mods.fml.relauncher.{Side, SideOnly}
import edx.core.Reference
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.{AdvancedModelLoader, IModelCustom}
import org.lwjgl.opengl.GL11
import resonantengine.lib.render.RenderUtility
import resonantengine.lib.render.model.FixedTechneModel

@SideOnly(Side.CLIENT) object RenderNuclearBoiler
{
  final val MODEL: IModelCustom = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "nuclearBoiler.tcn"))
  final val TEXTURE: ResourceLocation = new ResourceLocation(Reference.domain, Reference.modelPath + "nuclearBoiler.png")
}

@SideOnly(Side.CLIENT) class RenderNuclearBoiler extends TileEntitySpecialRenderer
{
  def renderTileEntityAt(tileEntity: TileEntity, var2: Double, var4: Double, var6: Double, var8: Float)
  {
    this.renderAModelAt(tileEntity.asInstanceOf[TileNuclearBoiler], var2, var4, var6, var8)
  }

  def renderAModelAt(tileEntity: TileNuclearBoiler, x: Double, y: Double, z: Double, f: Float)
  {
    GL11.glPushMatrix
    GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5)
    GL11.glRotatef(90, 0, 1, 0)
    if (tileEntity.world != null)
    {
      RenderUtility.rotateBlockBasedOnDirection(tileEntity.getDirection)
    }
    bindTexture(RenderNuclearBoiler.TEXTURE)
    if (RenderNuclearBoiler.MODEL.isInstanceOf[FixedTechneModel])
    {
      (RenderNuclearBoiler.MODEL.asInstanceOf[FixedTechneModel]).renderOnlyAroundPivot(Math.toDegrees(tileEntity.rotation), 0, 1, 0, "FUEL BAR SUPPORT 1 ROTATES", "FUEL BAR 1 ROTATES")
      (RenderNuclearBoiler.MODEL.asInstanceOf[FixedTechneModel]).renderOnlyAroundPivot(-Math.toDegrees(tileEntity.rotation), 0, 1, 0, "FUEL BAR SUPPORT 2 ROTATES", "FUEL BAR 2 ROTATES")
      RenderNuclearBoiler.MODEL.renderAllExcept("FUEL BAR SUPPORT 1 ROTATES", "FUEL BAR SUPPORT 2 ROTATES", "FUEL BAR 1 ROTATES", "FUEL BAR 2 ROTATES")
    }
    else
    {
      RenderNuclearBoiler.MODEL.renderAll
    }
    GL11.glPopMatrix
  }
}