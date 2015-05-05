package mffs.gui

import mffs.production.BlockFortronCapacitor
import nova.core.event.EventListener
import nova.core.gui.GuiEvent.BindEvent
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

	onGuiEvent(new EventListener[GuiEvent.BindEvent] {
		override def onEvent(event: BindEvent) {
			addInventory("main", event.block.get().asInstanceOf[BlockFortronCapacitor].inventory)
		}
	}, classOf[GuiEvent.BindEvent])
}
