package com.calclavia.edx.electric.grid

import java.util
import java.util.Collections

import com.calclavia.edx.electric.api.Electric.ElectricChangeEvent
import com.calclavia.edx.electric.api.{Electric, ElectricComponent}
import com.resonant.lib.WrapFunctions._
import nova.core.util.transform.matrix.Matrix
import nova.scala.ExtendedUpdater
import org.jgrapht.alg.CycleDetector
import org.jgrapht.graph.{DefaultDirectedGraph, DefaultEdge, SimpleGraph}

import scala.collection.convert.wrapAll._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.util.{Failure, Success}
/**
 * An electric circuit grid for independent voltage sources.
 * The circuit solver uses MNA, based on http://www.swarthmore.edu/NatSci/echeeve1/Ref/mna/MNA3.html
 * We will be solving systems of linear equations using matrices.
 *
 * @author Calclavia
 */
object ElectricGrid {
	private val grids = Collections.newSetFromMap[ElectricGrid](new util.WeakHashMap)

	def apply(electric: Electric): ElectricGrid = {
		//TODO: This may be VERY inefficient
		val find = grids.find(grid => grid.connectionGraph.vertexSet().contains(electric))

		if (find.isDefined)
			return find.get

		val grid = new ElectricGrid
		grids += grid
		return grid
	}

	def destroy(electric: Electric) {
		grids.removeAll(grids.filter(grid => grid.has(electric)))
	}

	/**
	 * An element in the electric grid
	 * @author Calclavia
	 */
	class ElectricElement {

	}

	/**
	 * A component
	 * @author Calclavia
	 */
	class Component(val component: NodeElectricComponent) extends ElectricElement {

		override def equals(obj: scala.Any): Boolean = {
			if (obj.isInstanceOf[Component]) {
				return obj.asInstanceOf[Component].component.equals(component)
			}

			return false
		}

		override def hashCode = component.hashCode()

		override def toString: String = "Component_" + component.toString.replaceAll(" ", "_")
	}

	/**
	 * Essentially a wrapper to collapse wires into junctions for easy management.
	 * @author Calclavia
	 */
	class Junction extends ElectricElement {
		/**
		 * The wires that collapsed into this junction
		 */
		var wires = Set.empty[NodeElectricJunction]
		/**
		 * The electric potential at this junction.
		 */
		private var _voltage = 0d

		def voltage = _voltage

		def voltage_=(newVoltage: Double) {
			_voltage = newVoltage
			wires.foreach(_._voltage = _voltage)
		}

		/**
		 * The total resistance of this junction due to wires
		 */
		def resistance = wires.map(_.resistance).sum

		override def toString: String = "Junction_" + wires.mkString.replaceAll(" ", "_")
	}

	/**
	 * A virtual junction is a junction that does not exist in the world, but exists in the graph space.
	 * @author Calclavia
	 */
	class VirtualJunction extends Junction {
		override def toString: String = "Virtual_Junction_" + hashCode()
	}

}

class ElectricGrid extends ExtendedUpdater {

	import ElectricGrid._

	//The general connection graph between electric
	protected val connectionGraph = new SimpleGraph[Electric, DefaultEdge](classOf[DefaultEdge])
	// There should always at least (node.size - 1) amount of junctions.
	var junctions = List.empty[Junction]
	//The reference ground junction where voltage is zero.
	var ground: Junction = null
	//The components in the circuit
	var components = List.empty[Component]
	//The modified nodal analysis matrix (A) in Ax=b linear equation.
	var mna: Matrix = null
	var voltageSources = List.empty[Component]
	var currentSources = List.empty[Component]
	var resistors = List.empty[Component]
	//Changed flags
	var resistorChanged = false
	var sourceChanged = false
	//The source matrix (B)
	protected[grid] var sourceMatrix: Matrix = null
	//The graph of all electric elements. In this directed graph the arrow points from positive to negative in potential difference.
	protected[grid] var electricGraph: DefaultDirectedGraph[ElectricElement, DefaultEdge] = null

