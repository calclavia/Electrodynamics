package mffs.field.gui

import mffs.ModularForceFieldSystem
import mffs.base.TilePacketType
import mffs.field.TileElectromagneticProjector
import mffs.render.button.GuiIcon
import net.minecraft.client.gui.GuiButton
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import resonant.lib.network.PacketTile
import resonant.lib.render.EnumColor
import resonant.lib.wrapper.WrapList._
import universalelectricity.api.UnitDisplay

class GuiElectromagneticProjector(player: EntityPlayer, tile: TileElectromagneticProjector) extends GuiMatrix(new ContainerMatrix(player, tile), tile)
{
  override def initGui()
  {
    super.initGui()
    buttonList.add(new GuiIcon(1, width / 2 - 110, height / 2 - 82, null, new ItemStack(Items.compass)))
    setupTooltips()
  }

  override def updateScreen
  {
    super.updateScreen

    if (buttonList.get(1).asInstanceOf[GuiIcon].setIndex(if (tile.absoluteDirection) 1 else 0))
    {
      setupTooltips()
    }
  }

  protected override def drawGuiContainerForegroundLayer(x: Int, y: Int)
  {
    drawStringCentered(tile.getInventoryName)
    drawString(tile.getDirection.name, 8, 100)

    drawFortronText(x, y)
    drawString(EnumColor.RED + new UnitDisplay(UnitDisplay.Unit.LITER, tile.getFortronCost * 20).symbol().toString + "/s", 120, 119)
    super.drawGuiContainerForegroundLayer(x, y)
  }

  protected override def drawGuiContainerBackgroundLayer(f: Float, x: Int, y: Int)
  {
    super.drawGuiContainerBackgroundLayer(f, x, y)
    drawMatrix()
    drawFrequencyGui()
  }

  protected override def actionPerformed(guiButton: GuiButton)
  {
    super.actionPerformed(guiButton)

    if (guiButton.id == 1)
    {
      ModularForceFieldSystem.packetHandler.sendToServer(new PacketTile(tile.asInstanceOf[TileEntity], TilePacketType.TOGGLE_MODE_4.id: Integer))
    }
  }

}