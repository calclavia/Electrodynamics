package com.calclavia.edx.electric.graph

import com.calclavia.edx.electric.graph.api.Electric
import com.calclavia.graph.graph.GraphConnect
import com.calclavia.edx.electric.graph.component.{Junction, VirtualJunction}
import com.resonant.core.prefab.block.Updater
import com.calclavia.graph.matrix.AdjacencyMatrix
import nova.core.game.Game
import nova.core.util.transform.matrix.Matrix

import scala.collection.convert.wrapAll._

/**
 * An electric circuit grid for independent voltage sources.
 * The circuit solver uses MNA, based on http://www.swarthmore.edu/NatSci/echeeve1/Ref/mna/MNA3.html
 * We will be solving systems of linear equations using matrices.
 *
 * @author Calclavia
 */
class GraphElectric extends GraphConnect[Electric] with Updater {

	// There should always at least (node.size - 1) amount of junctions.
	var junctions = List.empty[Junction]
	//The reference ground junction where voltage is zero.
	var ground: Junction = null
	//The components in the circuit
	var components = List.empty[NodeElectricComponent]

	//The modified nodal analysis matrix (A) in Ax=b linear equation.
	var mna: Matrix = null
	var voltageSources = List.empty[NodeElectricComponent]
	var currentSources = List.empty[NodeElectricComponent]
	var resistors = List.empty[NodeElectricComponent]
	//Changed flags
	var resistorChanged = false
	var sourceChanged = false
	//The source matrix (B)
	protected var sourceMatrix: Matrix = null
	//The component-junction matrix. Rows are from, columns are to. In the directed graph the arrow points from positive to negative in potential difference.
	protected var terminalMatrix: AdjacencyMatrix[AnyRef] = null

	override def add(node: Electric) {
		super.add(node)

		node match {
			case node: NodeElectricComponent =>
				node.onResistanceChange :+= ((resistor: Electric) => resistorChanged = true)
				node.onSetVoltage :+= ((source: Electric) => sourceChanged = true)
				node.onSetCurrent :+= ((source: Electric) => sourceChanged = true)
			case _ =>
		}
	}

	/**
	 * Reconstruct must build the links and intersections of the grid
	 */
	override def build() {
		buildAll()
		Game.instance.syncTicker.add(this)
	}

	def buildAll() {
		/**
		 * Clean all variables
		 */
		junctions = List.empty
		ground = null
		components = List.empty
		mna = null
		sourceMatrix = null
		terminalMatrix = null

		/**
		 * Builds the adjacency matrix.
		 * The directed graph indicate current flow from positive terminal to negative terminal.
		 */
		adjMat = new AdjacencyMatrix(nodes, nodes)

		var recursedWires = Set.empty[NodeElectricJunction]
		//A queue of virtual junctions to their corresponding tuple component to be binded together
		var virtualBindQueue = Map.empty[VirtualJunction, (NodeElectricComponent, NodeElectricComponent)]

		nodes.foreach {
			case node: NodeElectricComponent =>
				for (con <- node.positiveConnections()) {
					if (nodes.contains(con)) {
						con match {
							case component: NodeElectricComponent =>
								//Check if the "component" is negatively connected to the current node
								if (component.negativeConnections().contains(node)) {
									adjMat(node, component) = true
									//This component is connected to another component. Create virtual junctions between them.
									var junction = new VirtualJunction
									virtualBindQueue += junction ->(node, component)
									junctions :+= junction
								}
							case junction: NodeElectricJunction =>
								adjMat(node, junction) = true
						}

						components :+= node
					}
				}
			case node: NodeElectricJunction =>
				for (con <- node.connections()) {
					if (nodes.contains(con)) {
						adjMat(node, con) = true
					}
				}

				if (!recursedWires.contains(node)) {
					/**
					 * Collapse all wires into junctions.
					 */
					//Create a junction
					val junction = new Junction

					//Find all the wires for this junction
					val foundWires = recurseFind(node)
					//Mark the wire as found, preventing it from generating any new junctions.
					recursedWires ++= foundWires
					junction.wires = foundWires

					//Add to junctions
					foundWires.foreach(_.junction = junction)
					junctions :+= junction
					//TODO: Create virtual junctions with resistors to simulate wire resistance
				}
		}

		if (junctions.size > 0) {
			/**
			 * Create the connect adjacency matrix.
			 */
			terminalMatrix = new AdjacencyMatrix[AnyRef](nodes ++ junctions ++ virtualBindQueue.keys)

			junctions.foreach {
				case virtualJunction: VirtualJunction =>
					val (a, b) = virtualBindQueue(virtualJunction)
					terminalMatrix(a, virtualJunction) = true
					terminalMatrix(virtualJunction, b) = true
				case junction =>
					//Find all the components connected to this junction
					val connectedComponents = junction.wires
						.flatMap(_.connections)
						.collect { case n: NodeElectricComponent => n }

					//Set adjMat connection by marking the component-junction position as true
					connectedComponents.foreach(component => {
						if (adjMat.getDirectedFrom(component).exists(c => junction.wires.contains(c))) {
							//Component is connected to junction via positive terminal
							terminalMatrix(component, junction) = true
						}
						else if (adjMat.getDirectedTo(component).exists(c => junction.wires.contains(c))) {
							//Component is connected to junction via negative terminal
							terminalMatrix(junction, component) = true
						}
					})
			}

			//Select reference ground
			ground = junctions.head
			junctions = junctions.splitAt(1)._2
			ground.voltage = 0
		}
	}

