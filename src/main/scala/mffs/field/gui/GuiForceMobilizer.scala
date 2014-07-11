package mffs.field.gui

import mffs.ModularForceFieldSystem
import mffs.base.TilePacketType
import mffs.field.mobilize.TileForceMobilizer
import mffs.render.button.GuiIcon
import net.minecraft.client.gui.GuiButton
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.{Blocks, Items}
import net.minecraft.item.ItemStack
import resonant.lib.network.discriminator.PacketTile
import resonant.lib.render.EnumColor
import resonant.lib.utility.LanguageUtility
import resonant.lib.wrapper.WrapList._
import universalelectricity.api.UnitDisplay
import universalelectricity.core.transform.vector.Vector2

class GuiForceMobilizer(player: EntityPlayer, tile: TileForceMobilizer) extends GuiMatrix(new ContainerMatrix(player, tile, new Vector2(110, 55)), tile)
{
  override def initGui
  {
    super.initGui
    buttonList.add(new GuiIcon(1, width / 2 - 110, height / 2 - 16, new ItemStack(Items.clock)))
    buttonList.add(new GuiIcon(2, width / 2 - 110, height / 2 - 82, null, new ItemStack(Items.redstone), new ItemStack(Blocks.redstone_block)))
    buttonList.add(new GuiIcon(3, width / 2 - 110, height / 2 - 60, null, new ItemStack(Blocks.anvil)))
    buttonList.add(new GuiIcon(4, width / 2 - 110, height / 2 - 38, null, new ItemStack(Items.compass)))

    setupTooltips()
  }

  protected override def drawGuiContainerForegroundLayer(x: Int, y: Int)
  {
    drawStringCentered(tile.getInventoryName)

    drawString(EnumColor.DARK_AQUA + LanguageUtility.getLocal("gui.mobilizer.anchor") + ":", 8, 20)
    drawString(tile.anchor.xi + ", " + tile.anchor.yi + ", " + tile.anchor.zi, 8, 32)

    drawString(EnumColor.DARK_AQUA + LanguageUtility.getLocal("gui.direction") + ":", 8, 48)
    drawString(tile.getDirection.name, 8, 60)

    drawString(EnumColor.DARK_AQUA + LanguageUtility.getLocal("gui.mobilizer.time") + ":", 8, 75)
    drawString((tile.clientMoveTime / 20) + "s", 8, 87)

    drawTextWithTooltip("fortron", EnumColor.DARK_RED + new UnitDisplay(UnitDisplay.Unit.LITER, tile.getFortronCost * 20).symbol().toString + "/s", 8, 100, x, y)
    drawFortronText(x, y)
    super.drawGuiContainerForegroundLayer(x, y)
  }

  override def updateScreen
  {
    super.updateScreen
    buttonList.get(2).asInstanceOf[GuiIcon].setIndex(tile.previewMode)
    buttonList.get(3).asInstanceOf[GuiIcon].setIndex(if (tile.doAnchor) 1 else 0)

    if (buttonList.get(4).asInstanceOf[GuiIcon].setIndex(if (tile.absoluteDirection) 1 else 0))
    {
      setupTooltips()
    }
  }

  override def drawGuiContainerBackgroundLayer(f: Float, x: Int, y: Int)
  {
    super.drawGuiContainerBackgroundLayer(f, x, y)
    drawMatrix()
    drawFrequencyGui()
  }

  override def actionPerformed(guiButton: GuiButton)
  {
    super.actionPerformed(guiButton)
    if (guiButton.id == 1)
    {

      ModularForceFieldSystem.packetHandler.sendToAll(new PacketTile(tile, TilePacketType.TOGGLE_MODE.id: Integer))
    }
    else if (guiButton.id == 2)
    {
      ModularForceFieldSystem.packetHandler.sendToAll(new PacketTile(tile, TilePacketType.TOGGLE_MODE_2.id: Integer))
    }
    else if (guiButton.id == 3)
    {
      ModularForceFieldSystem.packetHandler.sendToAll(new PacketTile(tile, TilePacketType.TOGGLE_MODE_3.id: Integer))
    }
    else if (guiButton.id == 4)
    {
      ModularForceFieldSystem.packetHandler.sendToAll(new PacketTile(tile, TilePacketType.TOGGLE_MODE_4.id: Integer))
    }
  }
}