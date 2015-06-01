package com.calclavia.edx.electric.grid

import com.calclavia.edx.electric.grid.api.Electric
import com.resonant.lib.WrapFunctions._
import nova.core.block.Stateful.LoadEvent
import nova.core.block.component.Connectable
import nova.core.network.NetworkTarget.Side
import scala.collection.convert.wrapAll._

/**
 * A class extended by all electric nodes.
 * @author Calclavia
 */
trait ElectricLike extends Electric with BlockConnectable[Electric] {

	//Internal use for graphs
	protected[grid] var onResistanceChange = Seq.empty[(Electric) => Unit]

	private var _resistance = 1d

	//Hook block events.
	block.loadEvent.add((evt: LoadEvent) => {
		if (Side.get().isServer) {
			val grid = ElectricGrid(world)
			if (grid.has(this))
				grid.addRecursive(this).build()
		}
	})

	def resistance = _resistance

	def resistance_=(res: Double) {
		_resistance = res
		onResistanceChange.foreach(_.apply(this))
	}

	def getResistance = _resistance

	def setResistance(res: Double) = _resistance = res

	def con: Set[Electric] = connections.get().toSet
}
