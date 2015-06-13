package com.calclavia.edx.electric.grid

import com.calclavia.edx.electric.api.Electric
import nova.core.util.Profiler
import nova.internal.core.launch.NovaLauncher
import nova.scala.wrapper.FunctionalWrapper._
import nova.testutils.FakeBlock
import nova.wrappertests.NovaLauncherTestFactory
import org.assertj.core.api.Assertions._
import org.junit.{BeforeClass, Test}

import scala.collection.convert.wrapAll._

/**
 * @author Calclavia
 */
object ElectricGridTest {
	var launcher: NovaLauncher = _

	@BeforeClass
	def init {
		launcher = new NovaLauncherTestFactory().createLauncher
	}
}

class ElectricGridTest {

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

		val grid = new ElectricGrid

		val battery = new DummyComponent("Battery")
		val wire1 = new DummyWire("Wire 1")
		val resistor1 = new DummyComponent("Resistor 1")
		val wire2 = new DummyWire("Wire 2")

		battery.connectNeg(wire2)
		val components = connectInSeries(battery, wire1, resistor1, wire2)
		wire2.connect(battery)

		components.foreach(grid.add)
		profilerGen.end()

		val profilerAdj = new Profiler("Building adjacency for graph 1")
		grid.build()
		println(profilerAdj)

		val graph = grid.electricGraph

		ElectricGrid.exportGraph(graph, "testSolve1")

		//Test component & junction sizes
		assertThat(grid.components.size).isEqualTo(2)
		//There should be one less junction in the list, due to the ground
		assertThat(grid.junctions.size).isEqualTo(1)

		assertThat(graph.vertexSet.size).isEqualTo(4)
		assertThat(graph.edgeSet.size).isEqualTo(6)

		//Test edges connections
		assertThat(graph.containsEdge(grid.convert(battery), grid.convert(wire1))).isTrue
		assertThat(graph.containsEdge(grid.convert(wire1), grid.convert(resistor1))).isTrue
		assertThat(graph.containsEdge(grid.convert(resistor1), grid.convert(wire2))).isTrue
		assertThat(graph.containsEdge(grid.convert(wire2), grid.convert(battery))).isTrue

		assertThat(graph.containsEdge(grid.convert(wire1), grid.convert(battery))).isTrue
		assertThat(graph.containsEdge(grid.convert(wire2), grid.convert(resistor1))).isTrue

		val profiler = new Profiler("Solving graph 1")

		grid.enableThreading = false

		for (trial <- 1 to 1000) {
			val voltage = trial * 10d
			battery.generateVoltage(voltage)
			grid.update()

			//Test battery
			assertThat(battery.voltage).isCloseTo(voltage, within(error))
			assertThat(battery.current).isCloseTo(voltage, within(error))
			//Test resistor
			assertThat(resistor1.voltage).isCloseTo(voltage, within(error))
			assertThat(resistor1.current).isCloseTo(voltage, within(error))
			profiler.lap()
		}

		println("Average: " + profiler.average())
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
	 * Series circuit withPriority more than one node.
	 */
	@Test
	def testSolve2() {
		val profilerGen = new Profiler("Generate graph 2")

		val grid = new ElectricGrid

		val battery = new DummyComponent("Battery")
		val wire1 = new DummyWire("Wire 1")
		val wire2 = new DummyWire("Wire 2")
		val resistor1 = new DummyComponent("Resistor 1")
		val wire3 = new DummyWire("Wire 3")
		val resistor2 = new DummyComponent("Resistor 2")
		resistor2.setResistance(2)
		val wire4 = new DummyWire("Wire 4")

		battery.connectNeg(wire4)
		val components = connectInSeries(battery, wire1, wire2, resistor1, wire3, resistor2, wire4)
		wire4.connect(battery)

		components.foreach(grid.add)

		grid.build()
		profilerGen.end()

		ElectricGrid.exportGraph(grid.electricGraph, "testSolve2")

		//One junction became the ground
		assertThat(grid.junctions.size).isEqualTo(2)

		val profiler = new Profiler("Solving graph 2")
		grid.enableThreading = false

		for (trial <- 1 to 1000) {
			val voltage = trial * 10d
			battery.generateVoltage(voltage)
			grid.update()

			val current = voltage / 3d
			//Test battery
			assertThat(battery.voltage).isCloseTo(voltage, within(error))
			assertThat(battery.current).isCloseTo(current, within(error))
			//Test resistor1
			assertThat(resistor1.voltage).isCloseTo(voltage / 3d, within(error))
			assertThat(resistor1.current).isCloseTo(current, within(error))
			//Test resistor2
			assertThat(resistor2.voltage).isCloseTo(voltage * 2d / 3d, within(error))
			assertThat(resistor2.current).isCloseTo(current, within(error))
			profiler.lap()
		}

		println("Average: " + profiler.average())
	}

