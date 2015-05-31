package com.calclavia.edx.electric.graph

import com.calclavia.edx.electric.graph.api.Electric
import com.calclavia.graph.core.base.NodeBlockConnect
import com.resonant.lib.wrapper.WrapFunctions._
import nova.core.block.Block
import nova.core.block.Stateful.LoadEvent
import nova.core.component.Component

/**
 * A class extended by all electric nodes.
 * @author Calclavia
 */
abstract class NodeElectric(val provider: Block) extends Component with NodeBlockConnect[Electric] with Electric {

	protected[graph] var onResistanceChange = Seq.empty[(Electric) => Unit]

	private var _resistance = 1d

	private var graph: GraphElectric = _

	//Hook block events.
	provider.loadEvent.add((evt: LoadEvent) => {
		if (graph == null) {
			resetGraph()
		}
	})

	//TODO: We are calling connections() twice. Inefficient!
	def resetGraph() {
		graph = new GraphElectric
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
