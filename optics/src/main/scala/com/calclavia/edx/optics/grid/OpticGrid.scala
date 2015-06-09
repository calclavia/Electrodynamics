package com.calclavia.edx.optics.grid

import java.util

import com.calclavia.edx.core.EDX
import com.calclavia.edx.core.extension.GraphExtension._
import nova.core.component.Updater
import nova.core.world.World
import org.jgrapht.alg.ConnectivityInspector
import org.jgrapht.graph.{DefaultEdge, SimpleDirectedGraph}
import org.jgrapht.traverse.BreadthFirstIterator

import scala.collection.convert.wrapAll._
/**
 * A grid that manages all waves produced in the world
 * @author Calclavia
 */
object OpticGrid {

	//Max laser render distance
	val maxDistance = 100

	//Minimum energy for laser
	val minPower = 100d
	val maxPower = 20000d

	val minEnergyToMine = 10000d
	val maxEnergyToMine = 500000d
	val minBurnEnergy = minEnergyToMine

	private val grids = new util.WeakHashMap[World, OpticGrid]

	def apply(world: World): OpticGrid = {
		if (!grids.containsKey(world)) {
			grids += (world -> new OpticGrid(world))
		}

		return grids(world)
	}
}

class OpticGrid(val world: World) extends Updater {

	var graph = new SimpleDirectedGraph[Beam, DefaultEdge](classOf[DefaultEdge])

	private var graphChanged = true

	EDX.syncTicker.add(this)

	/**
	 * Gets the set of wave sources
	 */
	def waveSources: Set[Beam] = graph.vertexSet().filter(v => graph.sourcesOf(v).isEmpty).toSet

	/**
	 * Gets the set of wave targets
	 */
	def waveTargets: Set[Beam] = graph.vertexSet().filter(v => graph.targetsOf(v).isEmpty).toSet

	/**
	 * @return The set of paths formed by all waves
	 */
	def wavePaths: Set[List[Beam]] = {
		var orderedPaths = Set.empty[List[Beam]]

		//Find each wave path
		waveSources
			.foreach(
				source => {
					var orderedPath = List.empty[Beam]
					val iterator = new BreadthFirstIterator(graph, source)
					iterator.foreach(wave => orderedPath :+= wave)
					orderedPaths += orderedPath
				}
			)
		return orderedPaths
	}

	/**
	 * Creates a laser emission point
	 */
	def create(laser: Beam, from: Beam = null) {
		graph synchronized {
			if (laser.power > OpticGrid.minPower) {
				//Mark node in graph
				graph.addVertex(laser)

				if (from != null) {
					graph.addEdge(from, laser)
				}
				else if (EDX.network.isServer) {
					graphChanged = true
				}
			}
		}
	}

	/**
	 * Destroys the laser, removing all verticies in the graph.
	 * @param beam The laser to remove
	 */
	def destroy(beam: Beam) {
		graph.synchronized {
			if (graph.containsVertex(beam)) {
				val inspector = new ConnectivityInspector(graph)
				val connected = inspector.connectedSetOf(beam)
				graph.removeAllVertices(connected)
				graphChanged = true
			} else {
				EDX.logger.error("Attempt to remove node that does not exist in wave grid.")
			}
		}
	}

	var timer = 0d

	override def update(deltaTime: Double) {
		super.update(deltaTime)

		timer += deltaTime

		if (timer >= 1) {
			timer = 0

			graph synchronized {
				/*if(graph.vertexSet().size == 0) {
					EDX.syncTicker.preQueue(() => EDX.syncTicker.remove(this))
				}
				else {*/
				val sources = waveSources

				//Reset graph
				graph = new SimpleDirectedGraph(classOf[DefaultEdge])

				//Regenerate graph based on sources
				sources.foreach(graph.addVertex)
				sources.foreach(_.update(deltaTime))

				if (EDX.network.isServer) {
					//Update client
					if (graphChanged) {
						println("Sent optic packet: " + graph.vertexSet().size())
						EDX.network.sync(this)
						graphChanged = false
					}
					}
				//}
			}
		}
	}
}
