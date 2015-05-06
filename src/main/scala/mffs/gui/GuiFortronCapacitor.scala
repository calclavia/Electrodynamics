package mffs.gui

import com.resonant.lib.wrapper.WrapFunctions._
import mffs.production.BlockFortronCapacitor
import nova.core.gui.components.inventory.{PlayerInventory, Slot}
import nova.core.gui.components.{Container, Label}
import nova.core.gui.layout.{Anchor, FlowLayout}
import nova.core.gui.{Gui, GuiEvent}

/**
 * Fortron Capacitor Gui
 * @author Calclavia
 */
class GuiFortronCapacitor extends Gui("fortronCapacitor") {
	setPreferredSize(180, 200)
	add(new Label("title", "Fortron Capacitor"))
	add(new Container("container")
		.add(new Container("container")
		.setLayout(new FlowLayout)
		.add(new Slot("slot", "main", 0))

		)
	)
	add(new PlayerInventory("inventory"), Anchor.SOUTH)

	onGuiEvent((evt: GuiEvent.BindEvent) => addInventory("main", evt.block.get().asInstanceOf[BlockFortronCapacitor].inventory), classOf[GuiEvent.BindEvent])
}
