package com.calclavia.edx.electric.graph

import com.calclavia.graph.api.energy.NodeElectric
import com.calclavia.graph.core.base.NodeBlockConnect
import com.resonant.wrapper.core.api.tile.DebugInfo

import scala.collection.convert.wrapAll._

/**
 * @author Calclavia
 */
trait TraitElectric extends NodeElectric with NodeBlockConnect[NodeElectric] {

	protected[graph] var onResistanceChange = Seq.empty[(NodeElectric) => Unit]

	private var _resistance = 1d

	//Hook block events.

	def resistance = _resistance

	def resistance_=(res: Double) {
		_resistance = res
		onResistanceChange.foreach(_.apply(this))
	}

	def getResistance = _resistance

	def setResistance(res: Double) = _resistance = res
}
