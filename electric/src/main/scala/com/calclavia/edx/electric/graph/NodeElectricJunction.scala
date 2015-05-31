package com.calclavia.edx.electric.graph

import com.calclavia.edx.electric.graph.api.ElectricJunction
import com.calclavia.edx.electric.graph.component.Junction
import nova.core.block.Block

/**
 * Wires are getNodes in the grid that will not have different terminals, but instead can connect omni-directionally.
 * Wires will be treated as junctions and collapsed.
 * @author Calclavia
 */
class NodeElectricJunction(parent: Block) extends NodeElectric(parent) with ElectricJunction {

	var junction: Junction = null

	override def current: Double = voltage * voltage / resistance

	override def voltage: Double = junction.voltage

	override def toString: String =
		if (junction != null)
			"ElectricJunction [" + BigDecimal(voltage).setScale(2, BigDecimal.RoundingMode.HALF_UP) + "V]"
		else
			"ElectricJunction"
}
