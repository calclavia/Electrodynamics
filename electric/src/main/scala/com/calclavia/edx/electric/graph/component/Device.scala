package com.calclavia.edx.electric.graph.component

import com.calclavia.edx.electric.graph.NodeElectricComponent

/**
 * @author Calclavia
 */
class Device(val component: NodeElectricComponent) extends ElectricElement {
	override def hashCode = component.hashCode()
}