	/**
	 * Graph 3.
	 * Parallel circuit withPriority more than one node and employing virtual junctions.
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

		val grid = new ElectricGrid

		val battery = new DummyComponent("Battery 1")
		val wire1 = new DummyWire("Wire 1")
		val wire2 = new DummyWire("Wire 2")
		val resistor1 = new DummyComponent("Resistor 1")
		val wire3 = new DummyWire("Wire 3")
		val resistor2 = new DummyComponent("Resistor 2")
		resistor2.setResistance(2)
		val wire4 = new DummyWire("Wire 4")
		val resistor3 = new DummyComponent("Resistor 3")
		resistor3.setResistance(3)
		val resistor4 = new DummyComponent("Resistor 4")

		battery.connectNeg(wire4)
		val seriesA = connectInSeries(battery, wire1, wire2, resistor1, wire3, resistor2, wire4)
		wire4.connect(battery)
		val seriesB = connectInSeries(wire2, resistor3, resistor4, wire4)

		seriesA.foreach(grid.add)
		seriesB.foreach(grid.add)

		grid.build()
		println(profilerGen)

		ElectricGrid.exportGraph(grid.electricGraph, "testSolve3")

		val profiler = new Profiler("Solving graph 3")

		//Using 1/R = 1/R1+1/R2+...
		val totalResistance = 12 / 7d
		grid.enableThreading = false

		for (trial <- 1 to 1000) {
			val voltage = trial * 10d * Math.random()
			battery.generateVoltage(voltage)
			grid.update()

			//Test battery
			assertThat(battery.voltage).isCloseTo(voltage, within(error))
			assertThat(battery.current).isCloseTo(voltage / totalResistance, within(error))

			//Branch A:
			val currentA = voltage / 3d
			//Test resistor1
			assertThat(resistor1.voltage).isCloseTo(voltage / 3, within(error))
			assertThat(resistor1.current).isCloseTo(currentA, within(error))
			//Test resistor2
			assertThat(resistor2.voltage).isCloseTo(voltage * 2 / 3, within(error))
			assertThat(resistor2.current).isCloseTo(currentA, within(error))

			//Branch B:
			val currentB = voltage / 4d
			//Test resistor1
			assertThat(resistor4.voltage).isCloseTo(voltage / 4, within(error))
			assertThat(resistor4.current).isCloseTo(currentB, within(error))
			//Test resistor2
			assertThat(resistor3.voltage).isCloseTo(voltage * 3 / 4, within(error))
			assertThat(resistor3.current).isCloseTo(currentB, within(error))

			profiler.lap()
		}

		println(profiler.average)
	}

	/**
	 * A complex circuit withPriority multiple batteries
	 *
	 * |-|||- -|+ ------|
	 * |                |
	 * |----- -|+ --|||-|
	 * |                |
	 * |------||||------|
	 */
	@Test
	def testSolve4() {
		val profilerGen = new Profiler("Generate graph 4")

		val grid = new ElectricGrid

		val battery1 = new DummyComponent("Battery 1")
		val battery2 = new DummyComponent("Battery 2")
		val resistor1 = new DummyComponent("Resistor 1")
		val resistor2 = new DummyComponent("Resistor 2")
		resistor2.setResistance(2)
		val resistor3 = new DummyComponent("Resistor 3")
		resistor3.setResistance(2)

		val wire1 = new DummyWire("Wire 1")
		val wire2 = new DummyWire("Wire 2")
		val wire3 = new DummyWire("Wire 3")
		val wire4 = new DummyWire("Wire 4")

		battery1.connectPos(wire1)
		wire1.connect(battery1)
		battery1.connectNeg(wire2)
		wire2.connect(battery1)

		resistor1.connectNeg(wire2)
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

		grid.add(battery1)
		grid.add(battery2)
		grid.add(resistor1)
		grid.add(resistor2)
		grid.add(resistor3)
		grid.add(wire1)
		grid.add(wire2)
		grid.add(wire3)
		grid.add(wire4)

		grid.build()
		profilerGen.end()

		ElectricGrid.exportGraph(grid.electricGraph, "testSolve4")

		val profiler = new Profiler("Solving graph 3")
		grid.enableThreading = false

		for (trial <- 1 to 1000) {
			val voltage = trial * 10d
			battery1.generateVoltage(voltage)
			battery2.generateVoltage(voltage)
			grid.update()
			//TODO: Test results
			profiler.lap()
		}

		println(profiler.average)
	}

