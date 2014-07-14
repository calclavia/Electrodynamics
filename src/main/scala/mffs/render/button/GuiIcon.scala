package mffs.render.button

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.{FontRenderer, GuiButton}
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.entity.RenderItem
import net.minecraft.item.ItemStack
import org.lwjgl.opengl.GL11

class GuiIcon(id: Int, xPos: Int, yPos: Int, itemStacks: ItemStack*) extends GuiButton(id, xPos, yPos, 20, 20, "")
{
  private val itemRenderer = new RenderItem
  private var index = 0

  /**
   * Changes the index of the icon.
   * @param i - The index of hte icon
   * @return True if the index of the icon is changed.
   */
  def setIndex(i: Int): Boolean =
  {
    if (i >= 0 && i < itemStacks.length)
    {
      if (index != i)
      {
        index = i
        return true
      }
    }

    return false
  }

  override def drawButton(par1Minecraft: Minecraft, par2: Int, par3: Int)
  {
    super.drawButton(par1Minecraft, par2, par3)

    if (visible && itemStacks(index) != null)
    {
      drawItemStack(itemStacks(index), xPosition, yPosition)
    }
  }

  protected def drawItemStack(itemStack: ItemStack, x: Int, y: Int)
  {
    val renderX = x + 2
    val renderY = y + 1
    val mc: Minecraft = Minecraft.getMinecraft
    val fontRenderer: FontRenderer = mc.fontRenderer
    RenderHelper.enableGUIStandardItemLighting()
    GL11.glTranslatef(0.0F, 0.0F, 32.0F)
    zLevel = 500.0F
    itemRenderer.zLevel = 500.0F
    itemRenderer.renderItemAndEffectIntoGUI(fontRenderer, mc.renderEngine, itemStack, renderX, renderY)
    itemRenderer.renderItemOverlayIntoGUI(fontRenderer, mc.renderEngine, itemStack, renderX, renderY)
    zLevel = 0.0F
    itemRenderer.zLevel = 0.0F
    RenderHelper.disableStandardItemLighting
  }
}