	/**
	 * Finds all the nodes that are interconnected
	 * @param node The node to being with
	 * @param builder The accumulator
	 * @return A map of nodes with their connections
	 */
	def findAll(node: Electric, builder: Set[Electric] = Set.empty): Map[Electric, Set[Electric]] =
		node match {
			case component: ElectricComponent =>
				val connections = (component.positives ++ component.negatives).toSet
				val electrics = connections diff builder
				electrics.flatMap(n => findAll(n, builder + node)).toMap + (node -> connections)
			case junction: NodeElectricJunction =>
				val connections = junction.con
				(connections diff builder).flatMap(n => findAll(n, builder + node)).toMap + (node -> connections)
		}

	def addRecursive(node: Electric): this.type = {
		findAll(node).foreach {
			case (node, con) => add(node, con)
		}
		return this
	}

	def add(node: Electric): this.type =
		add(node,
			node match {
				case node: NodeElectricComponent =>
					(node.positives() ++ node.negatives()).toSet
				case node: NodeElectricJunction =>
					node.con
			}
		)

	def add(node: Electric, connections: Set[Electric]): this.type = {
		connectionGraph.addVertex(node)

		//TODO: Can't we build the electric graph from here?
		node match {
			case node: NodeElectricComponent =>
				connections.foreach(connectionGraph.addVertex)
				connections.foreach(n => connectionGraph.addEdge(node, n))

				node.onResistanceChange.add((evt: ElectricChangeEvent) => {
					resistorChanged = true
					requestUpdate()
				})
				node.onInternalVoltageChange :+= ((source: Electric) => {
					sourceChanged = true
					requestUpdate()
				})
				node.onInternalCurrentChange :+= ((source: Electric) => {
					sourceChanged = true
					requestUpdate()
				})
			case node: NodeElectricJunction =>
				connections.foreach(connectionGraph.addVertex(_))
		}
		return this
	}

	def has(node: Electric) = connectionGraph.vertexSet().contains(node)

	/**
	 * Converts a node electric into an electric element
	 */
	def convert(nodeElectric: NodeElectricComponent) = components.find(c => c.component.equals(nodeElectric)).getOrElse(new Component(nodeElectric))

	/**
	 * Creates or gets an existing Junction
	 */
	def convert(nodeJunction: NodeElectricJunction): Junction = {
		val find = junctions.find(j => j.wires.contains(nodeJunction))

		if (find.isDefined)
			return find.get

		if (ground != null && ground.wires.contains(nodeJunction))
			return ground

		val newJunction = new Junction
		newJunction.wires += nodeJunction
		junctions :+= newJunction
		return newJunction
	}

	/**
	 * Builds the MNA matrix and prepares graph for simulation
	 */
	def build() {
		/**
		 * Clean all variables
		 */
		junctions = List.empty
		components = List.empty
		ground = null
		mna = null
		sourceMatrix = null
		electricGraph = new DefaultDirectedGraph[ElectricElement, DefaultEdge](classOf[DefaultEdge])

		/**
		 * Builds the electric graph.
		 * The directed graph indicate current flow from positive terminal to negative terminal.
		 */
		connectionGraph.vertexSet().foreach {
			case nodeComponent: NodeElectricComponent =>
				val component = new Component(nodeComponent)
				//Check positive terminal connections
				connectionGraph
					.edgesOf(nodeComponent)
					.map(connectionGraph.getEdgeTarget)
					.foreach {
					case checkNodeComponent: NodeElectricComponent =>
						//Check if the "component" is negatively connected to the current node
						if (checkNodeComponent.negatives().contains(nodeComponent)) {
							val junction = new VirtualJunction
							val checkComponent = convert(checkNodeComponent)
							electricGraph.addVertex(component)
							electricGraph.addVertex(junction)
							electricGraph.addVertex(checkComponent)

							electricGraph.addEdge(component, junction)
							electricGraph.addEdge(junction, checkComponent)
							electricGraph.addEdge(junction, component)
							junctions :+= junction
						}
					case nodeJunction: NodeElectricJunction =>
						val junction = convert(nodeJunction)
						electricGraph.addVertex(component)
						electricGraph.addVertex(junction)
						electricGraph.addEdge(component, junction)
				}
				components :+= component
			case nodeJunction: NodeElectricJunction =>
				val junction = convert(nodeJunction)
				electricGraph.addVertex(junction)

				//Connect the junction to all of this NodeElectricJunction's nodes
				nodeJunction.con.foreach {
					case nodeComponent: NodeElectricComponent =>
						val component = convert(nodeComponent)
						electricGraph.addVertex(component)
						electricGraph.addEdge(junction, component)
					case nodeJunction: NodeElectricJunction =>
						junction.wires += nodeJunction
				}

			//TODO: Create virtual junctions with resistors to simulate wire resistance
		}

		if (electricGraph.vertexSet().size() > 0) {
			//Select reference ground
			ground = junctions.head
			junctions = junctions.splitAt(1)._2
			ground.voltage = 0

			println("Built grid successfully")
			requestUpdate()
		}
	}

