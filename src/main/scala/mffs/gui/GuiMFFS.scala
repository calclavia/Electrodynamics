package mffs.gui

import com.resonant.lib.wrapper.WrapFunctions._
import nova.core.gui.components.inventory.PlayerInventory
import nova.core.gui.layout.Anchor
import nova.core.gui.{Gui, GuiEvent}

/**
 * @author Calclavia
 */
class GuiMFFS(id: String) extends Gui(id) {
	add(new PlayerInventory("inventory"), Anchor.SOUTH)
	onGuiEvent((evt: GuiEvent.BindEvent) => reset(), classOf[GuiEvent.BindEvent])
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
