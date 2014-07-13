package mffs.field.gui

import mffs.ModularForceFieldSystem
import mffs.base.TilePacketType
import mffs.field.TileElectromagneticProjector
import mffs.render.button.GuiIcon
import net.minecraft.client.gui.GuiButton
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import resonant.lib.network.discriminator.PacketTile
import resonant.lib.render.EnumColor
import resonant.lib.wrapper.WrapList._
import universalelectricity.api.UnitDisplay

class GuiElectromagneticProjector(player: EntityPlayer, tile: TileElectromagneticProjector) extends GuiMatrix(new ContainerElectromagneticProjector(player, tile), tile)
{
  override def initGui()
  {
    super.initGui()
    buttonList.add(new GuiIcon(1, width / 2 - 110, height / 2 - 82, null, new ItemStack(Items.compass)))
    buttonList.add(new GuiButton(2, width / 2 - 73, height / 2 - 20, 45, 20, "Invert"))
    setupTooltips()
  }

  override def updateScreen
  {
    super.updateScreen

    if (buttonList.get(1).asInstanceOf[GuiIcon].setIndex(if (tile.absoluteDirection) 1 else 0))
    {
      setupTooltips()
    }

    buttonList.get(2).asInstanceOf[GuiButton].displayString = (if (tile.isInvertedFilter) EnumColor.BRIGHT_GREEN else EnumColor.RED) + "Invert"
  }

  protected override def drawGuiContainerForegroundLayer(x: Int, y: Int)
  {
    drawStringCentered(tile.getInventoryName)
    drawString("Filters", 20, 20)

    drawFortronText(x, y)
    drawString(EnumColor.RED + new UnitDisplay(UnitDisplay.Unit.LITER, tile.getFortronCost * 20).symbol().toString + "/s", 120, 119)
    super.drawGuiContainerForegroundLayer(x, y)
  }

  protected override def drawGuiContainerBackgroundLayer(f: Float, x: Int, y: Int)
  {
    super.drawGuiContainerBackgroundLayer(f, x, y)
    drawMatrix()
    drawFrequencyGui()

    //Filter slots
    for (x <- 0 until 2; y <- 0 until 3)
      drawSlot(20 + 18 * x, 30 + 18 * y)
  }

  protected override def actionPerformed(guiButton: GuiButton)
  {
    super.actionPerformed(guiButton)

    if (guiButton.id == 1)
    {
      ModularForceFieldSystem.packetHandler.sendToServer(new PacketTile(tile) <<< TilePacketType.toggleMode4.id)
    }

    if (guiButton.id == 2)
    {
      ModularForceFieldSystem.packetHandler.sendToServer(new PacketTile(tile) <<< TilePacketType.toggleMode2.id)
    }
  }

}