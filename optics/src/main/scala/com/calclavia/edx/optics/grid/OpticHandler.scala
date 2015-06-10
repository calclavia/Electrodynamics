package com.calclavia.edx.optics.grid

import com.calclavia.edx.optics.grid.OpticHandler.ReceiveBeamEvent
import nova.core.block.Block
import nova.core.block.Stateful.{LoadEvent, UnloadEvent}
import nova.core.component.Component
import nova.core.event.{Event, EventBus}
import nova.core.util.RayTracer.RayTraceResult
import nova.scala.wrapper.FunctionalWrapper._
import org.apache.commons.math3.util.FastMath

/**
 * Handles laser interaction
 * @author Calclavia
 */
object OpticHandler {

	class ReceiveBeamEvent(val incident: Beam, var hit: RayTraceResult) extends Event {
		var hasImpact = true

		def receivingPower = FastMath.max(incident.power - OpticGrid.minPower * hit.distance, 0)

		//Continues the beam
		def continue(outgoing: Beam) {
			outgoing.update()
		}
	}

}

class OpticHandler(val block: Block) extends Component {

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
			if (emitting.sameAs(laser)) {
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

	//TODO: Implement
	def energy = 0d

}
