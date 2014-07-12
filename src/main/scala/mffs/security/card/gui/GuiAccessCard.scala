package mffs.security.card.gui

import mffs.Settings
import mffs.item.gui.GuiItem
import net.minecraft.client.gui.GuiButton
import net.minecraft.inventory.Container
import net.minecraft.item.ItemStack
import resonant.lib.access.java.Permissions
import resonant.lib.utility.LanguageUtility
import universalelectricity.core.transform.vector.Vector2

import scala.collection.convert.wrapAll._
import scala.util.Random
import resonant.lib.wrapper.WrapList._

/**
 * A gui that contains the permissions
 * @author Calclavia
 */
abstract class GuiAccessCard(itemStack: ItemStack, container: Container) extends GuiItem(itemStack, container)
{
  val scroll = new GuiScroll( 5)
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
    val maxIndex = index + 4

    buttonList map (_.asInstanceOf[GuiButton]) foreach (button =>
    {
      if (button.id >= index && button.id <= maxIndex)
      {
        button.xPosition = width / 2 - 50
        button.xPosition = height / 2 - 60 + (button.id - index) * 20
        button.visible = true
      }
      else
      {
        button.visible = false
      }
    })
  }

  protected override def drawGuiContainerForegroundLayer(x: Int, y: Int)
  {
    drawStringCentered(LanguageUtility.getLocal("item.mffs:cardFrequency.name"))
    drawStringCentered("" + item.getEncodedFrequency(itemStack), 20)

    drawStringCentered(LanguageUtility.getLocal("gui.makecopy"), 80)

    super.drawGuiContainerForegroundLayer(x, y)
  }

  protected override def drawGuiContainerBackgroundLayer(f: Float, x: Int, y: Int)
  {
    super.drawGuiContainerBackgroundLayer(f, x, y)

    //Copy slot
    drawSlot(80, 100)
  }

  override def handleMouseInput()
  {
    super.handleMouseInput()
    scroll.handleMouseInput()
  }

  protected override def actionPerformed(guiButton: GuiButton)
  {
    super.actionPerformed(guiButton)

    if (guiButton.id == 1)
    {
      val ranFreq = new Random().nextInt(Math.pow(10, (Settings.maxFrequencyDigits - 1)).toInt)
      textField.setText(ranFreq + "")
      item.setFrequency(ranFreq, itemStack)
    }
  }

}
