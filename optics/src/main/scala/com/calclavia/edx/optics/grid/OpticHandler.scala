package com.calclavia.edx.optics.grid

import com.calclavia.edx.optics.grid.OpticHandler.ReceiveBeamEvent
import com.resonant.lib.WrapFunctions._
import nova.core.block.Block
import nova.core.block.Stateful.LoadEvent
import nova.core.component.Component
import nova.core.event.{Event, EventBus}
import nova.core.util.RayTracer.RayTraceResult

/**
 * Handles laser interaction
 * @author Calclavia
 */
object OpticHandler {

	class ReceiveBeamEvent(val incident: Beam, val hit: RayTraceResult) extends Event {
		def receivingPower = incident.power - OpticGrid.minPower / 5 * hit.distance
	}

}

class OpticHandler(block: Block) extends Component {

	/**
	 * Called when the total energy due to incident waves changes
	 */
	var onReceive = new EventBus[ReceiveBeamEvent]

	private var emitting: Electromagnetic = null

	//Hook block events.
	block.loadEvent.add(
		eventListener((evt: LoadEvent) => {
			//Init grid
			OpticGrid(block.world)
		})
	)

	def create(laser: Electromagnetic) {

		if (emitting != null) {
			if (!emitting.equals(laser)) {
				destroy()
			}
		}

		OpticGrid(block.world).create(laser)
		emitting = laser
	}

	def destroy() {
		OpticGrid(block.world).destroy(emitting)
		emitting = null
	}

}
