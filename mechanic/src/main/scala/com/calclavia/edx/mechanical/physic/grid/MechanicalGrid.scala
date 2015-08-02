package com.calclavia.edx.mechanical.physic.grid


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

}

class MechanicalGrid {

	val graph = new ListenableUndirectedGraph(new Multigraph[MechanicalNode, RotationalEdge](classOf[RotationalEdge]))
	val neighborIndex = new NeighborIndex[MechanicalNode, RotationalEdge](graph)

	var rootNode: Option[MechanicalNode] = None
	graph.addGraphListener(neighborIndex)
	graph.addVertexSetListener(neighborIndex)

	def add(node: MechanicalNode, connections: Seq[(MechanicalNode, Boolean)]): Unit = {
		graph.addVertex(node)
		connections.foreach {
			case (n, forward) => graph addEdge(node, n, RotationalEdge(node, n, forward))
		}
	}

	def remove(node: MechanicalNode): Unit = {
		graph.removeVertex(node)
	}

	import MechanicalGrid._

	def recalculate(): RecalculateResult = {
		val nodes = graph.vertexSet()
		nodes.foreach(_.relativeSpeed = None)
		rootNode = Some(graph.vertexSet().iterator().next())
		rootNode.foreach(_.relativeSpeed = Some(1))
		val it = new BreadthFirstIterator(graph, rootNode.get)


		//From unknown reasons this function is not tail recursive
		//@tailrec
		def walk(it: BreadthFirstIterator[MechanicalNode, RotationalEdge]): RecalculateResult = {
			it.hasNext match {
				case false => return Success
				case true =>
			}

			def updateSpeed(node: MechanicalNode, newSpeed: Double): Option[RecalculateResult] = {
				println(s"Old: ${node.relativeSpeed}, New: $newSpeed")
				node.relativeSpeed match {
					case Some(x) if x != newSpeed => Some(Loop(node))
					case None => node.relativeSpeed = Some(newSpeed); None
					case _ => None
				}
			}

			val spinning = it.next
			val nodes = neighborIndex.neighborListOf(spinning) map (neigh => (spinning, neigh, graph.getEdge(spinning, neigh).forward))


			for (node <- nodes) {
				node match {
					case (gear: MechanicalNodeGear, gear2: MechanicalNodeGear, forward) =>
						println(s"1: ${gear.size}, 2: ${gear2.size}")
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

		walk(it)
	}
}


