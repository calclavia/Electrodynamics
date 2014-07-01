package mffs.gui.button

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.{FontRenderer, GuiButton}
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.entity.RenderItem
import net.minecraft.init.Blocks
import net.minecraft.item.{ItemBlock, ItemStack}
import org.lwjgl.opengl.GL11

object GuiIcon
{
  var itemRenderer: RenderItem = new RenderItem
}

class GuiIcon(par1: Int, par2: Int, par3: Int, itemStacks: ItemStack*) extends GuiButton(par1, par2, par3, 20, 20, "")
{
  private var index: Int = 0

  def setIndex(i: Int)
  {
    if (i >= 0 && i < this.itemStacks.length)
    {
      this.index = i
    }
  }

  override def drawButton(par1Minecraft: Minecraft, par2: Int, par3: Int)
  {
    super.drawButton(par1Minecraft, par2, par3)
    if (this.drawButton && this.itemStacks(this.index) != null)
    {
      var yDisplacement: Int = 2
      val block = itemStacks(this.index).getItem.asInstanceOf[ItemBlock].field_150939_a

      if (block == Blocks.unlit_redstone_torch || block == Blocks.redstone_torch)
      {
        yDisplacement = 0
      }
      else if (this.itemStacks(this.index).getItem.isInstanceOf[ItemBlock])
      {
        yDisplacement = 3
      }
      this.drawItemStack(this.itemStacks(this.index), this.xPosition, this.yPosition + yDisplacement)
    }
  }

  protected def drawItemStack(itemStack: ItemStack, x: Int, y: Int)
  {
    x += 2
    y -= 1
    val mc: Minecraft = Minecraft.getMinecraft
    val fontRenderer: FontRenderer = mc.fontRenderer
    RenderHelper.enableGUIStandardItemLighting
    GL11.glTranslatef(0.0F, 0.0F, 32.0F)
    this.zLevel = 500.0F
    itemRenderer.zLevel = 500.0F
    itemRenderer.renderItemAndEffectIntoGUI(fontRenderer, mc.renderEngine, itemStack, x, y)
    itemRenderer.renderItemOverlayIntoGUI(fontRenderer, mc.renderEngine, itemStack, x, y)
    this.zLevel = 0.0F
    itemRenderer.zLevel = 0.0F
    RenderHelper.disableStandardItemLighting
  }
}