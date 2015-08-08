package com.calclavia.edx.mechanical.physic.grid


import com.calclavia.edx.mechanical.{DynamicValue, Watch}
import org.jgrapht.alg.NeighborIndex
import org.jgrapht.graph.{ListenableUndirectedGraph, DefaultListenableGraph, DefaultEdge, Multigraph}
import org.jgrapht.traverse.{BreadthFirstIterator, DepthFirstIterator}
import scala.annotation.tailrec
import scala.collection.JavaConversions._

object MechanicalGrid {

	sealed trait RecalculateResult

	object Success extends RecalculateResult

	trait Failure extends RecalculateResult {
		val message: String
		override val toString = message
	}

	case class Loop(node: MechanicalNode) extends Failure {
		val message = s"Loop detected. Faulty node $node"
	}

	def merge(grids: Seq[MechanicalGrid]): MechanicalGrid = ???

	def split(grid: MechanicalGrid): Seq[MechanicalGrid] = ???
}

class MechanicalGrid {

	private[this] val graph = new ListenableUndirectedGraph(new Multigraph[MechanicalNode, RotationalEdge](classOf[RotationalEdge]))
	private[this] val neighborIndex = new NeighborIndex[MechanicalNode, RotationalEdge](graph)

	private[this] var rootNode: Option[MechanicalNode] = None
	graph.addGraphListener(neighborIndex)
	graph.addVertexSetListener(neighborIndex)

	private[this] var angularSpeed = Math.PI / 4 //TODO: Calculate that.
	private[this] var systemRotation = 0D


	private[this] var systemMass: DynamicValue[Double] = null
	private[this] var systemFriction: DynamicValue[Double] = null

	private[this] val watch = {
		import scala.concurrent.duration._
		new Watch(Some((1/20).seconds))
	}

	def add(node: MechanicalNode, connections: Seq[(MechanicalNode, Boolean)]): Unit = {
		graph.addVertex(node)
		connections.foreach {
			case (n, forward) => graph addEdge(node, n, RotationalEdge(node, n, forward))
		}
	}

	def remove(node: MechanicalNode): Unit = {
		graph.removeVertex(node)
	}

	def rotation(node: MechanicalNode) = {
		update()

		node.relativeSpeed.map(_ * systemRotation).getOrElse(0D)
	}

	private[this] def update(): Unit = {
		import scala.concurrent.duration._
		if (watch.shouldUpdate) {
			val diff = watch.timeDiff
			systemRotation += angularSpeed * diff.toUnit(SECONDS)
			//TODO: friction and force logic
			//TODO: dynamic friction and force logic.
			watch.update()
		}
	}

	import MechanicalGrid._

	def recalculate(): RecalculateResult = {
		val nodes = graph.vertexSet()
		nodes.foreach(_.relativeSpeed = None)
		rootNode = Some(graph.vertexSet().iterator().next())
		rootNode.foreach(_.relativeSpeed = Some(1))
		val it = new BreadthFirstIterator(graph, rootNode.get)

		var flatFriction: Double = 0D
		var dynamicFriction: List[() => Double] = Nil
		var flatMass: Double = 0D
		var dynamicMass: List[() => Double] = Nil

		//From unknown reasons this function is not tail recursive
		//@tailrec
		def walk(it: BreadthFirstIterator[MechanicalNode, RotationalEdge]): RecalculateResult = {
			it.hasNext match {
				case false => return Success
				case true =>
			}

			def updateSpeed(node: MechanicalNode, newSpeed: Double): Option[RecalculateResult] = {
				node.relativeSpeed match {
					case Some(x) if x != newSpeed => Some(Loop(node))
					case None => node.relativeSpeed = Some(newSpeed); None
					case _ => None
				}
			}

			val spinning = it.next


			spinning match {
				case node: MechanicalNode.MechanicalNodeConstantFriction => flatFriction += node.friction
				case node: MechanicalNode => dynamicFriction = node.friction _ :: dynamicFriction
			}

			spinning match {
				case node: MechanicalNode.MechanicalNodeConstantMass => flatMass += node.mass
				case node: MechanicalNode => dynamicFriction = node.mass _ :: dynamicFriction
			}

			val nodes = neighborIndex.neighborListOf(spinning) map (neigh => (spinning, neigh, graph.getEdge(spinning, neigh).forward))

			for (node <- nodes) {
				node match {
					case (gear: MechanicalNodeGear, gear2: MechanicalNodeGear, forward) =>
						val newSpeed = gear.relativeSpeed.get * gear.size / gear2.size * (if (forward) 1 else -1)
						updateSpeed(gear2, newSpeed) match {
							case Some(x) => return x
							case _ =>
						}
					case (current, next, forward) =>
						val newSpeed = current.relativeSpeed.get * (if (forward) 1 else -1)
						updateSpeed(next, newSpeed) match {
							case Some(x) => return x
							case _ =>
						}
				}
			}

			walk(it)
		}

		walk(it) match {
			case Success => {
				systemFriction = DynamicValue(flatFriction, dynamicFriction)
				systemMass = DynamicValue(flatMass, dynamicMass)
				Success
			}
			case x => x
		}
	}
}


