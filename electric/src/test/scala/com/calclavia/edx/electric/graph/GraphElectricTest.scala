package com.calclavia.edx.electric.graph

import java.util

import com.calclavia.edx.electric.graph.api.Electric
import nova.core.util.Profiler
import nova.testutils.FakeBlock
import org.junit.Assert._
import org.junit.Test

import scala.collection.convert.wrapAll._

/**
 * @author Calclavia
 */
class GraphElectricTest {

	val error = 0.001

	/**
	 * Simplest circuit.
	 * Testing adjacency matrix and voltage at each resistor.
	 */
	@Test
	def testSolve1() {
		/**
		 * The most simple circuit
		 */
		val profilerGen = new Profiler("Generate graph 1")

		val graph = new ElectricGrid

		val battery = new DummyComponent()
		val wire1 = new DummyWire()
		val resistor1 = new DummyComponent()
		val wire2 = new DummyWire()

		battery.connectNeg(wire2)
		val components = connectInSeries(battery, wire1, resistor1, wire2)
		wire2.connect(battery)

		components.foreach(graph.add)
		println(profilerGen)

		val profilerAdj = new Profiler("Building adjacency for graph 1")
		graph.buildAll()
		println(profilerAdj)

		println(graph.adjMat)
		//Test component & junction sizes
		assertEquals(2, graph.components.size)
		//There should be one less junction in the list, due to the ground
		assertEquals(1, graph.junctions.size)
		//Test forward connections
		assertEquals(true, graph.adjMat(battery, wire1))
		assertEquals(true, graph.adjMat(wire1, resistor1))
		assertEquals(true, graph.adjMat(resistor1, wire2))
		assertEquals(true, graph.adjMat(wire2, battery))
		//Test getDirectedTo connections
		assertEquals(Set(wire1, wire2), graph.adjMat.getDirectedTo(battery))
		assertEquals(Set(battery), graph.adjMat.getDirectedTo(wire1))
		assertEquals(Set(wire1, wire2), graph.adjMat.getDirectedTo(resistor1))
		assertEquals(Set(resistor1), graph.adjMat.getDirectedTo(wire2))
		//Test getDirectedFrom connections
		assertEquals(Set(wire1), graph.adjMat.getDirectedFrom(battery))
		assertEquals(Set(battery, resistor1), graph.adjMat.getDirectedFrom(wire1))
		assertEquals(Set(wire2), graph.adjMat.getDirectedFrom(resistor1))
		assertEquals(Set(resistor1, battery), graph.adjMat.getDirectedFrom(wire2))

		val profiler = new Profiler("Solving graph 1")

		for (trial <- 1 to 1000) {
			val voltage = trial * 10d * Math.random()
			battery.setVoltage(voltage)
			graph.update(profiler.elapsed)

			//Test battery
			assertEquals(voltage, battery.voltage, error)
			assertEquals(voltage, battery.current, error)
			//Test resistor
			assertEquals(voltage, resistor1.voltage, error)
			assertEquals(voltage, resistor1.current, error)
			profiler.lap()
		}

		println(profiler.average())
	}

	/**
	 * Connects a sequence of electric nodes in series excluding the first and last connection.
	 */
	def connectInSeries(series: Electric*): Seq[Electric] = {
		series.zipWithIndex.foreach {
			case (component: DummyComponent, index) =>
				index match {
					case 0 => component.connectPos(series(index + 1))
					case l if l == series.size - 1 =>
						component.connectNeg(series(index - 1))
					case _ =>
						component.connectNeg(series(index - 1))
						component.connectPos(series(index + 1))
				}
			case (wire: DummyWire, index) =>
				index match {
					case 0 => wire.connect(series(index + 1))
					case l if l == series.size - 1 =>
						wire.connect(series(index - 1))
					case _ =>
						wire.connect(series(index - 1))
						wire.connect(series(index + 1))
				}
		}
		return series
	}

