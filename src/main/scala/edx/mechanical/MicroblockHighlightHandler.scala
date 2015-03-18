package edx.mechanical

import codechicken.lib.render.RenderUtils
import codechicken.lib.vec.Vector3
import codechicken.microblock.{CornerPlacementGrid, FacePlacementGrid}
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.relauncher.{Side, SideOnly}
import edx.core.prefab.part.IHighlight
import net.minecraft.util.MovingObjectPosition
import net.minecraftforge.client.event.DrawBlockHighlightEvent
import org.lwjgl.opengl.GL11

object MicroblockHighlightHandler
{
  @SubscribeEvent
  @SideOnly(Side.CLIENT)
  def drawBlockHighlight(event: DrawBlockHighlightEvent)
  {
    if (event.currentItem != null && event.currentItem.getItem.isInstanceOf[IHighlight] && event.target != null && event.target.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
    {
      GL11.glPushMatrix()
      RenderUtils.translateToWorldCoords(event.player, event.partialTicks)
      val hit: Vector3 = new Vector3(event.target.hitVec)
      val t = event.currentItem.getItem.asInstanceOf[IHighlight].getHighlightType
      if (t == 0)
      {
        FacePlacementGrid.render(hit, event.target.sideHit)
      }
      if (t == 1)
      {
        CornerPlacementGrid.render(hit, event.target.sideHit)
      }
      event.setCanceled(true)
      GL11.glPopMatrix()
    }
  }
}