package com.calclavia.edx.electric.graph

import com.calclavia.edx.electric.graph.api.{Electric, ElectricComponent, ElectricJunction}
import com.resonant.core.prefab.block.Updater
import nova.core.game.Game
import nova.core.util.transform.matrix.Matrix
import nova.core.world.World
import org.jgrapht.graph.{DefaultDirectedGraph, DefaultEdge, SimpleGraph}

import scala.collection.convert.wrapAll._
import scala.collection.mutable

/**
 * An electric circuit grid for independent voltage sources.
 * The circuit solver uses MNA, based on http://www.swarthmore.edu/NatSci/echeeve1/Ref/mna/MNA3.html
 * We will be solving systems of linear equations using matrices.
 *
 * @author Calclavia
 */
object ElectricGrid {
	private val worldMap = mutable.WeakHashMap.empty[World, ElectricGrid]

	def apply(world: World): ElectricGrid = {
		worldMap.getOrElseUpdate(world, new ElectricGrid)
	}
}

class ElectricGrid extends Updater {
	//The general connection graph between electric
	protected val connectionGraph = new SimpleGraph[Electric, DefaultEdge](classOf[DefaultEdge])
	// There should always at least (node.size - 1) amount of junctions.
	var junctions = List.empty[Junction]
	//The reference ground junction where voltage is zero.
	var ground: Junction = null
	//The components in the circuit
	var components = List.empty[Device]
	//The modified nodal analysis matrix (A) in Ax=b linear equation.
	var mna: Matrix = null
	var voltageSources = List.empty[Device]
	var currentSources = List.empty[Device]
	var resistors = List.empty[Device]
	//Changed flags
	var resistorChanged = false
	var sourceChanged = false
	//The source matrix (B)
	protected var sourceMatrix: Matrix = null
	//The graph of all electric elements. In this directed graph the arrow points from positive to negative in potential difference.
	protected var electricGraph = new DefaultDirectedGraph[ElectricElement, DefaultEdge](classOf[DefaultEdge])

	Game.syncTicker.add(this)

	def findAll(node: Electric, builder: Set[Electric] = Set.empty): Set[Electric] =
		node match {
			case component: ElectricComponent =>
				(component.positives ++ component.negatives).filterNot(builder.contains(_)).flatMap(n => findAll(n, builder + node)).toSet
			case junction: ElectricJunction =>
				junction.connections.get.filterNot(builder.contains(_)).flatMap(n => findAll(n, builder + node)).toSet
		}

	def addRecursive(node: Electric): this.type = {
		findAll(node).foreach(add)
		return this
	}

	def add(node: Electric): this.type = {
		connectionGraph.addVertex(node)

		//TODO: Can't we build the electric graph from here?
		node match {
			case node: NodeElectricComponent =>
				val connections = node.positives() ++ node.negatives()
				connections.foreach(connectionGraph.addVertex)
				connections.foreach(n => connectionGraph.addEdge(node, n))
				node.onResistanceChange :+= ((resistor: Electric) => resistorChanged = true)
				node.onSetVoltage :+= ((source: Electric) => sourceChanged = true)
				node.onSetCurrent :+= ((source: Electric) => sourceChanged = true)
			case node: NodeElectricJunction =>
				node.con.foreach(connectionGraph.addVertex(_))
		}
		return this
	}

	def has(node: Electric) = connectionGraph.vertexSet().contains(node)

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
		 * Builds the adjacency matrix.
		 * The directed graph indicate current flow from positive terminal to negative terminal.
		 */
		var recursedWires = Set.empty[NodeElectricJunction]
		//A queue of virtual junctions to their corresponding tuple component to be binded together
		var virtualBindQueue = Map.empty[VirtualJunction, (NodeElectricComponent, NodeElectricComponent)]