	/**
	 * Graph 2.
	 * Series circuit with more than one node.
	 */
	@Test
	def testSolve2() {
		val profilerGen = new Profiler("Generate graph 2")

		val graph = new ElectricGrid

		val battery = new DummyComponent()
		val wire1 = new DummyWire()
		val wire2 = new DummyWire()
		val resistor1 = new DummyComponent()
		val wire3 = new DummyWire()
		val resistor2 = new DummyComponent()
		resistor2.setResistance(2)
		val wire4 = new DummyWire()

		battery.connectNeg(wire4)
		val components = connectInSeries(battery, wire1, wire2, resistor1, wire3, resistor2, wire4)
		wire4.connect(battery)

		components.foreach(graph.add)
		println(profilerGen)

		graph.buildAll()
		val profiler = new Profiler("Solving graph 2")

		for (trial <- 1 to 1000) {
			val voltage = trial * 10d * Math.random()
			battery.setVoltage(voltage)
			graph.update(profiler.elapsed)

			val current = voltage / 3d
			//Test battery
			assertEquals(voltage, battery.voltage, error)
			assertEquals(current, battery.current, error)
			//Test resistor1
			assertEquals(voltage / 3, resistor1.voltage, error)
			assertEquals(current, resistor1.current, error)
			//Test resistor2
			assertEquals(voltage * 2 / 3, resistor2.voltage, error)
			assertEquals(current, resistor2.current, error)
			profiler.lap()
		}

		print(profiler.average())
	}

	/**
	 * Graph 3.
	 * Parallel circuit with more than one node and employing virtual junctions.
	 * |-- -|+ ---|
	 * |          |
	 * |--||---||-|
	 * |          |
	 * |---||-||--|
	 */
	//TODO: Make string to circuit converter. :)
	@Test
	def testSolve3() {
		val profilerGen = new Profiler("Generate graph 3")

		val graph = new ElectricGrid

		val battery = new DummyComponent()
		val wire1 = new DummyWire()
		val wire2 = new DummyWire()
		val resistor1 = new DummyComponent()
		val wire3 = new DummyWire()
		val resistor2 = new DummyComponent()
		resistor2.setResistance(2)
		val wire4 = new DummyWire()
		val resistor3 = new DummyComponent()
		resistor3.setResistance(3)
		val resistor4 = new DummyComponent()

		battery.connectNeg(wire4)
		val seriesA = connectInSeries(battery, wire1, wire2, resistor1, wire3, resistor2, wire4)
		wire4.connect(battery)
		val seriesB = connectInSeries(wire2, resistor3, resistor4, wire4)

		seriesA.foreach(graph.add)
		seriesB.foreach(graph.add)

		println(profilerGen)

		graph.buildAll()
		val profiler = new Profiler("Solving graph 3")

		//Using 1/R = 1/R1+1/R2+...
		val totalResistance = 12 / 7d

		for (trial <- 1 to 1000) {
			val voltage = trial * 10d * Math.random()
			battery.setVoltage(voltage)
			graph.update(profiler.elapsed)

			//Test battery
			assertEquals(voltage, battery.voltage, error)
			assertEquals(voltage / totalResistance, battery.current, error)

			//Branch A:
			val currentA = voltage / 3d
			//Test resistor1
			assertEquals(voltage / 3, resistor1.voltage, error)
			assertEquals(currentA, resistor1.current, error)
			//Test resistor2
			assertEquals(voltage * 2 / 3, resistor2.voltage, error)
			assertEquals(currentA, resistor2.current, error)

			//Branch B:
			val currentB = voltage / 4d
			//Test resistor1
			assertEquals(voltage / 4, resistor4.voltage, error)
			assertEquals(currentB, resistor4.current, error)
			//Test resistor2
			assertEquals(voltage * 3 / 4, resistor3.voltage, error)
			assertEquals(currentB, resistor3.current, error)

			profiler.lap()
		}

		println(profiler.average)
	}

