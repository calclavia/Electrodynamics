package mffs.item.gui

import mffs.Settings
import net.minecraft.client.gui.GuiButton
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import resonant.lib.utility.LanguageUtility
import resonant.lib.wrapper.WrapList._

import scala.util.Random

/**
 * @author Calclavia
 */
class GuiFrequency(player: EntityPlayer, itemStack: ItemStack) extends GuiItem(itemStack, new ContainerItem(player, itemStack))
{
  override def initGui()
  {
    super.initGui()
    buttonList.add(new GuiButton(1, width / 2 - 50, height / 2 - 60, 110, 20, LanguageUtility.getLocal("gui.frequency.random")))
    textField.setMaxStringLength(Settings.maxFrequencyDigits)
  }

  protected override def drawGuiContainerForegroundLayer(x: Int, y: Int)
  {
    drawStringCentered(LanguageUtility.getLocal("item.mffs:cardFrequency.name"))
    drawStringCentered("" + item.getEncodedFrequency(itemStack), 20)
    // drawStringCentered(LanguageUtility.getLocal("gui.makecopy"), 80)

    val tooltip = LanguageUtility.splitStringPerWord(LanguageUtility.getLocal("item.mffs:cardFrequency.tooltip"), 3)
    (0 until tooltip.size) foreach (i => drawStringCentered(tooltip.get(i), 80 + 10 * i))

    super.drawGuiContainerForegroundLayer(x, y)
  }

  override def keyTyped(char: Char, p_73869_2_ : Int)
  {
    super.keyTyped(char, p_73869_2_)

    try
    {
      item.setFrequency(Math.abs(textField.getText.toInt), itemStack)
    }
    catch
      {
        case _: Throwable =>
      }
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
