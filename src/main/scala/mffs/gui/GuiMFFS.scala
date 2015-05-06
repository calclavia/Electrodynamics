package mffs.gui

import nova.core.gui.Gui
import nova.core.gui.components.inventory.PlayerInventory
import nova.core.gui.layout.Anchor

/**
 * @author Calclavia
 */
class GuiMFFS(id: String) extends Gui(id) {
	add(new PlayerInventory("inventory"), Anchor.SOUTH)

	/*
	Machine activate button

	  protected def drawFortronText(x: Int, y: Int)
	  {
		  if (tile.isInstanceOf[BlockFortron])
		{
			val fortronTile = tile.asInstanceOf[BlockFortron]
			drawTextWithTooltip("fortron", EnumColor.WHITE + "" + new UnitDisplay(UnitDisplay.Unit.LITER, fortronTile.getFortron).symbol() + "/" + new
					UnitDisplay(UnitDisplay.Unit.LITER, fortronTile.getFortronCapacity).symbol(), 35, 119, x, y)
		}
	  }

	  protected def drawFrequencyGui()
	  {
		//Frequency Card
		drawSlot(7, 113)

		  if (tile.isInstanceOf[BlockFortron])
		{
			val fortronTile = tile.asInstanceOf[BlockFortron]

		  //Fortron Bar
			drawLongBlueBar(30, 115, Math.min(fortronTile.getFortron.asInstanceOf[Float] / fortronTile.getFortronCapacity.asInstanceOf[Float], 1))
		}
	  }

	 */
}
