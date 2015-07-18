package com.calclavia.edx.optics.grid

import com.calclavia.edx.optics.grid.OpticHandler.ReceiveBeamEvent
import nova.core.block.Block
import nova.core.block.Stateful.{LoadEvent, UnloadEvent}
import nova.core.component.Component
import nova.core.event.Event
import nova.core.util.Direction
import nova.core.util.RayTracer.RayTraceResult
import nova.scala.wrapper.FunctionalWrapper._
import org.apache.commons.math3.util.FastMath

/**
 * Handles laser interaction
 * @author Calclavia
 */
object OpticHandler {

	/** w
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

	//A set of incoming beams
	private var incoming = Set.empty[Beam]

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

	/**
	 * Sets the optic handler to accumulate beams within it.
	 */
	def accumulate(): this.type = {
		block.events
			.on(classOf[ReceiveBeamEvent])
			.bind((evt: ReceiveBeamEvent) => incoming += evt.incident)

		return this
	}

	/**
	 * Resets the accumulation
	 */
	def reset() {
		incoming = Set.empty
	}

	/**
	 * Calculates the power from a specific direction
	 * @param dir - The direction
	 * @return The power in watts
	 */
	def power(dir: Direction) = incoming
		.filter(i => Direction.fromVector(i.source.dir).opposite() == dir)
		.map(_.power)
		.sum + 60000

	//TODO: Remove power
	/**
	 * Calculates the total power
	 * @return The power in watts
	 */
	def power = incoming.map(_.power).sum + 60000
}
