package com.calclavia.edx.electric.graph

import com.calclavia.graph.api.energy.NodeElectric
import com.calclavia.graph.core.base.NodeBlockConnect
import com.resonant.wrapper.core.api.tile.DebugInfo

import scala.collection.convert.wrapAll._

/**
 * @author Calclavia
 */
trait NodeAbstractElectric extends NodeElectric with NodeBlockConnect[NodeElectric] with DebugInfo {

	protected[graph] var onResistanceChange = Seq.empty[(NodeElectric) => Unit]
	private var _resistance = 1d

	def resistance = _resistance

	def resistance_=(res: Double) {
		_resistance = res
		onResistanceChange.foreach(_.apply(this))
	}

	def getResistance = _resistance

	def setResistance(res: Double) = _resistance = res

	override def getDebugInfo = List(toString)
}
