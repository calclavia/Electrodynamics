package com.calclavia.edx.optics.grid

import nova.core.block.Block
import nova.core.block.Stateful.{LoadEvent, UnloadEvent}
import nova.core.component.Component
import nova.core.event.Event
import nova.core.util.RayTracer.RayTraceResult
import nova.scala.wrapper.FunctionalWrapper._
import org.apache.commons.math3.util.FastMath

/**
 * Handles laser interaction
 * @author Calclavia
 */
object OpticHandler {

	/**
	 * Called when the total energy due to incident waves changes
	 */
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

	private var emitting: ElectromagneticBeam = null

	var power = 0d

	//Hook block events.
	block.events.on(classOf[LoadEvent]).bind((evt: LoadEvent) => OpticGrid(block.world).register(this))
	block.events.on(classOf[UnloadEvent]).bind((evt: UnloadEvent) => {
		destroy()
		OpticGrid(block.world).unregister(this)
	})

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
}
