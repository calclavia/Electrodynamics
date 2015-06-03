package com.calclavia.edx.electric.circuit.component.laser

import java.util
import java.util.Optional

import com.calclavia.edx.electric.circuit.component.laser.LaserGrid.Laser
import com.resonant.lib.WrapFunctions._
import nova.core.block.Block.DropEvent
import nova.core.component.Updater
import nova.core.component.misc.Damageable
import nova.core.game.Game
import nova.core.render.Color
import nova.core.util.RayTracer.{RayTraceBlockResult, RayTraceEntityResult, RayTraceResult}
import nova.core.util.transform.vector.Vector3d
import nova.core.util.{Ray, RayTracer}
import nova.core.world.World
import org.jgrapht.alg.ConnectivityInspector
import org.jgrapht.graph.{DefaultDirectedGraph, DefaultEdge}

import scala.collection.convert.wrapAll._

/**
 * @author Calclavia
 */
object LaserGrid {

	//Max laser render distance
	val maxDistance = 100

	//Minimum energy for laser
	val minEnergy = 100d
	val maxEnergy = 20000d

	val minEnergyToMine = 10000d
	val maxEnergyToMine = 500000d
	val minBurnEnergy = minEnergyToMine

	private val grids = new util.WeakHashMap[World, LaserGrid]

	def apply(world: World): LaserGrid = {
		if (!grids.containsKey(world)) {
			grids += (world -> new LaserGrid(world))
		}

		return grids(world)
	}

	class Beam(val source: Ray, val renderOrigin: Vector3d, val power: Double, val color: Color) {
		def computeHit(world: World) =
			new RayTracer(source)
				.setDistance(maxDistance)
				.rayTraceAll(world)
				.findFirst()
	}

	class Laser(source: Ray, renderOrigin: Vector3d, power: Double, color: Color) extends Beam(source, renderOrigin, power, color) {

		var hit: RayTraceResult = null
		var hitTime = -1L

		def this(source: Ray, renderOrigin: Vector3d, energy: Double) {
			this(source, renderOrigin, energy, Color.white)
		}

		def this(source: Ray, energy: Double, color: Color = Color.white) {
			this(source, source.origin, energy, color)
		}

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
	}

}

class LaserGrid(world: World) extends Updater {

	val laserGraph = new DefaultDirectedGraph[Laser, DefaultEdge](classOf[DefaultEdge])

	Game.syncTicker().add(this)

	/**
	 * Creates a laser emission point
	 */
	def create(laser: Laser, from: Laser = null) {
		//Do ray trace
		if (laser.power > LaserGrid.minEnergy) {
			//Mark node in graph
			laserGraph.addVertex(laser)

			if (from != null) {
				laserGraph.addEdge(from, laser)
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
							case hitBlock if hitBlock.has(classOf[LaserHandler]) =>
								hitBlock.get(classOf[LaserHandler]).receive(laser)
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
								create(new Laser(new Ray(hitVec + laser.source.dir * 0.9, laser.source.dir), hitVec, laser.power * 0.95, newColor.average(laser.color)), laser)
						}
					case hit: RayTraceEntityResult =>
						if (laser.power > LaserGrid.minBurnEnergy) {
							val fireTime = (10 * (laser.power / LaserGrid.maxEnergy)).toInt

							if (fireTime > 0) {
								//hit.entity.setFire (fireTime)
								hit.entity.getOp(classOf[Damageable]).ifPresent(consumer(d => d.damage(20 * (laser.power / LaserGrid.maxEnergy))))
							}
						}
					//Electrodynamics.proxy.renderLaser (world, laser.renderOrigin, new Vector3d (hit.hitVec), laser.color, laser.energy)
				}
			} else {
				//Electrodynamics.proxy.renderLaser(world, laser.renderOrigin, maxPos, laser.color, energy)
			}
		}
	}

	override def update(deltaTime: Double) {
		super.update(deltaTime)

		if (Game.network().isServer) {
			val loneLasers = laserGraph.vertexSet()
				.filter(v => laserGraph.outgoingEdgesOf(v).size == 0)

			//Find lasers that are not hitting another laser handler, but another block
			loneLasers
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
						val energyRequiredToMineBlock = hitBlock.getHardness * LaserGrid.maxEnergyToMine

						//TODO: Render breaking effect
						//world.destroyBlockInWorldPartially(Block.blockRegistry.getIDForObject(hitBlock), hitBlockPos.x.toInt, hitBlockPos.y.toInt, hitBlockPos.z.toInt, (accumulatedEnergy / energyRequiredToMineBlock * 10).toInt)

						if (energyUsed >= energyRequiredToMineBlock) {
							//We can disintegrate the block!
							val event = new DropEvent(hitBlock)
							hitBlock.dropEvent.publish(event)
							event.drops.foreach(drop => hitBlock.world.addEntity(hitBlock.position.toDouble + 0.5, drop))
							world.removeBlock(hitBlock.position)
							//Re-calculate raycast because block is now broken.
							v.computeHit(world)
						}
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
		}
		else {
			//TODO: Traverse all graph nodes, render laser fxs
			/**
			 * Render laser hit
			 */
			//Electrodynamics.proxy.renderLaser(world, laser.renderOrigin, hitVec, laser.color, laser.energy)

			/**
			 * Render scorch and particles
			 */
			//Electrodynamics.proxy.renderScorch(world, hitVec - (direction * 0.02), hit.sideHit)
			//Electrodynamics.proxy.renderBlockParticle(world, hitVec, hitBlock, hit.sideHit)
		}
	}

	/**
	 * Destroys the laser, removing all verticies in the graph.
	 * @param laser The laser to remove
	 */
	def destroy(laser: Laser) {
		val inspector = new ConnectivityInspector(laserGraph)
		val connected = inspector.connectedSetOf(laser)
		laserGraph.removeAllVertices(connected)
	}

	def blockToDye(blockMeta: Int): Int = ~blockMeta & 15
}