	/**
	 * A complex circuit with multiple batteries
	 *
	 * |-|||- -|+ ------|
	 * |                |
	 * |----- -|+ --|||-|
	 * |                |
	 * |------||||------|
	 */
	@Test
	def testSolve4() {
		val profilerGen = new Profiler("Generate graph 3")

		val graph = new ElectricGrid

		val battery1 = new DummyComponent()
		val battery2 = new DummyComponent()
		val resistor1 = new DummyComponent()
		val resistor2 = new DummyComponent()
		resistor2.setResistance(2)
		val resistor3 = new DummyComponent()
		resistor3.setResistance(2)

		val wire1 = new DummyWire()
		val wire2 = new DummyWire()
		val wire3 = new DummyWire()
		val wire4 = new DummyWire()

		battery1.connectPos(wire1)
		wire1.connect(battery1)
		battery1.connectNeg(wire2)
		wire2.connect(battery1)

		resistor1.connectNeg(wire1)
		wire2.connect(resistor1)
		resistor1.connectPos(wire3)
		wire3.connect(resistor1)

		resistor2.connectPos(wire4)
		wire4.connect(resistor2)
		resistor2.connectNeg(wire1)
		wire1.connect(resistor2)

		battery2.connectPos(wire4)
		wire4.connect(battery2)
		battery2.connectNeg(wire3)
		wire3.connect(battery2)

		resistor3.connectPos(wire3)
		wire3.connect(resistor3)
		resistor3.connectNeg(wire1)
		wire1.connect(resistor3)

		graph.add(battery1)
		graph.add(battery2)
		graph.add(resistor1)
		graph.add(resistor2)
		graph.add(resistor3)
		graph.add(wire1)
		graph.add(wire2)
		graph.add(wire3)
		graph.add(wire4)

		println(profilerGen)

		graph.buildAll()
		val profiler = new Profiler("Solving graph 3")

		for (trial <- 1 to 1000) {
			val voltage = trial * 10d * Math.random()
			battery1.setVoltage(voltage)
			battery2.setVoltage(voltage)
			graph.update(profiler.elapsed)
			//TODO: Test results
			profiler.lap()
		}

		println(profiler.average)
	}

	/**
	 * Series circuit stress test.
	 * Attempt to generate graphs with more and more resistors.
	 */
	@Test
	def testSolve5() {
		println("Conducting stress test.")

		for (trial <- 2 to 1000) {

			val graph = new ElectricGrid
			val battery = new DummyComponent()
			val resistors = (0 until trial).map(i => new DummyComponent()).toList

			battery.connectNeg(resistors.last)
			val components = connectInSeries(battery :: resistors: _*)
			resistors.last.connectPos(battery)

			components.foreach(graph.add)

			val profilerGen = new Profiler("Generate graph with " + trial + " resistors")
			graph.buildAll()
			println(profilerGen)

			val voltage = trial * 10d * Math.random() + 0.1
			battery.setVoltage(voltage)

			val profiler = new Profiler("Solve circuit with " + trial + " resistors")
			graph.update(profiler.elapsed)
			println(profiler)

			val current = voltage / trial.toDouble

			//Test battery
			assertEquals(voltage, battery.voltage, error)
			assertEquals(current, battery.current, error)

			resistors.foreach(r => {
				//Test resistor1
				assertEquals(voltage / trial, r.voltage, error)
				assertEquals(current, r.current, error)
			})
		}
	}

	class DummyComponent extends NodeElectricComponent(new FakeBlock("dummy")) {
		var positives = Set.empty[Electric]
		var negatives = Set.empty[Electric]

		setPositiveConnections(() => positives)
		setNegativeConnections(() => negatives)

		def connectPos(electric: Electric) {
			positives += electric
		}

		def connectNeg(electric: Electric) {
			negatives += electric
		}

		override def con: util.Set[Electric] = positives ++ negatives
	}

	class DummyWire extends NodeElectricJunction(new FakeBlock("dummy")) {

		var _connections = Set.empty[Electric]

		def connect(electric: Electric) {
			_connections += electric
		}

		override def con: util.Set[Electric] = _connections
	}

}