	var updateFuture: Future[Unit] = _

	def requestUpdate() {
		if (updateFuture == null || updateFuture.isCompleted) {
			updateFuture = update()
			updateFuture.onComplete {
				case Success(nothing) => println("Circuit solved")
				case Failure(ex) => println("Circuit failed: " + ex.printStackTrace)
			}
		}
		else {
			//	updateFuture.onComplete(f => requestUpdate())
		}
	}

	def update(): Future[Unit] = Future {
		electricGraph.synchronized {
			val detector = new CycleDetector(electricGraph)

			if (junctions.nonEmpty && detector.detectCycles()) {
				println("Solving circuit...")
				if (mna == null) {
					setupMNA()
					generateConnectionMatrix()
					resistorChanged = true
					sourceChanged = true
				}

				if (resistorChanged) {
					generateConductanceMatrix()
				}

				if (sourceChanged) {
					computeSourceMatrix()
				}

				if (resistorChanged || sourceChanged) {
					solve()
				}

				resistorChanged = false
				sourceChanged = false
			}
		}
	}

	/**
	 * Setup MNA Matrix.
	 * This should be called if the number of voltage sources changes.
	 */

	def setupMNA() {
		voltageSources = components.collect { case source if source.component.genVoltage != 0 => source }
		currentSources = components.collect { case source if source.component.genCurrent != 0 => source }
		resistors = components diff voltageSources diff currentSources
		mna = new Matrix(voltageSources.size + junctions.size)
	}

	/**
	 * Construct B nxm and C mxn sub-matrix, with only 0, 1, and -1 elements.
	 * The C matrix is the transpose of B matrix.
	 * The B matrix is an nxm matrix with only 0, 1 and -1 elements.
	 * Each location in the matrix corresponds to a particular voltage source (first dimension) or a node (second dimension).
	 * If the positive terminal of the ith voltage source is connected to node k, then the element (i,k) in the B matrix is a 1.
	 * If the negative terminal of the ith voltage source is connected to node k, then the element (i,k) in the B matrix is a -1.
	 * Otherwise, elements of the B matrix are zero.
	 * Matrix B and C only change when grid is rebuilt
	 */
	def generateConnectionMatrix() {
		voltageSources.zipWithIndex.foreach {
			case (voltageSource, i) =>
				junctions.zipWithIndex.foreach {
					case (junction, junctionIndex) =>
						//Check positive connection
						if (electricGraph.containsEdge(voltageSource, junction)) {
							mna(junctions.size + i, junctionIndex) = 1
							mna(junctionIndex, junctions.size + i) = 1
						}
						else if (electricGraph.containsEdge(junction, voltageSource)) {
							mna(junctions.size + i, junctionIndex) = -1
							mna(junctionIndex, junctions.size + i) = -1
						}
				}
		}
	}

