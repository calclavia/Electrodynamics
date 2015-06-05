package com.calclavia.edx.optics.grid

import com.calclavia.edx.optics.grid.OpticHandler.ReceiveBeamEvent
import nova.core.component.Updater
import nova.core.game.Game
import nova.core.render.Color
import nova.core.retention.{Data, Storable, Stored}
import nova.core.util.RayTracer.{RayTraceBlockResult, RayTraceEntityResult, RayTraceResult}
import nova.core.util.transform.vector.Vector3d
import nova.core.util.{Ray, RayTracer}
import nova.core.world.World

/**
 * A wave can travel as a focused beam.
 * @author Calclavia
 */
abstract class Beam extends Storable with Updater {

	var world: World = _

	/**
	 * The ray that defines the wave.
	 */
	var source: Ray = _

	/**
	 * The render offset position for the wave.
	 */
	@Stored
	var renderOffset = Vector3d.zero

	/**
	 * The power of the wave.
	 */
	@Stored
	var power = 0d

	/**
	 * The color of the wave.
	 */
	var color = Color.white

	/**
	 * The time since the ray trace changed target.
	 */
	private var hitTime = -1L

	private var prevHit: RayTraceResult = null

	def rayTrace =
		new RayTracer(source)
			.setDistance(OpticGrid.maxDistance)
			.rayTraceAll(world)
			.findFirst()

	def timeElapsed = (System.currentTimeMillis - hitTime) / 1000d

	override def save(data: Data) {
		super.save(data)
		data.put("origin", source.origin)
		data.put("dir", source.dir)
		data.put("renderOffset", renderOffset)
		data.put("color", color.rgba())
	}

	override def load(data: Data) {
		super.load(data)
		source = new Ray(data.getStorable("origin"), data.getStorable("dir"))
		renderOffset = data.getStorable("renderOffset")
		color = Color.rgba(data.get("color"))
	}

	override def update(deltaTime: Double) {
		super.update(deltaTime)

		val opHit = rayTrace

		//Check with previous hit and compute hit time
		if ((opHit.isPresent && prevHit != null && opHit.get().hit.equals(prevHit.hit)) || (!opHit.isPresent || prevHit == null)) {
			hitTime = System.currentTimeMillis
			prevHit = opHit.orElse(null)
		}

		if (opHit.isPresent) {
			opHit.get match {
				case hit: RayTraceBlockResult =>
					//We hit a block
					hit.block match {
						/**
						 * Let the optic handler handle the beam
						 */
						case hitBlock if hitBlock.has(classOf[OpticHandler]) =>
							hitBlock.get(classOf[OpticHandler]).onReceive.publish(new ReceiveBeamEvent(this, hit))

						/**
						 * All beams refract and reflect
						 */
						case hitBlock if hitBlock.getID.equals("glass") =>
							var newColor = color

							//TODO: Check block IDs
							if (hitBlock.getID.equals("stainedGlass") || hitBlock.getID.equals("stainedGlassPane")) {
								//val dyeColor = new laser.color(ItemDye.field_150922_c(blockToDye(hitMetadata)))
								//newColor = new Vector3d(dyeColor.getRed, dyeColor.getGreen, dyeColor.getBlue).normalize
							}

							//TODO: Energy loss
							//TODO: do refraction
							val refractiveIndex = 1
						//create(new Electromagnetic(new Ray(hitVec + laser.source.dir * 0.9, laser.source.dir), hitVec, laser.power * 0.95, newColor.average(laser.color)), laser)
						case _ => onHitBlock(hit)
					}
				case hit: RayTraceEntityResult => onHitEntity(hit)
			}
		}

		if (Game.network.isClient) {
			render(opHit.orElse(null))
		}
	}

	def onHitBlock(rayTrace: RayTraceBlockResult)

	def onHitEntity(rayTrace: RayTraceEntityResult)

	/**
	 * Renders the beam
	 * @param hit Null if nothing is hit.
	 */
	def render(hit: RayTraceResult)

	override def equals(obj: scala.Any): Boolean = {
		if (obj.isInstanceOf[Beam]) {
			val otherWave = obj.asInstanceOf[Beam]
			return source.origin.equals(otherWave.source.origin) &&
				source.dir.equals(otherWave.source.dir) &&
				renderOffset.equals(otherWave.renderOffset) &&
				color.equals(otherWave.color) &&
				power.equals(otherWave.power)
		}
		return false
	}
}