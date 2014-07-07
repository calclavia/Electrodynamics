package mffs.render.button

import mffs.base.GuiMFFS
import mffs.production.TileFortronCapacitor
import net.minecraft.client.Minecraft
import universalelectricity.core.transform.vector.Vector2

class GuiButtonPressTransferMode(id: Int, x: Int, y: Int, mainGui: GuiMFFS, tileEntity: TileFortronCapacitor) extends GuiButtonPress(id, x, y, new Vector2, mainGui)
{
  override def drawButton(minecraft: Minecraft, x: Int, y: Int)
  {
    var transferName: String = this.tileEntity.getTransferMode.name.toLowerCase
    val first: Char = Character.toUpperCase(transferName.charAt(0))
    transferName = first + transferName.substring(1)
    this.displayString = "transferMode" + transferName
    this.offset.y = 18 * this.tileEntity.getTransferMode.ordinal
    super.drawButton(minecraft, x, y)
  }
}