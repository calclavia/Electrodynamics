package com.calclavia.edx.electric.circuit.component.laser

import java.util
import java.util.Optional

import com.calclavia.edx.core.extension.GraphExtension._
import com.calclavia.edx.electric.circuit.component.laser.WaveGrid.{Electromagnetic, Wave}
import com.calclavia.edx.electric.circuit.component.laser.fx.EntityLaser
import com.resonant.lib.WrapFunctions._
import nova.core.component.Updater
import nova.core.component.misc.Damageable
import nova.core.game.Game
import nova.core.network.Packet
import nova.core.network.handler.PacketType
import nova.core.render.Color
import nova.core.retention.{Data, Storable, Stored}
import nova.core.util.RayTracer.{RayTraceBlockResult, RayTraceEntityResult, RayTraceResult}
import nova.core.util.exception.NovaException
import nova.core.util.transform.vector.Vector3d
import nova.core.util.{Ray, RayTracer}
import nova.core.world.World
import org.jgrapht.alg.ConnectivityInspector
import org.jgrapht.graph.{DefaultEdge, SimpleDirectedGraph}
import org.jgrapht.traverse.BreadthFirstIterator

import scala.collection.convert.wrapAll._

/**
 * A grid that manages all waves produced in the world
 * @author Calclavia
 */
object WaveGrid {

	//Max laser render distance
	val maxDistance = 100

	//Minimum energy for laser
	val minEnergy = 100d
	val maxPower = 20000d

	val minEnergyToMine = 10000d
	val maxEnergyToMine = 500000d
	val minBurnEnergy = minEnergyToMine

	private val grids = new util.WeakHashMap[World, WaveGrid]

	def apply(world: World): WaveGrid = {
		if (!grids.containsKey(world)) {
			grids += (world -> new WaveGrid(world))
		}

		return grids(world)
	}

	abstract class Wave(var source: Ray, @Stored var renderOrigin: Vector3d, @Stored var power: Double, var color: Color) extends Storable {
		var hit: RayTraceResult = null
		var hitTime = -1L

		def computeHit(world: World) =
			new RayTracer(source)
				.setDistance(maxDistance)
				.rayTraceAll(world)
				.findFirst()

		def fire(world: World): Optional[RayTraceResult] = {
			hit = computeHit(world).orElseGet(() => null)
			hitTime = System.currentTimeMillis
			return Optional.ofNullable(hit)
		}

		def timeElapsed = (System.currentTimeMillis - hitTime) / 1000d

		/**
		 * The power at the receiving end (after energy loss)
		 */
		def hitPower = if (hit != null) Math.max(power - power * hit.distance * 0.95, 0) else 0d

		override def save(data: Data) {
			super.save(data)
			data.put("origin", source.origin)
			data.put("dir", source.dir)
			data.put("renderOrigin", renderOrigin)
			data.put("color", color.rgba())
		}

		override def load(data: Data) {
			super.load(data)
			source = new Ray(data.getStorable("origin"), data.getStorable("dir"))
			renderOrigin = data.getStorable("renderOrigin")
			color = Color.rgba(data.get("color"))
		}

		def render(world: World)

		override def equals(obj: scala.Any): Boolean = {
			if (obj.isInstanceOf[Wave]) {
				val otherWave = obj.asInstanceOf[Wave]
				return source.origin.equals(otherWave.source.origin) &&
					source.dir.equals(otherWave.source.dir) &&
					renderOrigin.equals(otherWave.renderOrigin) &&
					color.equals(otherWave.color) &&
					power.equals(otherWave.power)
			}
			return false
		}
	}

	class Electromagnetic(source: Ray, renderOrigin: Vector3d, power: Double, color: Color) extends Wave(source, renderOrigin, power, color) {
		//For Storable
		def this() {
			this(null, null, 0, Color.white)
		}

		def this(source: Ray, renderOrigin: Vector3d, energy: Double) {
			this(source, renderOrigin, energy, Color.white)
		}

		def this(source: Ray, energy: Double, color: Color = Color.white) {
			this(source, source.origin, energy, color)
		}

