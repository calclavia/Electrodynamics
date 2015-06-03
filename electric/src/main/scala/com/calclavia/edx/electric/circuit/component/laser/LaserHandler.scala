package com.calclavia.edx.electric.circuit.component.laser

import com.calclavia.edx.electric.circuit.component.laser.LaserHandler.Laser
import com.resonant.lib.WrapFunctions._
import nova.core.block.Block
import nova.core.block.Block.DropEvent
import nova.core.block.Stateful.{LoadEvent, UnloadEvent}
import nova.core.component.Component
import nova.core.component.misc.Damageable
import nova.core.event.{Event, EventBus}
import nova.core.game.Game
import nova.core.network.NetworkTarget.Side
import nova.core.render.Color
import nova.core.util.RayTracer.{RayTraceBlockResult, RayTraceEntityResult}
import nova.core.util.transform.vector.{Vector3d, Vector3i}
import nova.core.util.{Ray, RayTracer}

import scala.collection.convert.wrapAll._

/**
 * Handles laser interaction
 * @author Calclavia
 */
object LaserHandler {
	val maxDistance = 100

	val minEnergy = 100d
	val maxEnergy = 20000d

	val minEnergyToMine = 10000d
	val maxEnergyToMine = 500000d
	val minBurnEnergy = minEnergyToMine

	//TODO: Conflict with different worlds?
	var currentBlockEnergy = Map.empty[Vector3i, Double]
	var accumulateBlockEnergy = Map.empty[Vector3i, Double]
	var lastUpdateTime = 0L

	class Laser(val source: Ray, val renderOrigin: Vector3d, val energy: Double, val color: Color) {

		def this(source: Ray, renderOrigin: Vector3d, energy: Double) {
			this(source, renderOrigin, energy, Color.white)
		}

		def this(source: Ray, energy: Double, color: Color = Color.white) {
			this(source, source.origin, energy, color)
		}
	}

}

class LaserHandler(block: Block) extends Component {

	var onEnergyChange = new EventBus[Event]

	/**
	 * All incident lasers on this handler
	 */
	var incidentLasers = Seq.empty[Laser]

	/**
	 * The laser currently being emitted
	 */
	private var emittingLaser: Laser = null

	//TODO: Handle laser states
	//Hook block events.
	block.loadEvent.add(
		(evt: LoadEvent) => {
			//Wait for next tick
			if (Side.get().isServer) {
				Game.syncTicker().preQueue(() => build())
			}
		}
	)
	block.unloadEvent.add(
		(evt: UnloadEvent) => {
			//Destroy laser
		}
	)

	private def build() {
		//Register laser grid
	}

	/**
	 * The current energy being received
	 */
	def energyReceiving = 0d

	def receive(renderStart: Vector3d, incident: Ray, hit: RayTracer.RayTraceBlockResult, color: Color, energy: Double): Boolean = {
		false
	}