		connectionGraph.vertexSet().foreach {
			case nodeComponent: NodeElectricComponent =>
				val component = new Device(nodeComponent)
				//Check positive terminal connections
				nodeComponent.positives().foreach {
					case checkComponent: NodeElectricComponent =>
						//Check if the "component" is negatively connected to the current node
						if (checkComponent.negatives().contains(nodeComponent)) {
							val junction = new VirtualJunction
							val checkDevice = new Device(nodeComponent)
							electricGraph.addVertex(component)
							electricGraph.addVertex(junction)
							electricGraph.addVertex(checkDevice)

							electricGraph.addEdge(component, junction)
							electricGraph.addEdge(junction, checkDevice)
							//This component is connected to another component. Create virtual junctions between them.
							//var junction = new VirtualJunction
							//virtualBindQueue += junction ->(node, component)
							//junctions :+= junction
						}
					case nodeJunction: NodeElectricJunction =>
						val junction = getOrCreateJunction(nodeJunction)
						electricGraph.addVertex(component)
						electricGraph.addVertex(junction)
						electricGraph.addEdge(component, junction)
				}
				components :+= component
			case nodeJunction: NodeElectricJunction =>
				val junction = getOrCreateJunction(nodeJunction)
				electricGraph.addVertex(junction)

				//Connect the junction to all of this NodeElectricJunction's nodes
				nodeJunction.con.foreach {
					case nodeComponent: NodeElectricComponent =>
						//TODO: Check hashcode connection
						val device = new Device(nodeComponent)
						electricGraph.addVertex(device)
						electricGraph.addEdge(junction, device)
				}

			/*
			if (!recursedWires.contains(nodeJunction)) {
				/**
				 * Collapse all wires into junctions.
				*/
				//Create a junction
				val junction = new Junction

				//Find all the wires for this junction
				val foundWires = recurseFind(nodeJunction)
				//Mark the wire as found, preventing it from generating any new junctions.
				recursedWires ++= foundWires
				junction.wires = foundWires

				//Add to junctions
				foundWires.foreach(_.junction = junction)
				junctions :+= junction
			} */

