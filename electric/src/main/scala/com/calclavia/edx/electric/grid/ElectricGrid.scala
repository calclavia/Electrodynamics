package com.calclavia.edx.electric.grid

import java.io.{File, FileWriter}
import java.util
import java.util.Collections

import com.calclavia.edx.core.extension.GraphExtension._
import com.calclavia.edx.electric.api.Electric.{ElectricChangeEvent, GraphBuiltEvent}
import com.calclavia.edx.electric.api.{Electric, ElectricComponent}
import com.resonant.lib.WrapFunctions._
import nova.core.util.transform.matrix.Matrix
import org.jgrapht.alg.ConnectivityInspector
import org.jgrapht.ext.{DOTExporter, VertexNameProvider}
import org.jgrapht.graph.{DefaultDirectedGraph, DefaultEdge}
import org.jgrapht.{DirectedGraph, Graph}

import scala.collection.convert.wrapAll._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
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

		if (find.isDefined) {
			return find.get
		}

		val grid = new ElectricGrid
		grids += grid
		return grid
	}

	def destroy(electric: Electric) {
		grids.removeAll(grids.filter(grid => grid.has(electric)))
	}

	def exportGraph[A, B](graph: Graph[A, B], name: String = "test") {
		//Export graph
		val exporter = new DOTExporter[A, B](new VertexNameProvider[A] {
			override def getVertexName(v: A): String = v.toString
		}, null, null)

		val targetDirectory = "edx/graph/"
		new File(targetDirectory).mkdirs()
		exporter.export(new FileWriter(targetDirectory + name + ".dot"), graph)
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

class ElectricGrid {

	import ElectricGrid._

	/**
	 * The general connection graph between electric devices.
	 *
	 * If A is connected to B, then A ---> B.
	 *
	 * The graph disregards positive and negative terminals
	 */
	protected[grid] val connectionGraph = new DefaultDirectedGraph[Electric, DefaultEdge](classOf[DefaultEdge])
	//The graph of all electric elements. In this directed graph the arrow points from positive to negative in potential difference.
	protected[grid] var electricGraph: DefaultDirectedGraph[ElectricElement, DefaultEdge] = null
	// There should always at least (node.size - 1) amount of junctions.
	var junctions = List.empty[Junction]
	//The reference ground junction where voltage is zero.
	var ground: Junction = null
	//The components in the circuit
	var components = List.empty[Component]
	//The modified nodal analysis matrix (A) in Ax=b linear equation.
	var mna: Matrix = null
	//A list of voltage sources
	var voltageSources = List.empty[Component]
	//A list of current sources
	var currentSources = List.empty[Component]
	//A list of resistors
	var resistors = List.empty[Component]
	//The source matrix (B)
	protected[grid] var sourceMatrix: Matrix = null

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
			case (node, connections) => add(node, connections)
		}
		return this
	}

	def add(node: Electric): this.type = {
		node match {
			case nodeComponent: NodeElectricComponent => add(nodeComponent, (nodeComponent.positives() ++ nodeComponent.negatives()).toSet)
			case nodeJunction: NodeElectricJunction => add(nodeJunction, nodeJunction.con)
		}
		return this
	}

	def add(node: Electric, connections: Set[Electric]): this.type = {
		connectionGraph.addVertex(node)

		connections.foreach(
			neighbor => {
				connectionGraph.addVertex(neighbor)
				connectionGraph.addEdge(node, neighbor)
			}
		)

		node match {
			case node: NodeElectricComponent =>

				node.onResistanceChange.add((evt: ElectricChangeEvent) => {
					requestUpdate(resistorChanged = true)
				})
				node.onInternalVoltageChange :+= ((source: Electric) => {
					requestUpdate(sourceChanged = true)
				})
				node.onInternalCurrentChange :+= ((source: Electric) => {
					requestUpdate(sourceChanged = true)
				})
			case node: NodeElectricJunction =>
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

		if (find.isDefined) {
			return find.get
		}

		if (ground != null && ground.wires.contains(nodeJunction)) {
			return ground
		}

		val newJunction = new Junction

		val clonedGraph = connectionGraph.clone.asInstanceOf[DirectedGraph[Electric, DefaultEdge]]
		clonedGraph.vertexSet()
			.filterNot(_.isInstanceOf[NodeElectricJunction])
			.foreach(clonedGraph.removeVertex)

		val inspector = new ConnectivityInspector(clonedGraph)
		newJunction.wires ++= inspector.connectedSetOf(nodeJunction).map(_.asInstanceOf[NodeElectricJunction])
		junctions :+= newJunction
		return newJunction
	}

	/**
	 * Builds the MNA matrix and prepares graph for simulation
	 */
	def build(): this.type = {
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
				//Check all mutual connections
				(connectionGraph.connectionsOf(nodeComponent) & nodeComponent.positives().toSet)
					.foreach {
					case checkNodeComponent: NodeElectricComponent =>
						//Let the checkComponent connect to this. Don't need to check negative terminal for incoming connections
						//TODO: Cache the terminal. It's expensive calculation!
						val junction = new VirtualJunction
						val checkComponent = convert(checkNodeComponent)

						electricGraph.addVertex(component)
						electricGraph.addVertex(junction)
						electricGraph.addVertex(checkComponent)

						//It's a negative terminal connection, so point this component to the checkComponent
						electricGraph.addEdge(component, junction)
						electricGraph.addEdge(junction, checkComponent)
						electricGraph.addEdge(junction, component)

						junctions :+= junction

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

				//Connect the junction to all of this NodeElectricJunction's nodes. These are mutual connections as defined by #connectionOf.
				connectionGraph
					.connectionsOf(nodeJunction)
					.foreach {
					case nodeComponent: NodeElectricComponent =>
						val component = convert(nodeComponent)
						electricGraph.addVertex(component)
						electricGraph.addEdge(junction, component)
					case nodeJunction: NodeElectricJunction =>
				}

			//TODO: Create virtual junctions with resistors to simulate wire resistance
		}

		if (electricGraph.vertexSet().size() > 0) {
			//Select reference ground
			ground = junctions.head
			junctions = junctions.splitAt(1)._2
			ground.voltage = 0

			println("Built grid successfully")
			//exportGraph(electricGraph, "Electric Grid " + gridID)
			connectionGraph.vertexSet()
				.foreach(
					v => v.onGridBuilt.publish(new GraphBuiltEvent(connectionGraph.connectionsOf(v)))
				)
		}
		return this
	}

	var updateFuture: Future[Unit] = _

	def requestUpdate(resistorChanged: Boolean = true, sourceChanged: Boolean = true) {
		if (updateFuture == null || updateFuture.isCompleted) {
			updateFuture = Future {
				update()
			}
			updateFuture.onComplete {
				case Success(nothing) => println("Circuit solved")
				case Failure(ex) => println("Circuit failed: " + ex.printStackTrace)
			}
		}
		else {
			//	updateFuture.onComplete(f => requestUpdate())
		}
	}

	def update(resistorChanged: Boolean = true, sourceChanged: Boolean = true) {
		electricGraph.synchronized {
			//Reset all componets
			electricGraph.vertexSet().foreach {
				case component: Component =>
					component.component.voltage = 0
					component.component.current = 0
				case junction: Junction =>
					junction.voltage = 0
			}


			//You need a junction and a ground
			if (junctions.nonEmpty) {
				val allChange = mna == null || sourceChanged

				if (allChange) {
					setupMNA()
				}

				if (voltageSources.isEmpty && currentSources.isEmpty) {
					println("No voltage or current source. Skipping.")
					return
				}

				if (allChange) {
					generateConnectionMatrix()
				}

				if (resistorChanged || allChange) {
					generateConductanceMatrix()
				}

				if (sourceChanged || allChange) {
					computeSourceMatrix()
				}

				if (resistorChanged || sourceChanged || allChange) {
					try {
						solve()
					}
					catch {
						case e: Exception =>
							println("Failed to solve circuit")
							e.printStackTrace()
					}
				}
			}
			else {
				println("Circuit incomplete. Skipping.")
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
			val target = electricGraph.targetsOf(resistor).head
			//The id of the junction at positive terminal
			val j = junctions.indexOf(target)

			val source = electricGraph.sourcesOf(resistor).find(s => s != target).getOrElse(null)
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
						//TODO: note tested
						(electricGraph.sourcesOf(source).contains(junctions(i)) && source.component.current > 0) ||
							(electricGraph.targetsOf(source).contains(junctions(i)) && source.component.current < 0)
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
			val x = mna.solve(sourceMatrix)

			//Retrieve the voltage of the junctions
			junctions.indices.foreach(i => junctions(i).voltage = x(i, 0))

			//Retrieve the current values of the voltage sources
			voltageSources.indices.foreach(i => {
				voltageSources(i).component.voltage = voltageSources(i).component.genVoltage
				voltageSources(i).component.current = -x(i + junctions.size, 0)
			})

			//Calculate the potential difference for each component based on its junctions
			resistors.zipWithIndex.foreach {
				case (component, index) =>
					val wireTo = electricGraph.targetsOf(component).map(_.asInstanceOf[Junction]).head
					val wireFrom = electricGraph.sourcesOf(component).map(_.asInstanceOf[Junction]).filter(_ != wireTo).head
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