	override def update(deltaTime: Double) {
		if (junctions.size > 0) {
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

	/**
	 * Setup MNA Matrix.
	 * This should be called if the number of voltage sources changes
	 */

	def setupMNA(): Unit = {
		voltageSources = components.collect { case source if source.genVoltage != 0 => source }
		currentSources = components.collect { case source if source.genCurrent != 0 => source }
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
					.filter(resistor => terminalMatrix.isConnected(resistor, junction))
					.map(1 / _.resistance)
					.sum
		}

		//The off diagonal elements are the negative conductance of the element connected to the pair of corresponding node.
		//Therefore a resistor between nodes 1 and 2 goes into the G matrix at location (1,2) and locations (2,1).
		for (resistor <- resistors) {
			//The id of the junction at negative terminal
			val i = junctions.indexOf(terminalMatrix.getDirectedTo(resistor).head)
			//The id of the junction at positive terminal
			val j = junctions.indexOf(terminalMatrix.getDirectedFrom(resistor).head)

			//Check to make sure this is not the ground reference junction
			if (i != -1 && j != -1) {
				val negConductance = -1 / resistor.resistance
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
				val posIndex = junctions.indexOf(terminalMatrix.getDirectedFrom(voltageSource).head)
				//Check to make sure this is not the ground reference junction
				if (posIndex != -1) {
					mna(junctions.size + i, posIndex) = 1
					mna(posIndex, junctions.size + i) = 1
				}
				//Negative terminal
				val negIndex = junctions.indexOf(terminalMatrix.getDirectedTo(voltageSource).head)
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
			sourceMatrix(i, 0) = currentSources.filter(
				source =>
					(adjMat.getDirectedTo(source).contains(junctions(i)) && source.current > 0) || (adjMat.getDirectedFrom(source).contains(junctions(i)) && source.current < 0)
			)
				.map(_.current)
				.sum
		}

		//Part two: The voltage of each voltage source
		for (i <- 0 until voltageSources.size) {
			sourceMatrix(i + junctions.size, 0) = voltageSources(i).genVoltage
		}
	}

	/**
	 * Solve the circuit based on the currently buffered matrices, then injects the data back into the nodes.
	 */
	def solve() {
		//TODO: Check why negation is required?
		val x = mna.solve(sourceMatrix * -1)

		//Retrieve the voltage of the junctions
		for (i <- 0 until junctions.size) {
			junctions(i).voltage = x(i, 0)
		}

		//Retrieve the current values of the voltage sources
		for (i <- 0 until voltageSources.size) {
			voltageSources(i).voltage = voltageSources(i).genVoltage
			voltageSources(i).current = x(i + junctions.size, 0)
		}

		//Calculate the potential difference for each component based on its junctions
		resistors.zipWithIndex.foreach {
			case (component, index) =>
				val wireTo = terminalMatrix.getDirectedTo(component).head.asInstanceOf[Junction]
				val wireFrom = terminalMatrix.getDirectedFrom(component).head.asInstanceOf[Junction]
				component.voltage = wireFrom.voltage - wireTo.voltage
				component.current = component.voltage / component.resistance
		}
	}

	/**
	 * Finds all the interconnected wires that connect to a particular wire.
	 * @param wire The wire to search for.
	 * @return A set of wires that are interconnected.
	 */
	private def recurseFind(wire: NodeElectricJunction, result: Set[NodeElectricJunction] = Set.empty): Set[NodeElectricJunction] = {
		val wireConnections = wire.connections.filter(_.isInstanceOf[NodeElectricJunction]).map(_.asInstanceOf[NodeElectricJunction])
		var newResult = result + wire

		newResult ++= wireConnections
			.filterNot(result.contains)
			.map(n => recurseFind(n, newResult))
			.flatten

		return newResult
	}
}