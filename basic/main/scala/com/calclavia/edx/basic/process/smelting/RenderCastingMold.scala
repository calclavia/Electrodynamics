package com.calclavia.edx.basic.process.smelting

import cpw.mods.fml.relauncher.{Side, SideOnly}
import edx.core.Reference
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.{AdvancedModelLoader, IModelCustom}
import org.lwjgl.opengl.GL11
import resonantengine.lib.render.{RenderItemOverlayUtility, RenderUtility}

@SideOnly(Side.CLIENT) object RenderCastingMold
{
  final val MODEL: IModelCustom = AdvancedModelLoader.loadModel(new ResourceLocation(Reference.domain, Reference.modelPath + "castingMold.tcn"))
  var INSTANCE: RenderCastingMold = new RenderCastingMold
}

@SideOnly(Side.CLIENT) class RenderCastingMold extends TileEntitySpecialRenderer
{
  def renderTileEntityAt(tileEntity: TileEntity, x: Double, y: Double, z: Double, var8: Float)
  {
    if (tileEntity.isInstanceOf[TileCastingMold])
    {
      val tile: TileCastingMold = tileEntity.asInstanceOf[TileCastingMold]
      GL11.glPushMatrix
      GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5)
      GL11.glTranslated(0, -0.25, 0)
      GL11.glScalef(0.5f, 0.5f, 0.5f)
      RenderUtility.bind(Reference.domain, Reference.modelPath + "castingMold.png")
      RenderCastingMold.MODEL.renderAll
      GL11.glPopMatrix
      if (tile.getWorldObj != null)
      {
        RenderItemOverlayUtility.renderItemOnSides(tileEntity, tile.getStackInSlot(0), x, y, z, "")
      }
    }
  }
}