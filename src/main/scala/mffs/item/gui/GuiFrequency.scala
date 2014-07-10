package mffs.item.gui

import mffs.{ModularForceFieldSystem, Settings}
import net.minecraft.client.gui.GuiButton
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import resonant.lib.network.PacketPlayerItem
import resonant.lib.utility.LanguageUtility
import resonant.lib.wrapper.WrapList._

import scala.util.Random

/**
 * @author Calclavia
 */
class GuiFrequency(player: EntityPlayer, itemStack: ItemStack) extends GuiItem(itemStack, new ContainerFrequency(player, itemStack))
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

    drawStringCentered(LanguageUtility.getLocal("gui.makecopy"), 80)

    super.drawGuiContainerForegroundLayer(x, y)
  }

  protected override def drawGuiContainerBackgroundLayer(f: Float, x: Int, y: Int)
  {
    super.drawGuiContainerBackgroundLayer(f, x, y)
    drawSlot(80, 100)
  }

  override def keyTyped(char: Char, p_73869_2_ : Int)
  {
    super.keyTyped(char, p_73869_2_)

    try
    {
      val newFreq = Math.abs(textField.getText.toInt)
      ModularForceFieldSystem.packetHandler.sendToServer(new PacketPlayerItem(player, newFreq: Integer))
      item.setFrequency(newFreq, itemStack)
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
