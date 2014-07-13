package mffs.security.card.gui

import mffs.ModularForceFieldSystem
import mffs.item.gui.ContainerItem
import mffs.security.card.ItemCardIdentification
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import resonant.lib.network.discriminator.PacketPlayerItem
import resonant.lib.utility.LanguageUtility

/**
 * @author Calclavia
 */
class GuiCardID(player: EntityPlayer, itemStack: ItemStack) extends GuiAccessCard(player, itemStack, new ContainerItem(player, itemStack))
{
  override def initGui()
  {
    super.initGui()
    textField.setMaxStringLength(20)

    val item = itemStack.getItem.asInstanceOf[ItemCardIdentification]
    val access = item.getAccess(itemStack)

    if (access != null)
      textField.setText(access.username)
  }

  protected override def drawGuiContainerForegroundLayer(x: Int, y: Int)
  {
    drawStringCentered(LanguageUtility.getLocal("item.mffs:cardIdentification.name"))
    textField.drawTextBox()
    super.drawGuiContainerForegroundLayer(x, y)
  }

  protected override def drawGuiContainerBackgroundLayer(f: Float, x: Int, y: Int)
  {
    super.drawGuiContainerBackgroundLayer(f, x, y)
  }

  override def keyTyped(char: Char, p_73869_2_ : Int)
  {
    super.keyTyped(char, p_73869_2_)
    ModularForceFieldSystem.packetHandler.sendToServer(new PacketPlayerItem(player) <<< 1 <<< textField.getText)
  }

}
