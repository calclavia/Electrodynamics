package com.calclavia.edx.optics.beam

import com.calclavia.edx.core.prefab.BlockEDX
import com.calclavia.edx.optics.content.{OpticsModels, OpticsTextures}
import com.calclavia.edx.optics.grid.OpticHandler.ReceiveBeamEvent
import com.calclavia.edx.optics.grid.{ElectromagneticBeam, OpticHandler}
import nova.core.block.Stateful
import nova.core.component.misc.Collider
import nova.core.component.renderer.{DynamicRenderer, ItemRenderer}
import nova.core.network.{Sync, Syncable}
import nova.core.render.model.Model
import nova.core.retention.{Storable, Store}
import nova.core.util.Ray
import nova.core.util.RayTracer.RayTraceBlockResult
import nova.scala.wrapper.FunctionalWrapper._
import nova.scala.wrapper.VectorWrapper._
import org.apache.commons.math3.geometry.euclidean.threed.{Rotation, Vector3D}

/**
 * Redirects beams to one point
 *
 * @author Calclavia
 */
//TODO: Add wrench focus
class BlockCrystal extends BlockEDX with Stateful with Syncable with Storable {

	@Store
	@Sync
	private val focus = components.add(new Focus(this))
	private val renderer = components.add(new DynamicRenderer())
	private val itemRenderer = components.add(new ItemRenderer(this))
	private val optic = components.add(new OpticHandler(this))

	collider.isCube(false)
	collider.isOpaqueCube(false)

	renderer.onRender(
		(model: Model) => {
			val subModel = OpticsModels.crystal.getModel
			//GL_SRC_ALPHA
			model.blendSFactor = 0x302
			//GL_ Minus One
			model.blendDFactor = 0x303
			model.matrix.rotate(new Rotation(new Vector3D(1, -1.5, 3), focus.normal).revert())
			model.children.add(subModel)
			subModel.bindAll(OpticsTextures.crystal)
		}
	)

	events.on(classOf[ReceiveBeamEvent]).bind(
		(evt: ReceiveBeamEvent) => {
			/**
			 * Change incoming render laser position
			 */
			val newHit = position + 0.5
			val newBound = components.get(classOf[Collider]).boundingBox.get + position
			evt.hit = new RayTraceBlockResult(newHit, evt.incident.source.origin.distance(newHit), evt.hit.side, newBound, this)
			evt.hasImpact = false

			/**
			 * New focus
			 */
			//Emit beam
			val newDirection = focus.normal
			val beam = new ElectromagneticBeam
			beam.world = world
			val traceStart = position + 0.5 + newDirection * 0.55
			beam.source = new Ray(traceStart, newDirection)
			beam.renderOffset = evt.hit.hit - traceStart
			beam.color = evt.incident.color
			beam.power = evt.receivingPower
			evt.continue(beam)
		}
	)
}