	def emit(laser: Laser) {
		if (laser.energy > LaserHandler.minEnergy) {

			val maxPos = laser.source.origin + (laser.source.dir * LaserHandler.maxDistance)
			val opHit = new RayTracer(laser.source)
				.setDistance(LaserHandler.maxDistance)
				.rayTraceAll(block.world)
				.findFirst()

			if (opHit.isPresent) {
				opHit.get match {
					case hit: RayTraceBlockResult =>
						val hitVec = hit.hit
						val hitBlockPos = hit.block.position

						hit.block match {

							/**
							 * Handle reflectors
							 */
							case hitBlock if hitBlock.has(classOf[LaserHandler]) =>
								if (!hitBlock.get(classOf[LaserHandler]).receive(laser.renderOrigin, laser.source, hit, laser.color, laser.energy)) {
									//Electrodynamics.proxy.renderLaser(world, laser.renderOrigin, hitVec, laser.color, laser.energy)
								}

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

								//TODO: Refraction
								val refractiveIndex = 1
								emit(new Laser(new Ray(hitVec + laser.source.dir * 0.9, laser.source.dir), hitVec, laser.energy * 0.95, newColor.average(laser.color)))

							/**
							 * Attempt to burn block
							 */
							case hitBlock =>
								if (Game.network.isServer) {
									val hardness = hitBlock.getHardness

									if (hardness != Double.PositiveInfinity) {
										if (LaserHandler.lastUpdateTime != System.currentTimeMillis) {
											LaserHandler.currentBlockEnergy = Map.empty
											LaserHandler.lastUpdateTime = System.currentTimeMillis
										}

										val energyOnBlock = (if (LaserHandler.currentBlockEnergy.contains(hitBlockPos)) LaserHandler.currentBlockEnergy(hitBlockPos) else 0) + laser.energy
										LaserHandler.currentBlockEnergy += (hitBlockPos -> energyOnBlock)

										/*
										if (hitTile.isInstanceOf[TileEntityFurnace]) {
											/**
											 * Cook in furnace
											 */
											val furnace = hitTile.asInstanceOf[TileEntityFurnace]

											try {
												if (ReflectionHelper.findMethod(classOf[TileEntityFurnace], furnace, Array("canSmelt", "func_145948_k")).invoke(furnace).asInstanceOf[Boolean]) {
													furnace.furnaceBurnTime = Math.max(2, furnace.furnaceBurnTime)
													furnace.furnaceCookTime = Math.min(199, furnace.furnaceCookTime + (15 * (laser.energy / maxEnergy)).toInt)
												}
											}
											catch {
												case e: Exception => e.printStackTrace()
											}
										}
										else*/
										{
											if (energyOnBlock > LaserHandler.minEnergyToMine) {
												/**
												 * Mine the block
												 */
												val accumulatedEnergy = (if (LaserHandler.accumulateBlockEnergy.contains(hitBlockPos)) LaserHandler.accumulateBlockEnergy(hitBlockPos) else 0) + laser.energy
												LaserHandler.accumulateBlockEnergy.put(hitBlockPos, accumulatedEnergy)

												val energyRequiredToMineBlock = hardness * LaserHandler.maxEnergyToMine

												//TODO: Render breaking effect
												//world.destroyBlockInWorldPartially(Block.blockRegistry.getIDForObject(hitBlock), hitBlockPos.x.toInt, hitBlockPos.y.toInt, hitBlockPos.z.toInt, (accumulatedEnergy / energyRequiredToMineBlock * 10).toInt)

												if (accumulatedEnergy > energyRequiredToMineBlock) {
													val event = new DropEvent(hitBlock)
													hitBlock.dropEvent.publish(event)
													event.drops.foreach(drop => block.world.addEntity(hitBlockPos.toDouble + 0.5, drop))
													block.world.removeBlock(hitBlock.position)
													LaserHandler.accumulateBlockEnergy.remove(hitBlockPos)
												}
											}
											else {
												//	LaserHandler.accumulateBlockEnergy.remove(hitBlockPos)
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
									}
								}

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

					case hit: RayTraceEntityResult =>
						if (laser.energy > LaserHandler.minBurnEnergy) {
							val fireTime = (10 * (laser.energy / LaserHandler.maxEnergy)).toInt

							if (fireTime > 0) {
								//hit.entity.setFire (fireTime)
								hit.entity.getOp(classOf[Damageable]).ifPresent(consumer(d => d.damage(20 * (laser.energy / LaserHandler.maxEnergy))))
							}
						}

					//Electrodynamics.proxy.renderLaser (world, laser.renderOrigin, new Vector3d (hit.hitVec), laser.color, laser.energy)
				}
			} else {
				//Electrodynamics.proxy.renderLaser(world, laser.renderOrigin, maxPos, laser.color, energy)
			}
		}
	}

	def blockToDye(blockMeta: Int): Int = ~blockMeta & 15
}
