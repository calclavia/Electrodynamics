package mffs.production

import mffs.ModularForceFieldSystem
import mffs.base.{GuiMFFS, TilePacketType}
import mffs.render.button.GuiTransferModeButton
import net.minecraft.client.gui.GuiButton
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.opengl.GL11
import resonant.lib.gui.GuiContainerBase.SlotType
import resonant.lib.network.PacketTile
import resonant.lib.render.EnumColor
import resonant.lib.wrapper.WrapList._
import universalelectricity.api.UnitDisplay

class GuiFortronCapacitor(player: EntityPlayer, tile: TileFortronCapacitor) extends GuiMFFS(new ContainerFortronCapacitor(player, tile), tile)
{
  override def initGui()
  {
    super.initGui()
    this.buttonList.add(new GuiTransferModeButton(1, this.width / 2 - 30, this.height / 2 - 30, this, this.tile))
  }

  protected override def drawGuiContainerForegroundLayer(x: Int, y: Int)
  {
    fontRendererObj.drawString(tile.getInventoryName, this.xSize / 2 - fontRendererObj.getStringWidth(tile.getInventoryName) / 2, 6, 4210752)
    GL11.glPushMatrix
    GL11.glRotatef(-90, 0, 0, 1)
    drawTextWithTooltip("upgrade", -95, 140, x, y)
    GL11.glPopMatrix
    drawTextWithTooltip("linkedDevice", "%1: " + tile.getDeviceCount, 8, 20, x, y)
    drawTextWithTooltip("transmissionRate", "%1: " + new UnitDisplay(UnitDisplay.Unit.LITER, tile.getTransmissionRate * 20).symbol() + "/s", 8, 32, x, y)
    drawTextWithTooltip("range", "%1: " + tile.getTransmissionRange, 8, 44, x, y)
    drawTextWithTooltip("input", EnumColor.DARK_GREEN + "%1", 12, 62, x, y)
    drawTextWithTooltip("output", EnumColor.RED + "%1", 92, 62, x, y)
    drawFortronText(x, y)
    super.drawGuiContainerForegroundLayer(x, y)
  }

  protected override def drawGuiContainerBackgroundLayer(f: Float, x: Int, y: Int)
  {
    super.drawGuiContainerBackgroundLayer(f, x, y)

    //Upgrade slots
    (0 to 2) foreach (y => drawSlot(153, 46 + y * 18))

    //Input slots
    for (x <- 0 to 1; y <- 0 to 1)
      drawSlot(8 + x * 18, 73 + y * 18)

    //Output slots
    for (x <- 0 to 1; y <- 0 to 1)
      drawSlot(90 + x * 18, 73 + y * 18)

    drawFrequencyGui()
  }

  protected override def actionPerformed(guiButton: GuiButton)
  {
    super.actionPerformed(guiButton)

    if (guiButton.id == 1)
    {
      ModularForceFieldSystem.packetHandler.sendToAll(new PacketTile(this.tile, TilePacketType.TOGGLE_MODE.id: Integer))
    }
  }
}