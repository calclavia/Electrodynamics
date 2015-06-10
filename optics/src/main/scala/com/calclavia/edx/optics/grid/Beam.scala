package com.calclavia.edx.optics.grid

import java.util.Optional

import com.calclavia.edx.core.EDX
import com.calclavia.edx.optics.grid.OpticHandler.ReceiveBeamEvent
import nova.core.render.Color
import nova.core.retention.{Data, Storable, Store}
import nova.core.util.RayTracer.{RayTraceBlockResult, RayTraceEntityResult, RayTraceResult}
import nova.core.util.{Ray, RayTracer}
import nova.core.world.World
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D

/**
 * A wave can travel as a focused beam.
 * @author Calclavia
 */
abstract class Beam extends Storable {

	var world: World = _

	/**
	 * The ray that defines the wave.
	 */
	var source: Ray = _

	/**
	 * The render offset position for the wave.
	 */
	@Store
	var renderOffset = Vector3D.ZERO

	/**
	 * The power of the wave.
	 */
	@Store
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
		data.put("color", color.argb())
	}

	override def load(data: Data) {
		super.load(data)
		source = new Ray(data.get("origin"), data.get("dir"))
		renderOffset = data.get("renderOffset")
		color = Color.argb(data.get("color"))
	}

	def update() {
		var opHit = rayTrace

		//Check with previous hit and compute hit time
		if ((opHit.isPresent && prevHit != null && opHit.get().hit.equals(prevHit.hit)) || (!opHit.isPresent || prevHit == null)) {
			hitTime = System.currentTimeMillis
			prevHit = opHit.orElse(null)
		}

		var hasImpact = true

		if (opHit.isPresent) {
			opHit.get match {
				case hit: RayTraceBlockResult =>
					//We hit a block
					hit.block match {
						/**
						 * Let the optic handler handle the beam
						 */
						case hitBlock if hitBlock.has(classOf[OpticHandler]) =>
							val event = new ReceiveBeamEvent(this, hit)
							hitBlock.get(classOf[OpticHandler]).onReceive.publish(event)
							opHit = Optional.of(event.hit)
							hasImpact = event.hasImpact

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

		if (EDX.network.isClient) {
			render(opHit.orElse(null), hasImpact)
		}
	}

	def onHitBlock(rayTrace: RayTraceBlockResult)

	def onHitEntity(rayTrace: RayTraceEntityResult)

	/**
	 * Renders the beam
	 * @param hit Null if nothing is hit.
	 */
	def render(hit: RayTraceResult, hasImpact: Boolean)

	def sameAs(other: Beam) =
		source.origin.equals(other.source.origin) &&
			source.dir.equals(other.source.dir) &&
			renderOffset.equals(other.renderOffset) &&
			color.equals(other.color) &&
			power == other.power

}