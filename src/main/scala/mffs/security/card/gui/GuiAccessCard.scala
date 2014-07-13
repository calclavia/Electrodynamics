package mffs.security.card.gui

import mffs.ModularForceFieldSystem
import mffs.item.gui.GuiItem
import mffs.security.card.ItemCardAccess
import net.minecraft.client.gui.GuiButton
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.Container
import net.minecraft.item.ItemStack
import resonant.lib.access.java.Permissions
import resonant.lib.network.discriminator.PacketPlayerItem
import resonant.lib.render.EnumColor
import resonant.lib.wrapper.WrapList._

import scala.collection.convert.wrapAll._

/**
 * A gui that contains the permissions
 * @author Calclavia
 */
abstract class GuiAccessCard(player: EntityPlayer, itemStack: ItemStack, container: Container) extends GuiItem(itemStack, container)
{
  val itemAccess = itemStack.getItem.asInstanceOf[ItemCardAccess]
  val permissions = Permissions.root.getAllChildren.toList
  val scroll = new GuiScroll(permissions.size)

  override def initGui()
  {
    super.initGui()
    (0 until permissions.size) foreach (i => buttonList.add(new GuiButton(i, 0, 0, 160, 20, permissions(i).toString)))
  }

  /**
   * Updates the scroll list
   */
  protected override def updateScreen()
  {
    val index = (scroll.currentScroll * buttonList.size).toInt
    val maxIndex = index + 3
    val access = itemAccess.getAccess(player.getCurrentEquippedItem)

    buttonList map (_.asInstanceOf[GuiButton]) foreach (button =>
    {
      if (button.id >= index && button.id <= maxIndex)
      {
        val perm = permissions(button.id)
        button.displayString = (if (access != null && access.permissions.contains(perm)) EnumColor.BRIGHT_GREEN else EnumColor.RED) + perm.toString
        button.xPosition = width / 2 - 80
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
    ModularForceFieldSystem.packetHandler.sendToServer(new PacketPlayerItem(player) <<< 0 <<< permissions(guiButton.id).toString)
  }

}