	/**
	 * Series circuit stress test.
	 * Test addRecursive
	 * Attempt to generate graphs withPriority more and more resistors.
	 */
	@Test
	def testSolve5() {
		println("Conducting stress test.")

		for (trial <- 100 to 2000 by 100) {

			val grid = new ElectricGrid
			grid.enableThreading = false
			val battery = new DummyComponent("Battery")
			val resistors = (0 until trial).map(i => new DummyComponent("Resistor " + i)).toList

			battery.connectNeg(resistors.last)
			connectInSeries(battery :: resistors: _*)
			resistors.last.connectPos(battery)

			grid.addRecursive(battery)

			val profilerGen = new Profiler("Generate graph withPriority " + trial + " resistors").start()
			grid.build()
			profilerGen.end()

			assertThat(grid.electricGraph.vertexSet().size()).isEqualTo((trial + 1) * 2)

			val voltage = trial * 10d * Math.random() + 0.1
			battery.generateVoltage(voltage)

			//ElectricGrid.exportGraph(graph.electricGraph, "Stress Test " + trial)

			val profiler = new Profiler("Solve circuit withPriority " + trial + " resistors").start()
			grid.update()
			profiler.end()

			val current = voltage / trial.toDouble

			//Test battery
			assertThat(battery.voltage).isCloseTo(voltage, within(error))
			assertThat(battery.current).isCloseTo(current, within(error))

			resistors.foreach(r => {
				//Test resistor1
				assertThat(r.voltage).isCloseTo(voltage / trial, within(error))
				assertThat(r.current).isCloseTo(current, within(error))
			})
		}
	}

	class DummyComponent(val name: String = "Component") extends NodeElectricComponent(new FakeBlock("dummy")) {
		var positivesCon = Set.empty[Electric]
		var negativesCon = Set.empty[Electric]

		connections = supplier(() => positivesCon ++ negativesCon)

		setPositiveConnections(() => positivesCon)
		setNegativeConnections(() => negativesCon)

		def connectPos(electric: Electric) {
			positivesCon += electric
		}

		def connectNeg(electric: Electric) {
			negativesCon += electric
		}

		override def toString: String = name
	}

	class DummyWire(val name: String = "Wire") extends NodeElectricJunction(new FakeBlock("dummy")) {
		var _connections = Set.empty[Electric]
		connections = supplier(() => _connections)

		def connect(electric: Electric) {
			_connections += electric
		}

		override def toString: String = name
	}

}
