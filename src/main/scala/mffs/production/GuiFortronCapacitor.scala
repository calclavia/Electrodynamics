package mffs.production

import mffs.ModularForceFieldSystem
import mffs.base.{GuiMFFS, TilePacketType}
import mffs.render.button.GuiButtonPressTransferMode
import net.minecraft.client.gui.GuiButton
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.opengl.GL11
import resonant.lib.network.PacketTile
import resonant.lib.wrapper.WrapList._
import universalelectricity.api.UnitDisplay
import universalelectricity.core.transform.vector.Vector2

class GuiFortronCapacitor(player: EntityPlayer, tile: TileFortronCapacitor) extends GuiMFFS(new ContainerFortronCapacitor(player, tile), tile)
{
  override def initGui
  {
    this.textFieldPos = new Vector2(50, 76)
    super.initGui
    this.buttonList.add(new GuiButtonPressTransferMode(1, this.width / 2 + 15, this.height / 2 - 37, this, this.tile))
  }

  protected override def drawGuiContainerForegroundLayer(x: Int, y: Int)
  {
    fontRendererObj.drawString(tile.getInventoryName, this.xSize / 2 - fontRendererObj.getStringWidth(tile.getInventoryName) / 2, 6, 4210752)
    GL11.glPushMatrix
    GL11.glRotatef(-90, 0, 0, 1)
    this.drawTextWithTooltip("upgrade", -95, 140, x, y)
    GL11.glPopMatrix
    this.drawTextWithTooltip("linkedDevice", "%1: " + this.tile.getLinkedDevices.size, 8, 28, x, y)
    this.drawTextWithTooltip("transmissionRate", "%1: " + new UnitDisplay(UnitDisplay.Unit.LITER, tile.getTransmissionRate * 20).simple() + "/s", 8, 40, x, y)
    this.drawTextWithTooltip("range", "%1: " + this.tile.getTransmissionRange, 8, 52, x, y)
    this.drawTextWithTooltip("frequency", "%1:", 8, 63, x, y)
    this.textFieldFrequency.drawTextBox
    this.drawTextWithTooltip("fortron", "%1:", 8, 95, x, y)
    fontRendererObj.drawString(new UnitDisplay(UnitDisplay.Unit.LITER, tile.getFortronEnergy).simple() + "/" + new UnitDisplay(UnitDisplay.Unit.LITER, tile.getFortronCapacity), 8, 105, 4210752)

    if (tile.getFortronCost > 0)
    {
      fontRendererObj.drawString("\u00a74-" + new UnitDisplay(UnitDisplay.Unit.LITER, tile.getFortronCost * 20).simple() + "/s", 118, 116, 4210752)
    }
    super.drawGuiContainerForegroundLayer(x, y)
  }

  protected override def drawGuiContainerBackgroundLayer(f: Float, x: Int, y: Int)
  {
    super.drawGuiContainerBackgroundLayer(f, x, y)
    this.drawSlot(153, 46)
    this.drawSlot(153, 66)
    this.drawSlot(153, 86)
    this.drawSlot(8, 73)
    this.drawSlot(26, 73)
    this.drawForce(8, 115, Math.min(this.tile.getFortronEnergy.asInstanceOf[Float] / this.tile.getFortronCapacity.asInstanceOf[Float], 1))
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