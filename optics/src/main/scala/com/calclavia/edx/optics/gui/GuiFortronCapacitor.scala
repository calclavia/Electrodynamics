package com.calclavia.edx.optics.gui

import com.calclavia.edx.optics.production.BlockFortronCapacitor
import nova.scala.wrapper.FunctionalWrapper
import FunctionalWrapper._
import com.calclavia.edx.core.EDX
import nova.core.gui.ComponentEvent.ActionEvent
import nova.core.gui.component.inventory.{PlayerInventory, Slot}
import nova.core.gui.component.{Button, Container, Label}
import nova.core.gui.layout.{Anchor, FlowLayout}
import nova.core.gui.render.Graphics
import nova.core.gui.{ComponentEvent, GuiEvent}
import nova.core.network.NetworkTarget.Side
import nova.energy.UnitDisplay

import scala.collection.convert.wrapAll._

/**
 * Fortron Capacitor Gui
 * @author Calclavia
 */
class GuiFortronCapacitor extends GuiMFFS("fortronCapacitor") {

	var block: BlockFortronCapacitor = null

	onGuiEvent((evt: GuiEvent.BindEvent) => reset(evt), classOf[GuiEvent.BindEvent])

	/*
		drawTextWithTooltip("range", "%1: " + tile.getTransmissionRange, 8, 44, x, y)
		drawTextWithTooltip("input", EnumColor.DARK_GREEN + "%1", 12, 62, x, y)
		drawTextWithTooltip("output", EnumColor.RED + "%1", 92, 62, x, y)
		 drawFrequencyGui()
		 drawFortron()
		 Toggle button
	 */

	def reset(evt: GuiEvent.BindEvent) {
		reset()

		block = evt.block.get().asInstanceOf[BlockFortronCapacitor]
		addInventory("main", evt.block.get().asInstanceOf[BlockFortronCapacitor].inventory)

		setPreferredSize(180, 200)

		val upgrades = new Container("upgrades").setLayout(new FlowLayout)

		//Input slots
		val inputs = new Container("inputs").setLayout(new FlowLayout)
		//Upgrades
		(0 to 2) foreach (i => upgrades.add(new Slot("main", i)))
		//Output slots
		val outputs = new Container("outputs").setLayout(new FlowLayout)
		for (x <- 0 to 1; y <- 0 to 1)
			inputs.add(new Slot("main", x + y))

		for (x <- 0 to 1; y <- 0 to 1)
			outputs.add(new Slot("main", x + y))

		/**
		 * Layout
		 */
		add(new Label("title", "Fortron Capacitor"), Anchor.NORTH)

		add(
			new Container("layout")
				.add(
			    new Container("north")
				    .setLayout(new FlowLayout())
				    .add(new Label("linkedDevices", " ")).setMinimumSize(200, 20)
				    .add(new Label("transmissionRate", " ")).setMinimumSize(200, 20)
			    , Anchor.NORTH
				)
				.add(
			    new Container("south")
				    .setLayout(new FlowLayout())
				    .add(upgrades)
				    //.add(inputs)
				    //.add(outputs)
				    //Toggle button
				    .add(
			        new Button("toggle", "Toggle Mode")
				        .setPreferredSize(80, 20)
				        .onEvent((evt: ActionEvent, component: Button) => block.toggleTransferMode(), classOf[ComponentEvent.ActionEvent], Side.SERVER)
				    )
			    , Anchor.SOUTH
				)
			, Anchor.CENTER
		)

		add(new PlayerInventory("inventory"), Anchor.SOUTH)
	}

	override def render(mouseX: Int, mouseY: Int, graphics: Graphics) {
		super.render(mouseX, mouseY, graphics)
		getChildElement("layout.north.linkedDevices").get().asInstanceOf[Label].setText(EDX.language.translate("gui.linkedDevice", Map("%1" -> (block.getDeviceCount + ""))))
		getChildElement("layout.north.transmissionRate").get().asInstanceOf[Label].setText(
			EDX.language.translate("gui.transmissionRate", Map("%1" -> (new UnitDisplay(UnitDisplay.Unit.LITER, block.getTransmissionRate * 20).symbol() + "/s")))
		)
		getChildElement("layout.south.toggle").get().asInstanceOf[Button].setText(block.getTransferMode.name())
	}
}
