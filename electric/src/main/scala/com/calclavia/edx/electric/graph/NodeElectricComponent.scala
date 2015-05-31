package com.calclavia.edx.electric.graph

import java.util.{Set => JSet}

import com.calclavia.graph.api.energy.NodeElectric
import com.resonant.wrapper.lib.wrapper.BitmaskWrapper._
import nova.core.block.Block
import nova.core.util.Direction

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
class NodeElectricComponent(parent: Block) extends com.calclavia.graph.api.energy.NodeElectricComponent(parent) with TraitElectric {

	override protected val block: Block = parent

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
	protected[graph] var onSetVoltage = Seq.empty[NodeElectric => Unit]
	protected[graph] var onSetCurrent = Seq.empty[NodeElectric => Unit]

	/**
	 * The positive terminals are the directions in which charge can flow out of this electric component.
	 * Positive and negative terminals must be mutually exclusive.
	 *
	 * The mask is a 6 bit data each storing a specific side value
	 */
	private var positiveMask = 0
	/**
	 * The negative terminals are the directions in which charge can flow into this electric component.
	 * Positive and negative terminals must be mutually exclusive.
	 *
	 * The mask is a 6 bit data each storing a specific side value
	 */
	private var negativeMask = 0

	def positives: JSet[NodeElectric] = connectedMap.filter(keyVal => positiveMask.mask(keyVal._2)).keySet

	def negatives: JSet[NodeElectric] = connectedMap.filter(keyVal => negativeMask.mask(keyVal._2)).keySet

	def setPositive(dir: Direction, open: Boolean = true) {
		positiveMask = positiveMask.mask(dir, open)
		negativeMask &= ~positiveMask
		connectionMask = positiveMask | negativeMask
	}

	def setNegative(dir: Direction, open: Boolean = true) {
		negativeMask = negativeMask.mask(dir, open)
		positiveMask &= ~negativeMask
		connectionMask = positiveMask | negativeMask
	}

	override def setPositives(dirs: Direction*) {
		positiveMask = 0

		dirs.foreach(dir => positiveMask = positiveMask.mask(dir, true))
		negativeMask &= ~positiveMask
		connectionMask = positiveMask | negativeMask
	}

	override def setNegatives(dirs: Direction*) {
		negativeMask = 0

		dirs.foreach(dir => negativeMask = negativeMask.mask(dir, true))
		positiveMask &= ~negativeMask
		connectionMask = positiveMask | negativeMask
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
