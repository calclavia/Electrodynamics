package com.calclavia.edx.electric.grid

import java.util.function.Supplier
import java.util.{Set => JSet}

import com.calclavia.edx.electric.grid.api.{Electric, ElectricComponent}
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
class NodeElectricComponent(parent: Block) extends ElectricComponent with ElectricLike {

	/**
	 * The current and voltage values are set are determined by the DC Grid
	 */
	var voltage = 0d

	var current = 0d

	/**
	 * Variables to keep voltage source states
	 */
	protected[grid] var genVoltage = 0d
	protected[grid] var genCurrent = 0d
	protected[grid] var onSetVoltage = Seq.empty[Electric => Unit]
	protected[grid] var onSetCurrent = Seq.empty[Electric => Unit]

	/**
	 * The positive terminal connections
	 */
	private var positiveConnections = () => Set.empty[Electric]

	/**
	 * The negative terminal connections
	 */
	private var negativeConnections = () => Set.empty[Electric]

	override def positives(): JSet[Electric] = positiveConnections()

	override def negatives(): JSet[Electric] = negativeConnections()

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
	override def generateVoltage(voltage: Double) {
		genVoltage = voltage
		onSetVoltage.foreach(_.apply(this))
	}

	/**
	 * Generates power by adjusting varying the voltage until the target power is reached
	 * @param power - The target power, in Watts
	 */
	override def generateCurrent(power: Double) {
		genCurrent = power
		onSetCurrent.foreach(_.apply(this))
	}

	override def toString = "ElectricComponent [" + con.size + " " + BigDecimal(current).setScale(2, BigDecimal.RoundingMode.HALF_UP) + "A " + BigDecimal(voltage).setScale(2, BigDecimal.RoundingMode.HALF_UP) + "V]"

	override protected def block: Block = parent
}
