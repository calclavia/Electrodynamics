package mffs.security

import mffs.base.GuiMFFS
import net.minecraft.entity.player.EntityPlayer
import resonant.lib.render.EnumColor

class GuiBiometricIdentifier(player: EntityPlayer, tile: TileBiometricIdentifier) extends GuiMFFS(new ContainerBiometricIdentifier(player, tile), tile)
{
  override def initGui
  {
    super.initGui
  }

  protected override def drawGuiContainerForegroundLayer(x: Int, y: Int)
  {
    drawStringCentered(tile.getInventoryName)
    drawStringCentered(EnumColor.AQUA + "ID and Group Cards", 20)
    drawString("Frequency", 40, 118)
    drawFortronText(x, y)
    super.drawGuiContainerForegroundLayer(x, y)
  }

  protected override def drawGuiContainerBackgroundLayer(f: Float, x: Int, y: Int)
  {
    super.drawGuiContainerBackgroundLayer(f, x, y)
    drawFrequencyGui()
    for (x <- 0 until 9; y <- 0 until 4)
      drawSlot(8 + x * 18, 35 + y * 18)
  }
}