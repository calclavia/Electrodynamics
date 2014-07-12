package mffs.security.card.gui

import mffs.item.gui.GuiItem
import net.minecraft.client.gui.GuiButton
import net.minecraft.inventory.Container
import net.minecraft.item.ItemStack
import resonant.lib.access.java.Permissions
import resonant.lib.wrapper.WrapList._

import scala.collection.convert.wrapAll._

/**
 * A gui that contains the permissions
 * @author Calclavia
 */
abstract class GuiAccessCard(itemStack: ItemStack, container: Container) extends GuiItem(itemStack, container)
{
  val scroll = new GuiScroll(5)
  val permissions = Permissions.root.getAllChildren.toList

  override def initGui()
  {
    super.initGui()
    (0 until permissions.size) foreach (i => buttonList.add(new GuiButton(i, 0, 0, 110, 20, permissions(i).toString)))
  }

  /**
   * Updates the scroll list
   */
  protected override def updateScreen
  {
    val index = (scroll.currentScroll * buttonList.size).toInt
    val maxIndex = index + 3

    buttonList map (_.asInstanceOf[GuiButton]) foreach (button =>
    {
      if (button.id >= index && button.id <= maxIndex)
      {
        button.xPosition = width / 2 - 50
        button.yPosition = height / 2 - 60 + (button.id - index) * 20
        button.visible = true
      }
      else
      {
        button.visible = false
      }
    })
  }

  override def handleMouseInput()
  {
    super.handleMouseInput()
    scroll.handleMouseInput()
  }

  protected override def actionPerformed(guiButton: GuiButton)
  {
    super.actionPerformed(guiButton)

    //Toggle this specific permission
    permissions(guiButton.id)
  }

}