			//TODO: Create virtual junctions with resistors to simulate wire resistance
		}

		if (electricGraph.vertexSet().size() > 0) {
			/*
		/**
		 * Create the connect adjacency matrix.
		 */
		electricGraph = new AdjacencyMatrix[AnyRef](nodes ++ junctions ++ virtualBindQueue.keys)

		junctions.foreach {
			case virtualJunction: VirtualJunction =>
				val (a, b) = virtualBindQueue(virtualJunction)
				electricGraph ~+>= (a ~> virtualJunction)
				electricGraph ~+>= (virtualJunction ~> b)
			case junction =>
				//Find all the components connected to this junction
				val connectedComponents = junction.wires
					.flatMap(_.connections)
					.collect { case n: NodeElectricComponent => n }

				//Set adjMat connection by marking the component-junction position as true
				connectedComponents.foreach(component => {
					if (adjMat.getDirectedFrom(component).exists(c => junction.wires.contains(c))) {
						//Component is connected to junction via positive terminal
						electricGraph(component, junction) = true
					}
					else if (adjMat.getDirectedTo(component).exists(c => junction.wires.contains(c))) {
						//Component is connected to junction via negative terminal
						electricGraph(junction, component) = true
					}
				})
		} */

			//Select reference ground
			ground = junctions.head
			junctions = junctions.splitAt(1)._2
			ground.voltage = 0
		}
	}

	//Create or get an existing Junction
	def getOrCreateJunction(nodeJunction: NodeElectricJunction): Junction =
		junctions
			.find(j => j.wires.contains(nodeJunction))
			.collect {
			case junction: Junction => junction
			case _ =>
				val newJunction = new Junction
				newJunction.wires += nodeJunction
				junctions.add(newJunction)
				newJunction
		}
			.get

	override def update(deltaTime: Double) {
		if (junctions.nonEmpty) {
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

			//TODO: Do not empty voltage
			resistorChanged = false
			sourceChanged = false
		}
	}

	/**
	 * Setup MNA Matrix.
	 * This should be called if the number of voltage sources changes
	 */

	def setupMNA() {
		voltageSources = components.collect { case source if source.component.genVoltage != 0 => source }
		currentSources = components.collect { case source if source.component.genCurrent != 0 => source }
		resistors = components diff voltageSources diff currentSources
		mna = new Matrix(voltageSources.size + junctions.size)
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
					.filter(resistor => electricGraph.containsEdge(resistor, junction))
					.map(1 / _.component.resistance)
					.sum
		}

		//The off diagonal elements are the negative conductance of the element connected to the pair of corresponding node.
		//Therefore a resistor between nodes 1 and 2 goes into the G matrix at location (1,2) and locations (2,1).
		for (resistor <- resistors) {
			//The id of the junction at negative terminal
			//Get directted to
			val i = junctions.indexOf(electricGraph.getEdgeSource(electricGraph.incomingEdgesOf(resistor).head))
			//The id of the junction at positive terminal
			//Get directed from
			val j = junctions.indexOf(electricGraph.getEdgeTarget(electricGraph.outgoingEdgesOf(resistor).head))

			//Check to make sure this is not the ground reference junction
			if (i != -1 && j != -1) {
				val negConductance = -1 / resistor.component.resistance
				mna(i, j) = negConductance
				mna(j, i) = negConductance
			}
		}
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
				//Positive terminal
				val posIndex = junctions.indexOf(electricGraph.getEdgeTarget(electricGraph.outgoingEdgesOf(voltageSource).head))
				//Check to make sure this is not the ground reference junction
				if (posIndex != -1) {
					mna(junctions.size + i, posIndex) = 1
					mna(posIndex, junctions.size + i) = 1
				}
				//Negative terminal
				val negIndex = junctions.indexOf(electricGraph.getEdgeSource(electricGraph.incomingEdgesOf(voltageSource).head))
				//Check to make sure this is not the ground reference junction
				if (negIndex != -1) {
					mna(junctions.size + i, negIndex) = -1
					mna(negIndex, junctions.size + i) = -1
				}
		}
	}

	/**
	 * The source matrix is a column vector, the right hand side of Ax = b equation.
	 * It contains two parts. This matrix is only recalculated when sources change.
	 */
	def computeSourceMatrix() {
		sourceMatrix = new Matrix(junctions.size + voltageSources.size, 1)

		//Part one: The sum of current sources corresponding to a particular node
		for (i <- 0 until junctions.size) {
			//A set of current sources that is going into this junction
			sourceMatrix(i, 0) = currentSources
				.filter(
			    source =>
				    ((electricGraph.getEdgeSource(electricGraph.incomingEdgesOf(source).head) == junctions(i)) && source.component.current > 0) ||
					    ((electricGraph.getEdgeSource(electricGraph.incomingEdgesOf(source).head) == junctions(i)) && source.component.current < 0)
				)
				.map(_.component.current)
				.sum
		}

		//Part two: The voltage of each voltage source
		voltageSources.indices.foreach(i => sourceMatrix(i + junctions.size, 0) = voltageSources(i).component.genVoltage)
	}

	/**
	 * Solve the circuit based on the currently buffered matrices, then injects the data back into the nodes.
	 */
	def solve() {
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
				val wireTo = electricGraph.getEdgeSource(electricGraph.incomingEdgesOf(component).head).asInstanceOf[Junction]
				val wireFrom = electricGraph.getEdgeTarget(electricGraph.outgoingEdgesOf(component).head).asInstanceOf[Junction]
				component.component.voltage = wireFrom.voltage - wireTo.voltage
				component.component.current = component.component.voltage / component.component.resistance
		}
	}

	/**
	 * Finds all the interconnected wires that connect to a particular wire.
	 * @param wire The wire to search for.
	 * @return A set of wires that are interconnected.
	 */
	private def recurseFind(wire: NodeElectricJunction, result: Set[NodeElectricJunction] = Set.empty): Set[NodeElectricJunction] = {
		//TODO: we're calling connections() too many times!
		val wireConnections = wire.con.filter(_.isInstanceOf[NodeElectricJunction]).map(_.asInstanceOf[NodeElectricJunction])
		var newResult = result + wire

		newResult ++= (wireConnections diff result).flatMap(n => recurseFind(n, newResult))

		return newResult
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
	class Device(val component: NodeElectricComponent) extends ElectricElement {
		override def hashCode = component.hashCode()
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
	}

	/**
	 * A virtual junction is a junction that does not exist in the world, but exists in the graph space.
	 * @author Calclavia
	 */
	class VirtualJunction extends Junction {

	}

}