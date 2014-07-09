package mffs.production

import mffs.ModularForceFieldSystem
import mffs.base.{GuiMFFS, TilePacketType}
import net.minecraft.client.gui.GuiButton
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.tileentity.TileEntity
import org.lwjgl.opengl.GL11
import resonant.lib.gui.GuiContainerBase.SlotType
import resonant.lib.network.PacketTile
import resonant.lib.utility.LanguageUtility
import resonant.lib.wrapper.WrapList._
import universalelectricity.api.UnitDisplay
import universalelectricity.core.transform.vector.Vector2

class GuiCoercionDeriver(player: EntityPlayer, tile: TileCoercionDeriver) extends GuiMFFS(new ContainerCoercionDeriver(player, tile), tile)
{
  override def initGui
  {
    super.initGui
    this.buttonList.add(new GuiButton(1, this.width / 2 - 10, this.height / 2 - 28, 58, 20, LanguageUtility.getLocal("gui.deriver.derive")))
  }

  override def drawGuiContainerForegroundLayer(x: Int, y: Int)
  {
    fontRendererObj.drawString(tile.getInventoryName, this.xSize / 2 - fontRendererObj.getStringWidth(tile.getInventoryName) / 2, 6, 4210752)
    this.drawTextWithTooltip("frequency", "%1:", 8, 30, x, y)
    GL11.glPushMatrix
    GL11.glRotatef(-90, 0, 0, 1)
    this.drawTextWithTooltip("upgrade", -95, 140, x, y)
    GL11.glPopMatrix

    if (buttonList.get(1).isInstanceOf[GuiButton])
    {
      if (!this.tile.isInversed)
      {
        (buttonList.get(1).asInstanceOf[GuiButton]).displayString = LanguageUtility.getLocal("gui.deriver.derive")
      }
      else
      {
        (buttonList.get(1).asInstanceOf[GuiButton]).displayString = LanguageUtility.getLocal("gui.deriver.integrate")
      }
    }

    renderUniversalDisplay(85, 30, tile.getPower, x, y, UnitDisplay.Unit.WATT)
    fontRendererObj.drawString(new UnitDisplay(UnitDisplay.Unit.VOLTAGE, tile.getVoltage).simple().toString, 85, 40, 4210752)
    this.drawTextWithTooltip("progress", "%1: " + (if (this.tile.isActive) LanguageUtility.getLocal("gui.deriver.running") else LanguageUtility.getLocal("gui.deriver.idle")), 8, 70, x, y)
    this.drawTextWithTooltip("fortron", "%1: " + new UnitDisplay(UnitDisplay.Unit.LITER, this.tile.getFortronEnergy).simple(), 8, 105, x, y)
    fontRendererObj.drawString((if (this.tile.isInversed) "\u00a74-" else "\u00a72+") + new UnitDisplay(UnitDisplay.Unit.LITER, tile.productionRate * 20).simple() + "/s", 118, 117, 4210752)
    super.drawGuiContainerForegroundLayer(x, y)
  }

  override def drawGuiContainerBackgroundLayer(f: Float, x: Int, y: Int)
  {
    super.drawGuiContainerBackgroundLayer(f, x, y)
    this.drawSlot(153, 46)
    this.drawSlot(153, 66)
    this.drawSlot(153, 86)
    this.drawSlot(8, 40)
    this.drawSlot(8, 82, SlotType.BATTERY)
    this.drawSlot(8 + 20, 82)
    this.drawBar(50, 84, 1)
    this.drawForce(8, 115, this.tile.getFortronEnergy.asInstanceOf[Float] / this.tile.getFortronCapacity.asInstanceOf[Float])
  }

  override def actionPerformed(guibutton: GuiButton)
  {
    super.actionPerformed(guibutton)

    if (guibutton.id == 1)
    {
      ModularForceFieldSystem.packetHandler.sendToAll(new PacketTile(tile.asInstanceOf[TileEntity], TilePacketType.TOGGLE_MODE.id: Integer))
    }
  }
}