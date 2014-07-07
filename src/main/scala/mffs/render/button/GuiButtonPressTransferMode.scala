package mffs.render.button

import mffs.base.GuiMFFS
import mffs.production.TileFortronCapacitor
import net.minecraft.client.Minecraft
import universalelectricity.core.transform.vector.Vector2

class GuiButtonPressTransferMode(id: Int, x: Int, y: Int, mainGui: GuiMFFS, tile: TileFortronCapacitor) extends GuiButtonPress(id, x, y, new Vector2, mainGui)
{
  override def drawButton(minecraft: Minecraft, x: Int, y: Int)
  {
    var transferName = tile.getTransferMode.toString
    val first = Character.toUpperCase(transferName.charAt(0))
    transferName = first + transferName.substring(1)
    this.displayString = "transferMode" + transferName
    val offsetY = 18 * tile.getTransferMode.id
    super.drawButton(minecraft, x, y)
  }
}