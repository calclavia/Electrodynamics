package resonantinduction.mechanical

import cpw.mods.fml.common.Mod
import net.minecraft.util.MovingObjectPosition
import net.minecraftforge.client.event.DrawBlockHighlightEvent
import org.lwjgl.opengl.GL11
import resonantinduction.core.prefab.part.IHighlight
import codechicken.lib.render.RenderUtils
import codechicken.lib.vec.Vector3
import codechicken.microblock.CornerPlacementGrid
import codechicken.microblock.FacePlacementGrid
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly

class MicroblockHighlightHandler {
  @Mod.EventHandler
  @SideOnly(Side.CLIENT)
  def drawBlockHighlight(event: DrawBlockHighlightEvent)
  {
    if (event.currentItem != null && (event.currentItem.getItem.isInstanceOf[IHighlight]) && event.target != null && event.target.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
    {
      GL11.glPushMatrix
      RenderUtils.translateToWorldCoords(event.player, event.partialTicks)
      val hit: Vector3 = new Vector3(event.target.hitVec)
      val t = event.currentItem.getItem.asInstanceOf[IHighlight].getHighlightType
      if(t == 0)
      {
          FacePlacementGrid.render(hit, event.target.sideHit)
      }
      if(t == 1)
      {
          CornerPlacementGrid.render(hit, event.target.sideHit)
      }
      event.setCanceled(true)
      GL11.glPopMatrix
    }
  }
}