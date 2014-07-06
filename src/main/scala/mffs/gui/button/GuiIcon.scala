package mffs.gui.button

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.{FontRenderer, GuiButton}
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.entity.RenderItem
import net.minecraft.init.Blocks
import net.minecraft.item.{ItemBlock, ItemStack}
import org.lwjgl.opengl.GL11

class GuiIcon(id: Int, xPos: Int, yPos: Int, itemStacks: ItemStack*) extends GuiButton(id, xPos, yPos, 20, 20, "")
{
  private val itemRenderer = new RenderItem
  private var index = 0

  def setIndex(i: Int)
  {
    if (i >= 0 && i < itemStacks.length)
    {
      this.index = i
    }
  }

  override def drawButton(par1Minecraft: Minecraft, par2: Int, par3: Int)
  {
    super.drawButton(par1Minecraft, par2, par3)

    if (visible && itemStacks(index) != null)
    {
      var yDisplacement = 2

      val block = itemStacks(index).getItem.asInstanceOf[ItemBlock].field_150939_a

      if (block == Blocks.unlit_redstone_torch || block == Blocks.redstone_torch)
      {
        yDisplacement = 0
      }
      else if (this.itemStacks(index).getItem.isInstanceOf[ItemBlock])
      {
        yDisplacement = 3
      }

      this.drawItemStack(this.itemStacks(index), xPosition, yPosition + yDisplacement)
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