package com.calclavia.edx.optics.beam

import com.calclavia.edx.core.EDX
import com.calclavia.edx.core.prefab.BlockEDX
import com.calclavia.edx.optics.content.{OpticsModels, OpticsTextures}
import com.calclavia.edx.optics.grid.OpticHandler.ReceiveBeamEvent
import com.calclavia.edx.optics.grid.{ElectromagneticBeam, OpticHandler}
import nova.core.block.Block.RightClickEvent
import nova.core.block.Stateful
import nova.core.block.component.StaticBlockRenderer
import nova.core.component.misc.Collider
import nova.core.component.renderer.ItemRenderer
import nova.core.network.{Packet, Sync, Syncable}
import nova.core.render.model.Model
import nova.core.retention.{Storable, Store}
import nova.core.util.Ray
import nova.core.util.RayTracer.RayTraceBlockResult
import nova.core.util.math.Vector3DUtil
import nova.scala.wrapper.FunctionalWrapper._
import nova.scala.wrapper.VectorWrapper._
import org.apache.commons.math3.geometry.euclidean.threed.{Rotation, Vector3D}

/**
 * A mirror reflects lasers.
 *
 * TODO: Make it actually reflect light (render reflection)
 *
 * @author Calclavia
 */
class BlockMirror extends BlockEDX with Stateful with Syncable with Storable {
	@Store
	@Sync
	private val focus = add(new Focus(this))
	private val renderer = add(new StaticBlockRenderer(this))
	private val itemRenderer = add(new ItemRenderer(this))
	private val optic = add(new OpticHandler(this))

	renderer.setOnRender(
		(model: Model) => {
			model.matrix.rotate(new Rotation(Vector3DUtil.FORWARD, focus.normal).revert())
			model.matrix.rotate(Vector3D.PLUS_I, Math.PI / 2)

			val child = OpticsModels.mirrorModel.getModel.combineChildren("mirror", "mirror", "mirrorBacking", "standConnector")
			model.children.add(child)
			model.bindAll(OpticsTextures.mirrorTexture)
		}
	)
	collider.isCube(false)
	collider.isOpaqueCube(false)

	rightClickEvent.add((evt: RightClickEvent) => EDX.network.sync(this))

	optic.onReceive.add(
		(evt: ReceiveBeamEvent) => {
			/**
			 * Change incoming render laser position
			 */
			val newHit = position + 0.5
			val newBound = get(classOf[Collider]).boundingBox.get + position
			evt.hit = new RayTraceBlockResult(newHit, evt.incident.source.origin.distance(newHit), evt.hit.side, newBound, this)
			evt.hasImpact = false

			/**
			 * Calculate Reflection
			 */
			val incidentDirection = evt.incident.source.dir
			val angle = Math.acos(incidentDirection.dotProduct(focus.normal))

			val axisOfReflection = incidentDirection.crossProduct(focus.normal)
			val rotateAngle = 2 * angle - Math.PI

			if (rotateAngle < Math.PI) {
				//Emit beam
				val newDirection = new Rotation(axisOfReflection, rotateAngle).applyTo(incidentDirection)
				val beam = new ElectromagneticBeam
				beam.world = world
				val traceStart = position + 0.5 + newDirection * 0.52
				beam.source = new Ray(traceStart, newDirection)
				beam.renderOffset = evt.hit.hit - traceStart
				beam.color = evt.incident.color
				beam.power = evt.receivingPower
				evt.continue(beam)
			}
		})

	override def getID: String = "mirror"

	override def read(packet: Packet) {
		super.read(packet)
		world.markStaticRender(position)
	}
}
