package com.calclavia.edx.electric.grid

import com.calclavia.edx.electric.grid.api.ElectricJunction
import nova.core.block.Block

/**
 * Wires are getNodes in the grid that will not have different terminals, but instead can connect omni-directionally.
 * Wires will be treated as junctions and collapsed.
 * @author Calclavia
 */
class NodeElectricJunction(parent: Block) extends ElectricJunction with ElectricLike {

	protected[grid] var _voltage = 0d

	override def current: Double = voltage * voltage / resistance

	override def toString: String = "ElectricJunction [" + BigDecimal(voltage).setScale(2, BigDecimal.RoundingMode.HALF_UP) + "V]"

	override def voltage: Double = _voltage

	override protected def block: Block = parent
}
