package com.calclavia.edx.electric.grid

import com.calclavia.edx.electric.api.Electric
import com.calclavia.edx.electric.api.Electric.ElectricChangeEvent
import com.resonant.lib.WrapFunctions._
import nova.core.block.Block.NeighborChangeEvent
import nova.core.block.Stateful.{LoadEvent, UnloadEvent}
import nova.core.game.Game
import nova.core.network.NetworkTarget.Side

import scala.collection.convert.wrapAll._

/**
 * A class extended by all electric nodes.
 * @author Calclavia
 */
trait ElectricLike extends Electric with BlockConnectable[Electric] {

	private var _resistance = 1d

	//Hook block events.
	block.loadEvent.add(
		(evt: LoadEvent) => {
			//Wait for next tick
			if (Side.get().isServer) {
				Game.syncTicker().preQueue(() => build())
			}
		}
	)
	block.unloadEvent.add(
		(evt: UnloadEvent) => {
			ElectricGrid.destroy(this)
		}
	)

	block.neighborChangeEvent.add((evt: NeighborChangeEvent) => rebuild())

	def rebuild() {
		ElectricGrid.destroy(this)
		build()
	}

	def build() {
		ElectricGrid(this).addRecursive(this).build()
	}

	override def resistance = _resistance

	def resistance_=(res: Double) {
		_resistance = res
		onResistanceChange.publish(new ElectricChangeEvent)
	}

	override def setResistance(resistance: Double): Electric = {
		_resistance = resistance
		this
	}

	def con: Set[Electric] = connections.get().toSet
}
