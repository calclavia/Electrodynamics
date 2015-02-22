package mffs.security.card.gui

import mffs.ModularForceFieldSystem
import mffs.item.gui.ContainerItem
import mffs.security.card.ItemCardIdentification

/**
 * @author Calclavia
 */
class GuiCardID(player: EntityPlayer, Item: Item) extends GuiAccessCard(player, Item, new ContainerItem(player, Item))
{
  override def initGui()
  {
    super.initGui()
    textField.setMaxStringLength(20)

	  val item = Item.getItem.asInstanceOf[ItemCardIdentification]
	  val access = item.getAccess(Item)

    if (access != null)
      textField.setText(access.username)
  }

  override def keyTyped(char: Char, p_73869_2_ : Int)
  {
    super.keyTyped(char, p_73869_2_)
    ModularForceFieldSystem.packetHandler.sendToServer(new PacketPlayerItem(player) <<< 1 <<< textField.getText)
  }

  protected override def drawGuiContainerForegroundLayer(x: Int, y: Int)
  {
	  drawStringCentered(Game.instance.get.languageManager.getLocal("item.mffs:cardIdentification.name"))
    textField.drawTextBox()
    super.drawGuiContainerForegroundLayer(x, y)
  }

  protected override def drawGuiContainerBackgroundLayer(f: Float, x: Int, y: Int)
  {
    super.drawGuiContainerBackgroundLayer(f, x, y)
  }

}
