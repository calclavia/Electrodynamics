package com.calclavia.edx.electric.graph

import com.calclavia.edx.electric.graph.api.Electric
import com.calclavia.graph.node.BlockConnectable
import com.resonant.lib.WrapFunctions
import WrapFunctions._
import nova.core.block.Block
import nova.core.block.Stateful.LoadEvent
import nova.core.component.Component
import nova.core.network.NetworkTarget.Side

/**
 * A class extended by all electric nodes.
 * @author Calclavia
 */
abstract class NodeElectric(val provider: Block) extends Component with BlockConnectable[Electric] with Electric {

	protected[graph] var onResistanceChange = Seq.empty[(Electric) => Unit]

	private var _resistance = 1d

	private var graph: ElectricGrid = _

	//Hook block events.
	provider.loadEvent.add((evt: LoadEvent) => {
		if (Side.get().isServer && graph == null) {
			resetGraph()
		}
	})

	//TODO: We are calling connections() twice. Inefficient!
	def resetGraph() {
		graph = new ElectricGrid
		val all = graph.findAll(this)
		all.foreach(node => {
			graph.add(node)
			node.asInstanceOf[NodeElectric].graph = graph
		})
		graph.build()
	}

	def resistance = _resistance

	def resistance_=(res: Double) {
		_resistance = res
		onResistanceChange.foreach(_.apply(this))
	}

	def getResistance = _resistance

	def setResistance(res: Double) = _resistance = res

	override protected def block: Block = this.provider
}
