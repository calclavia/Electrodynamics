package com.calclavia.edx.optics.beam

import com.calclavia.edx.core.EDX
import com.calclavia.edx.core.prefab.BlockEDX
import nova.core.block.Block.RightClickEvent
import nova.core.block.Stateful
import nova.core.block.component.StaticBlockRenderer
import nova.core.component.renderer.ItemRenderer
import nova.core.network.{Packet, Sync, Syncable}
import nova.core.render.model.Model
import nova.core.retention.{Storable, Store}
import nova.core.util.Ray
import nova.core.util.transform.matrix.Rotation
import nova.scala.wrapper.FunctionalWrapper
import nova.scala.wrapper.FunctionalWrapper._

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

	private var cachedHits = List[Vector3D]()

	renderer.setOnRender(
		(model: Model) => {
			/*
			val angle = normal.toEulerAngle
			glRotated(angle.yaw, 0, 1, 0)
			glRotated(angle.pitch, 1, 0, 0)
			glRotated(90, 1, 0, 0)
			*/
			model.rotate(Rotation.fromDirection(focus.normal))
			model.rotate(Vector3D.PLUS_I, Math.PI / 2)

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
			 * Render incoming laser
			 */
			//TODO: Change render endpoint
			//Electrodynamics.proxy.renderLaser(worldObj, renderStart, position + 0.5, color, energy)

			/**
			 * Calculate Reflection
			 */
			val incidentDirection = evt.incident.source.dir
			val angle = Math.acos(incidentDirection.dotProduct(focus.normal))

			val axisOfReflection = incidentDirection * focus.normal
			val rotateAngle = 2 * angle - Math.PI

			if (rotateAngle < Math.PI) {
				//Emit beam
				val newDirection = incidentDirection.transform(Rotation.fromAxis(axisOfReflection, rotateAngle)).normalize
				val beam = new ElectromagneticBeam
				beam.world = world
				beam.source = new Ray(position + 0.5 + newDirection * 0.9, newDirection)
				beam.renderOffset = -newDirection * 0.9
				beam.color = evt.incident.color
				beam.power = evt.receivingPower
				evt.continue(beam)
			}
		})

	//TODO: Think of better ways to control mirror
	/*
	override def update() {
		if (isPowered) {
			for (a <- 0 to 5) {
				val dir = Direction.getOrientation(a)
				val axis = new Vector3d(dir)
				val rotateAngle = world.getIndirectPowerLevelTo(x + axis.x.toInt, y + axis.y.toInt, z + axis.z.toInt, a) * 15

				if (rotateAngle > 0) {
					normal = normal.transform(new Quaternion(Math.toRadians(rotateAngle), axis)).normalize
				}
			}

			world.markBlockForUpdate(x, y, z)
		}

		if (world.getTotalWorldTime % 20 == 0) {
			cachedHits = List()
		}
	}*/

	override def getID: String = "mirror"

	override def read(packet: Packet) {
		super.read(packet)
		world.markStaticRender(position)
	}
}
