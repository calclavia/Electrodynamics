package com.calclavia.edx.basic.process.smelting.firebox

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.common.util.ForgeDirection
import resonantengine.lib.render.RenderItemOverlayUtility

@SideOnly(Side.CLIENT) class RenderHotPlate extends TileEntitySpecialRenderer
{
  private final val renderBlocks: RenderBlocks = new RenderBlocks

  def renderTileEntityAt(tileEntity: TileEntity, x: Double, y: Double, z: Double, var8: Float)
  {
    if (tileEntity.isInstanceOf[TileHotPlate])
    {
      val tile: TileHotPlate = tileEntity.asInstanceOf[TileHotPlate]
      RenderItemOverlayUtility.renderTopOverlay(tileEntity, tile.getInventory.getContainedItems, ForgeDirection.EAST, 2, 2, x, y - 0.8, z, 1f)
    }
  }

}