package com.calclavia.edx.electric.graph

import java.util.function.Supplier
import java.util.{Set => JSet}

import com.calclavia.edx.electric.graph.api.{Electric, ElectricComponent}
import nova.core.block.Block

import scala.collection.convert.wrapAll._

/**
 * Represents an electric component in a circuit.
 *
 * An electric component must be in between two junctions in order to function.
 *
 * Flow of current should be positive when current from junction A is flowing to B
 *
 * @author Calclavia
 */
class NodeElectricComponent(parent: Block) extends NodeElectric(parent) with ElectricComponent {

	/**
	 * The current and voltage values are set are determined by the DC Grid
	 */
	var voltage = 0d

	var current = 0d

	/**
	 * Variables to keep voltage source states
	 */
	protected[graph] var genVoltage = 0d
	protected[graph] var genCurrent = 0d
	protected[graph] var onSetVoltage = Seq.empty[Electric => Unit]
	protected[graph] var onSetCurrent = Seq.empty[Electric => Unit]

	/**
	 * The positive terminal connections
	 */
	protected[graph] var positiveConnections = () => Set.empty[Electric]

	/**
	 * The negative terminal connections
	 */
	protected[graph] var negativeConnections = () => Set.empty[Electric]

	override def setPositiveConnections(supplier: Supplier[JSet[Electric]]) {
		positiveConnections = () => supplier.get().toSet
	}

	override def setNegativeConnections(supplier: Supplier[JSet[Electric]]) {
		negativeConnections = () => supplier.get().toSet
	}

	def setPositiveConnections(supplier: () => Set[Electric]) {
		positiveConnections = supplier
	}

	def setNegativeConnections(supplier: () => Set[Electric]) {
		negativeConnections = supplier
	}

	/**
	 * Generates a potential difference across the two intersections that go across this node.
	 * @param voltage - The target voltage, in Volts
	 */
	def setVoltage(voltage: Double) {
		genVoltage = voltage
		onSetVoltage.foreach(_.apply(this))
	}

	/**
	 * Generates power by adjusting varying the voltage until the target power is reached
	 * @param power - The target power, in Watts
	 */
	def setCurrent(power: Double) {
		genCurrent = power
		onSetCurrent.foreach(_.apply(this))
	}

	override def toString = "ElectricComponent [" + connections.size() + " " + BigDecimal(current).setScale(2, BigDecimal.RoundingMode.HALF_UP) + "A " + BigDecimal(voltage).setScale(2, BigDecimal.RoundingMode.HALF_UP) + "V]"
}