		override def render(world: World) {
			//TODO: Source shouldn't be null, but Storable makes it so it's not thread safe :(
			if (hit != null && source != null) {
				world.addClientEntity(new EntityLaser(renderOrigin, hit.hit, color, power))
			}
		}
	}

	/**
	 * Handles the packets for waves.
	 */
	class WaveGridPacket extends PacketType[WaveGrid] {
		override def read(packet: Packet) {
			val worldID = packet.readString()
			val opWorld = Game.worlds().findWorld(worldID)
			if (opWorld.isPresent) {
				val world = opWorld.get
				val grid = WaveGrid(world)

				grid.graph = new SimpleDirectedGraph(classOf[DefaultEdge])

				//Read graph
				(0 until packet.readInt())
					.foreach(i => {
					val path = List.empty[Wave]
					(0 until packet.readInt())
						.foreach(j => {
						val wave = packet.readStorable().asInstanceOf[Wave]

						grid.graph.addVertex(wave)
						path.lastOption match {
							case Some(last) => grid.graph.addEdge(last, wave)
							case _ =>
						}
					})
				})
			}
			else {
				throw new NovaException("Failed to read wave graph for invalid world: " + opWorld)
			}
		}

		override def write(handler: WaveGrid, packet: Packet) {
			packet.writeString(handler.world.getID)
			//Write graph.
			val paths = handler.getWavePaths
			packet.writeInt(paths.size)

			paths.foreach(path => {
				packet.writeInt(path.size)
				path.foreach(packet.writeStorable)
			})
		}

		override def isHandlerFor(handler: AnyRef): Boolean = handler.isInstanceOf[WaveGrid]
	}

}

class WaveGrid(val world: World) extends Updater {

	var graph = new SimpleDirectedGraph[Wave, DefaultEdge](classOf[DefaultEdge])

	private var graphChanged = true

	Game.syncTicker().add(this)

	/**
	 * Gets the set of wave sources
	 */
	def waveSources: Set[Wave] = graph.vertexSet().filter(v => graph.sourcesOf(v).isEmpty).toSet

	/**
	 * Gets the set of wave targets
	 */
	def waveTargets: Set[Wave] = graph.vertexSet().filter(v => graph.targetsOf(v).isEmpty).toSet

