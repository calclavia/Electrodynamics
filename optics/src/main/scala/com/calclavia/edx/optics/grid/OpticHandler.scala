package com.calclavia.edx.optics.grid

import com.calclavia.edx.optics.grid.OpticHandler.ReceiveBeamEvent
import com.resonant.lib.WrapFunctions._
import nova.core.block.Block
import nova.core.block.Stateful.{LoadEvent, UnloadEvent}
import nova.core.component.Component
import nova.core.event.{Event, EventBus}
import nova.core.util.RayTracer.RayTraceResult

/**
 * Handles laser interaction
 * @author Calclavia
 */
object OpticHandler {

	class ReceiveBeamEvent(val incident: Beam, var hit: RayTraceResult) extends Event {
		def receivingPower = incident.power - OpticGrid.minPower / 5 * hit.distance

		//Continues the beam
		def continue(outgoing: Beam) {
			OpticGrid(incident.world).create(outgoing, incident)
		}
	}

}

class OpticHandler(block: Block) extends Component {

	/**
	 * Called when the total energy due to incident waves changes
	 */
	var onReceive = new EventBus[ReceiveBeamEvent]

	private var emitting: ElectromagneticBeam = null

	//Hook block events.
	block.loadEvent.add(eventListener((evt: LoadEvent) => OpticGrid(block.world)))
	block.unloadEvent.add((evt: UnloadEvent) => destroy())

	def create(laser: ElectromagneticBeam) {
		if (emitting != null) {
			if (emitting.equals(laser)) {
				return
			}
			destroy()
		}

		OpticGrid(block.world).create(laser)
		emitting = laser
	}

	def destroy() {
		if (emitting != null) {
			OpticGrid(block.world).destroy(emitting)
			emitting = null
		}
	}

}