	/**
	 * Generates the G Matrix, the conductance.
	 * This matrix only changes if resistance changes.
	 */
	def generateConductanceMatrix() {
		//Construct G sub-matrix
		//Set all diagonals of the nxn part of the matrix with the sum of its adjacent resistor's conductance
		junctions.zipWithIndex.foreach {
			case (junction, i) =>
				mna(i, i) = resistors
					.filter(resistor => electricGraph.containsEdge(resistor, junction) || electricGraph.containsEdge(junction, resistor))
					.map(1 / _.component.resistance)
					.sum
		}

		//The off diagonal elements are the negative conductance of the element connected to the pair of corresponding node.
		//Therefore a resistor between nodes 1 and 2 goes into the G matrix at location (1,2) and locations (2,1).
		resistors.foreach(resistor => {
			val target = electricGraph.getEdgeTarget(electricGraph.outgoingEdgesOf(resistor).head)
			//The id of the junction at positive terminal
			val j = junctions.indexOf(target)
			val of = electricGraph.incomingEdgesOf(resistor)
			val map = of.map(electricGraph.getEdgeSource)
			val source = map.find(s => s != target).get
			//The id of the junction at negative terminal
			val i = junctions.indexOf(source)

			//Check to make sure this is not the ground reference junction
			if (i != -1 && j != -1) {
				val negConductance = -1 / resistor.component.resistance
				mna(i, j) = negConductance
				mna(j, i) = negConductance
			}
		})
	}

	/**
	 * The source matrix is a column vector, the right hand side of Ax = b equation.
	 * It contains two parts. This matrix is only recalculated when sources change.
	 */
	def computeSourceMatrix() {
		sourceMatrix = new Matrix(junctions.size + voltageSources.size, 1)

		//Part one: The sum of current sources corresponding to a particular node
		junctions.indices.foreach(i => {
			//A set of current sources that is going into this junction
			sourceMatrix(i, 0) = currentSources
				.filter(
			    source =>
				    (electricGraph.incomingEdgesOf(source).map(electricGraph.getEdgeSource).contains(junctions(i)) && source.component.current > 0) ||
					    (electricGraph.incomingEdgesOf(source).map(electricGraph.getEdgeSource).contains(junctions(i)) && source.component.current < 0)
				)
				.map(_.component.current)
				.sum
		})

		//Part two: The voltage of each voltage source
		voltageSources.indices.foreach(i => sourceMatrix(i + junctions.size, 0) = voltageSources(i).component.genVoltage)
	}

	/**
	 * Solve the circuit based on the currently buffered matrices, then injects the data back into the nodes.
	 */
	def solve() {
		if (electricGraph.vertexSet().size() > 2) {
			//TODO: Check why negation is required?
			val x = mna.solve(sourceMatrix * -1)

			//Retrieve the voltage of the junctions
			junctions.indices.foreach(i => junctions(i).voltage = x(i, 0))

			//Retrieve the current values of the voltage sources
			voltageSources.indices.foreach(i => {
				voltageSources(i).component.voltage = voltageSources(i).component.genVoltage
				voltageSources(i).component.current = x(i + junctions.size, 0)
			})

			//Calculate the potential difference for each component based on its junctions
			resistors.zipWithIndex.foreach {
				case (component, index) =>
					val wireFrom = electricGraph.outgoingEdgesOf(component).map(electricGraph.getEdgeTarget).head.asInstanceOf[Junction]
					val wireTo = electricGraph.incomingEdgesOf(component).map(electricGraph.getEdgeSource).find(w => w != wireFrom).get.asInstanceOf[Junction]
					val newVoltage = wireFrom.voltage - wireTo.voltage

					if (newVoltage != component.component.voltage) {
						component.component.voltage = newVoltage
						component.component.onVoltageChange.publish(new ElectricChangeEvent)
					}

					val newCurrent = component.component.voltage / component.component.resistance
					if (newCurrent != component.component.current) {
						component.component.current = newCurrent
						component.component.onCurrentChange.publish(new ElectricChangeEvent)
					}
			}
		}
	}
}