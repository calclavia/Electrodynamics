package com.calclavia.edx.electric.circuit.component.laser

import com.calclavia.edx.electric.circuit.component.laser.WaveGrid.Electromagnetic
import com.resonant.lib.WrapFunctions._
import nova.core.block.Block
import nova.core.block.Stateful.UnloadEvent
import nova.core.component.Component
import nova.core.event.{Event, EventBus}
import nova.core.util.RayTracer.RayTraceBlockResult

import scala.collection.convert.wrapAll._

/**
 * Handles laser interaction
 * @author Calclavia
 */
class WaveHandler(block: Block) extends Component {

	/**
	 * Called when the total energy due to incident waves changes
	 */
	var onPowerChange = new EventBus[Event]

	/**
	 * Called when the laser handler receives another laser
	 */
	var onReceive = new EventBus[Event]

	private var emittingLaser: Electromagnetic = _

	private var prevEnergy = -1d

	//Hook block events.
	block.unloadEvent.add(
		(evt: UnloadEvent) => {
			//Destroy laser
			if (emittingLaser != null) {
				WaveGrid(block.world).destroy(emittingLaser)
			}
		}
	)

	/**
	 * The current power being received
	 */
	def receivingPower =
		WaveGrid(block.world)
			.graph
			.vertexSet()
			.filter(_.hit.isInstanceOf[RayTraceBlockResult])
			.filter(_.hit.asInstanceOf[RayTraceBlockResult].block == block)
			.map(_.hitPower)
			.sum

	def receive(incident: Electromagnetic) {
		onReceive.publish(new Event)

		val power = receivingPower

		if (prevEnergy != power) {
			onPowerChange.publish(new Event)
			prevEnergy = power
		}
	}

	def emit(laser: Electromagnetic) {
		if (emittingLaser != null) {
			WaveGrid(block.world).destroy(emittingLaser)
		}

		WaveGrid(block.world).create(laser)
		emittingLaser = laser
	}
}
