package mffs.item.gui

import mffs.item.card.ItemCardFrequency
import net.minecraft.client.gui.GuiTextField
import net.minecraft.inventory.Container
import net.minecraft.item.ItemStack
import resonant.lib.gui.GuiContainerBase

/**
 * @author Calclavia
 */
class GuiItem(itemStack: ItemStack, container: Container) extends GuiContainerBase(container)
{
  val item = itemStack.getItem.asInstanceOf[ItemCardFrequency]
  var textField: GuiTextField = _

  override def initGui()
  {
    super.initGui()
    textField = new GuiTextField(fontRendererObj, 50, 30, 80, 15)
  }

  protected override def drawGuiContainerForegroundLayer(x: Int, y: Int)
  {
    textField.drawTextBox()
    super.drawGuiContainerForegroundLayer(x, y)
  }

  override def mouseClicked(x: Int, y: Int, par3: Int)
  {

    super.mouseClicked(x, y, par3)

    if (textField != null)
    {
      textField.mouseClicked(x - this.containerWidth, y - this.containerHeight, par3)
    }
  }

  override def keyTyped(char: Char, p_73869_2_ : Int)
  {
    if (p_73869_2_ == 1 || p_73869_2_ == this.mc.gameSettings.keyBindInventory.getKeyCode)
      super.keyTyped(char, p_73869_2_)
    textField.textboxKeyTyped(char, p_73869_2_)
  }
}