	/**
	 * @return The set of paths formed by all waves
	 */
	def getWavePaths: Set[List[Wave]] = {
		var orderedPaths = Set.empty[List[Wave]]

		//Find each wave path
		waveSources
			.foreach(
				source => {
					var orderedPath = List.empty[Wave]
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
	def create(laser: Electromagnetic, from: Electromagnetic = null) {
		graph synchronized {
			//Do ray trace
			if (laser.power > WaveGrid.minEnergy) {
				//Mark node in graph
				graph.addVertex(laser)
				graphChanged = true

				if (from != null) {
					graph.addEdge(from, laser)
				}

				val opHit = laser.fire(world)

				if (opHit.isPresent) {
					opHit.get match {
						case hit: RayTraceBlockResult =>
							val hitVec = hit.hit
							val hitBlockPos = hit.block.position

							//TODO: Render laser to the hit position
							//TODO: Load rendering to client ticker (packets)
							hit.block match {
								/**
								 * Handle other laser handlers
								 */
								case hitBlock if hitBlock.has(classOf[WaveHandler]) =>
									hitBlock.get(classOf[WaveHandler]).receive(laser)
								//Electrodynamics.proxy.renderLaser(world, laser.renderOrigin, hitVec, laser.color, laser.energy)
								/**
								 * Change laser.color when hit glass
								 */
								case hitBlock if hitBlock.getID.equals("glass") =>
									//Electrodynamics.proxy.renderLaser(world, laser.renderOrigin, hitVec, laser.color, laser.energy)
									var newColor = laser.color

									//TODO: Check block IDs
									if (hitBlock.getID.equals("stainedGlass") || hitBlock.getID.equals("stainedGlassPane")) {
										//val dyeColor = new laser.color(ItemDye.field_150922_c(blockToDye(hitMetadata)))
										//newColor = new Vector3d(dyeColor.getRed, dyeColor.getGreen, dyeColor.getBlue).normalize
									}
									//TODO: do refraction
									val refractiveIndex = 1
									create(new Electromagnetic(new Ray(hitVec + laser.source.dir * 0.9, laser.source.dir), hitVec, laser.power * 0.95, newColor.average(laser.color)), laser)
								case _ =>
							}
						case hit: RayTraceEntityResult =>
							if (laser.power > WaveGrid.minBurnEnergy) {
								val fireTime = (10 * (laser.power / WaveGrid.maxPower)).toInt

								if (fireTime > 0) {
									//hit.entity.setFire (fireTime)
									hit.entity.getOp(classOf[Damageable]).ifPresent(consumer(d => d.damage(20 * (laser.power / WaveGrid.maxPower))))
								}
							}
					}
				}
			}
		}
	}

	override def update(deltaTime: Double) {
		super.update(deltaTime)

		graph synchronized {
			if (Game.network().isServer) {
				//Find lasers that are not hitting another laser handler, but another block
				waveTargets
					.filter(v => v.hit.isInstanceOf[RayTraceBlockResult])
					.foreach(
						v => {
							//TODO: Handle furnace smelting
							val energyUsed = v.timeElapsed * v.power

							/**
							 * Mine the block
							 */
							val hit = v.hit.asInstanceOf[RayTraceBlockResult]
							val hitBlock = hit.block
							val energyRequiredToMineBlock = hitBlock.getHardness * WaveGrid.maxEnergyToMine

							//TODO: Render breaking effect
							//world.destroyBlockInWorldPartially(Block.blockRegistry.getIDForObject(hitBlock), hitBlockPos.x.toInt, hitBlockPos.y.toInt, hitBlockPos.z.toInt, (accumulatedEnergy / energyRequiredToMineBlock * 10).toInt)

							/*if (energyUsed >= energyRequiredToMineBlock) {
								//We can disintegrate the block!
								//TODO: Add drop event override to BWBlock
								val event = new DropEvent(hitBlock)
								hitBlock.dropEvent.publish(event)
								event.drops.foreach(drop => hitBlock.world.addEntity(hitBlock.position.toDouble + 0.5, drop))
								world.removeBlock(hitBlock.position)
								//Re-calculate raycast because block is now broken.
								v.fire(world)
							}*/
							//TODO: Burnable
							/**
							 * Catch Fire
						if (energyOnBlock > minBurnEnergy && hitBlock.getMaterial.getCanBurn) {
							if (hitBlock.isInstanceOf[BlockTNT]) {
								hitBlock.asInstanceOf[BlockTNT].func_150114_a(world, hitBlockPos.x.toInt, hitBlockPos.y.toInt, hitBlockPos.z.toInt, 1, null)
							}
							world.setBlock(hitBlockPos.x.toInt, hitBlockPos.y.toInt, hitBlockPos.z.toInt, Blocks.fire)
						}*/
						}
					)

				//TODO: Only update when graph changes
				//Update client
				if (graphChanged) {
					Game.network.sync(this)
					println("Sent wave packet")
					graphChanged = false
				}
			}
			else {
				/**
				 * Render all wave
				 */
				graph.vertexSet().foreach(wave => {
					//Recalculate raycast
					wave.fire(world)
					wave.render(world)
				})

				/**
				 * Render scorch and particles
				 */
				//Electrodynamics.proxy.renderScorch(world, hitVec - (direction * 0.02), hit.sideHit)
				//Electrodynamics.proxy.renderBlockParticle(world, hitVec, hitBlock, hit.sideHit)
			}
		}

	}

	/**
	 * Destroys the laser, removing all verticies in the graph.
	 * @param laser The laser to remove
	 */
	def destroy(laser: Electromagnetic) {
		if (graph.containsVertex(laser)) {
			val inspector = new ConnectivityInspector(graph)
			val connected = inspector.connectedSetOf(laser)
			graph.removeAllVertices(connected)
			graphChanged = true
		} else {
			Game.logger().error("Attempt to remove node that does not exist in wave grid.")
		}
	}

	def blockToDye(blockMeta: Int): Int = ~blockMeta & 15
}
