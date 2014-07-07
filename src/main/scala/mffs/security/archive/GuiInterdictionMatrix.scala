package mffs.security.archive

import mffs.ModularForceFieldSystem
import mffs.base.{GuiMFFS, TilePacketType}
import mffs.security.ContainerInterdictionMatrix
import net.minecraft.client.gui.GuiButton
import net.minecraft.entity.player.EntityPlayer
import resonant.lib.gui.GuiContainerBase.SlotType
import resonant.lib.network.PacketTile
import resonant.lib.utility.LanguageUtility
import universalelectricity.api.UnitDisplay
import universalelectricity.core.transform.vector.Vector2
import resonant.lib.wrapper.WrapList._
class GuiInterdictionMatrix(player: EntityPlayer, tileEntity: TileInterdictionMatrix) extends GuiMFFS(new ContainerInterdictionMatrix(player, tileEntity), tileEntity)
{
  override def initGui
  {
    this.textFieldPos = new Vector2(110, 91)
    super.initGui
    this.buttonList.add(new GuiButton(1, this.width / 2 - 80, this.height / 2 - 65, 50, 20, LanguageUtility.getLocal("gui.matrix.banned")))
  }

  override def actionPerformed(guiButton: GuiButton)
  {
    super.actionPerformed(guiButton)
    if (guiButton.id == 1)
    {
      ModularForceFieldSystem.packetHandler.sendToAll(new PacketTile(this.tileEntity, TilePacketType.TOGGLE_MODE.id : Integer))
    }
  }

  override def drawGuiContainerForegroundLayer(x: Int, y: Int)
  {
    this.fontRenderer.drawString(this.tileEntity.getInvName, xSize / 2 - fontRendererObj.getStringWidth(tileEntity.getInvName) / 2, 6, 4210752)
    this.drawTextWithTooltip("warn", "%1: " + this.tileEntity.getWarningRange, 35, 19, x, y)
    this.drawTextWithTooltip("action", "%1: " + this.tileEntity.getActionRange, 100, 19, x, y)
    this.drawTextWithTooltip("filterMode", "%1:", 9, 32, x, y)
    if (!this.tileEntity.isBanMode)
    {
      if (this.buttonList.get(1).isInstanceOf[GuiButton])
      {
        (this.buttonList.get(1).asInstanceOf[GuiButton]).displayString = LanguageUtility.getLocal("gui.matrix.allowed")
      }
    }
    else
    {
      if (this.buttonList.get(1).isInstanceOf[GuiButton])
      {
        (this.buttonList.get(1).asInstanceOf[GuiButton]).displayString = LanguageUtility.getLocal("gui.matrix.banned")
      }
    }
    this.drawTextWithTooltip("frequency", "%1:", 8, 93, x, y)
    this.textFieldFrequency.drawTextBox
    this.drawTextWithTooltip("fortron", "%1: " + new UnitDisplay(UnitDisplay.Unit.LITER, tileEntity.getFortronEnergy).simple + "/" + new UnitDisplay(UnitDisplay.Unit.LITER, tileEntity.getFortronCapacity).simple, 8, 110, x, y)
    this.fontRenderer.drawString("\u00a74-" + new UnitDisplay(UnitDisplay.Unit.LITER, tileEntity.getFortronCost * 20).simple + "/s", 118, 121, 4210752)
    super.drawGuiContainerForegroundLayer(x, y)
  }

  override def drawGuiContainerBackgroundLayer(var1: Float, x: Int, y: Int)
  {
    super.drawGuiContainerBackgroundLayer(var1, x, y)

      var var3: Int = 0

      while (var3 < 2)
      {
        {
          {
            var var4: Int = 0
            while (var4 < 4)
            {

              this.drawSlot(98 + var4 * 18, 30 + var3 * 18)


              var4 += 1
            }
          }
        }

        var3 += 1
      }
      var var4: Int = 0
      while (var4 < 9)
      {
        if (this.tileEntity.isBanMode)
        {
          this.drawSlot(8 + var4 * 18, 68, SlotType.NONE, 1f, 0.8f, 0.8f)
        }
        else
        {
          this.drawSlot(8 + var4 * 18, 68, SlotType.NONE, 0.8f, 1f, 0.8f)
        }

        var4 += 1
      }

    this.drawSlot(68, 88)
    this.drawSlot(86, 88)
    this.drawForce(8, 120, Math.min(this.tileEntity.getFortronEnergy.asInstanceOf[Float] / this.tileEntity.getFortronCapacity.asInstanceOf[Float], 1))
  }
}