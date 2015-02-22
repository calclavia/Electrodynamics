package mffs.item.gui

import mffs.item.card.ItemCardFrequency
import mffs.{ModularForceFieldSystem, Settings}

/**
 * @author Calclavia
 */
class GuiFrequency(player: EntityPlayer, Item: Item) extends GuiItem(Item, new ContainerFrequency(player, Item))
{
	val item = Item.getItem.asInstanceOf[ItemCardFrequency]

  override def initGui()
  {
    super.initGui()
	  buttonList.add(new GuiButton(1, width / 2 - 50, height / 2 - 60, 110, 20, Game.instance.get.languageManager.getLocal("gui.frequency.random")))
    textField.setMaxStringLength(Settings.maxFrequencyDigits)
  }

  override def keyTyped(char: Char, p_73869_2_ : Int)
  {
    super.keyTyped(char, p_73869_2_)

    try
    {
      val newFreq = Math.abs(textField.getText.toInt)
      ModularForceFieldSystem.packetHandler.sendToServer(new PacketPlayerItem(player, newFreq: Integer))
	  item.setFrequency(newFreq, Item)
    }
    catch
      {
        case _: Throwable =>
      }
  }

  protected override def drawGuiContainerForegroundLayer(x: Int, y: Int)
  {
	  drawStringCentered(Game.instance.get.languageManager.getLocal("item.mffs:cardFrequency.name"))
	  drawStringCentered("" + item.getEncodedFrequency(Item), 20)
    textField.drawTextBox()
	  drawStringCentered(Game.instance.get.languageManager.getLocal("gui.makecopy"), 80)

    super.drawGuiContainerForegroundLayer(x, y)
  }

  protected override def drawGuiContainerBackgroundLayer(f: Float, x: Int, y: Int)
  {
    super.drawGuiContainerBackgroundLayer(f, x, y)
    drawSlot(80, 100)
  }

  protected override def actionPerformed(guiButton: GuiButton)
  {
    super.actionPerformed(guiButton)

    if (guiButton.id == 1)
    {
      val ranFreq = new Random().nextInt(Math.pow(10, (Settings.maxFrequencyDigits - 1)).toInt)
      textField.setText(ranFreq + "")
		item.setFrequency(ranFreq, Item)
    }
  }

}
