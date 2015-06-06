package com.calclavia.edx.optics.beam

import com.calclavia.edx.core.prefab.BlockEDX
import com.calclavia.edx.optics.content.{OpticsModels, OpticsTextures}
import com.calclavia.edx.optics.grid.OpticHandler
import com.calclavia.edx.optics.grid.OpticHandler.ReceiveBeamEvent
import com.resonant.lib.WrapFunctions._
import nova.core.block.Block.RightClickEvent
import nova.core.block.Stateful
import nova.core.block.component.StaticBlockRenderer
import nova.core.game.Game
import nova.core.network.{Packet, Sync, Syncable}
import nova.core.render.model.Model
import nova.core.retention.{Storable, Store}
import nova.core.util.transform.matrix.Quaternion
import nova.core.util.transform.vector.Vector3d

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
	private val optic = add(new OpticHandler(this))

	private var cachedHits = List[Vector3d]()

	renderer.setOnRender(
		(model: Model) => {
			/*
			val angle = normal.toEulerAngle
			glRotated(angle.yaw, 0, 1, 0)
			glRotated(angle.pitch, 1, 0, 0)
			glRotated(90, 1, 0, 0)
			*/
			model.rotate(Quaternion.fromDirection(focus.normal))
			model.rotate(Vector3d.xAxis, Math.PI / 2)

			val child = OpticsModels.mirrorModel.getModel.combineChildren("mirror", "mirror", "mirrorBacking", "standConnector")
			model.children.add(child)
			model.bindAll(OpticsTextures.mirrorTexture)
		}
	)
	collider.isCube(false)
	collider.isOpaqueCube(false)

	rightClickEvent.add((evt: RightClickEvent) => Game.network().sync(this))

	optic.onReceive.add((evt: ReceiveBeamEvent) => {
		/**
		 * Render incoming laser
		 */
		//TODO: Change render endpoint
		//Electrodynamics.proxy.renderLaser(worldObj, renderStart, position + 0.5, color, energy)

		/**
		 * Calculate Reflection
		 */
		val incidentDirection = evt.incident.source.dir
		val angle = Math.acos(incidentDirection dot (focus.normal))

		val axisOfReflection = incidentDirection * focus.normal
		val rotateAngle = 2 * angle - Math.PI

		if (rotateAngle < Math.PI) {
			//Emit beam
			val newDirection = (incidentDirection.clone.transform(new Quaternion(rotateAngle, axisOfReflection))).normalize
			Laser.spawn(worldObj, position + 0.5 + newDirection * 0.9, position + 0.5, newDirection, color, energy / 1.2)
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
