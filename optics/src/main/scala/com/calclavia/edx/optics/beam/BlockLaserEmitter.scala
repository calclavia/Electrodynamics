package com.calclavia.edx.optics.beam

import java.util.function.Supplier
import java.util.{Set => JSet}

import com.calclavia.edx.core.prefab.BlockEDX
import com.calclavia.edx.electric.api.{ConnectionBuilder, Electric}
import com.calclavia.edx.electric.grid.NodeElectricComponent
import com.calclavia.edx.optics.content.{OpticsModels, OpticsTextures}
import com.calclavia.edx.optics.grid.{ElectromagneticBeam, OpticGrid, OpticHandler}
import nova.scala.wrapper.FunctionalWrapper
import FunctionalWrapper._
import nova.core.block.Block.RightClickEvent
import nova.core.block.Stateful
import nova.core.block.component.{LightEmitter, StaticBlockRenderer}
import nova.core.component.renderer.ItemRenderer
import nova.core.component.transform.Orientation
import nova.core.event.Event
import com.calclavia.edx.core.EDX
import nova.core.network.{Sync, Packet, Syncable}
import nova.core.render.model.Model
import nova.core.retention.{Store, Data, Storable}
import nova.core.util.transform.matrix.Quaternion
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D
import nova.core.util.{Direction, Ray}
import nova.scala.component.IO
import nova.scala.util.ExtendedUpdater

/**
 * An emitter that shoots out lasers.
 *
 * Consider: E=hf. Higher frequency light has more energy.
 *
 * @author Calclavia
 */
class BlockLaserEmitter extends BlockEDX with Stateful with ExtendedUpdater with Storable with Syncable {
	private val electricNode = add(new NodeElectricComponent(this))
	@Store
	@Sync
	private val orientation = add(new Orientation(this)).hookBlockEvents()
	private val optic = add(new OpticHandler(this))
	@Store
	@Sync
	private val io = add(new IO(this))
	private val renderer = add(new StaticBlockRenderer(this))
	private val itemRenderer = add(new ItemRenderer(this))
	private val lightEmitter = add(new LightEmitter())

	orientation.setMask(0x3F)

	electricNode.setPositiveConnections(
		new ConnectionBuilder(classOf[Electric])
			.setBlock(this)
			.setConnectMask(supplier(() => io.inputMask))
			.adjacentWireSupplier()
			.asInstanceOf[Supplier[JSet[Electric]]]
	)
	electricNode.setNegativeConnections(
		new ConnectionBuilder(classOf[Electric])
			.setBlock(this)
			.setConnectMask(supplier(() => io.outputMask))
			.adjacentWireSupplier()
			.asInstanceOf[Supplier[JSet[Electric]]]
	)
	electricNode.setResistance(100)

	collider.isCube(false)
	collider.isOpaqueCube(false)

	lightEmitter.setEmittedLevel(supplier(() => (electricNode.power / OpticGrid.maxPower).toFloat))

	orientation.onOrientationChange.add((evt: Event) => {
		if (EDX.network.isServer) {
			io.setIOAlternatingOrientation()
			electricNode.rebuild()
			EDX.network.sync(this)
		}
		else {
			world.markStaticRender(position)
		}
	})

	rightClickEvent.add((evt: RightClickEvent) => {
		optic.destroy()
	})

	renderer.setOnRender(
		(model: Model) => {
			val rot = orientation.orientation match {
				case Direction.UP => Quaternion.fromAxis(Vector3D.PLUS_I, -Math.PI / 2)
				case Direction.DOWN => Quaternion.fromAxis(Vector3D.PLUS_I, Math.PI / 2)
				case Direction.SOUTH => Quaternion.fromAxis(Vector3D.PLUS_J, Math.PI / 2)
				case Direction.NORTH => Quaternion.fromAxis(Vector3D.PLUS_J, -Math.PI / 2)
				case Direction.WEST => Quaternion.fromAxis(Vector3D.PLUS_J, Math.PI)
				case Direction.EAST => Quaternion.fromAxis(Vector3D.PLUS_J, 0)
				case _ => Quaternion.identity
			}

			model.rotate(rot)

			if (orientation.orientation.y == 0) {
				model.rotate(Vector3D.PLUS_J, -Math.PI / 2)
			}
			else {
				model.rotate(Vector3D.PLUS_I, Math.PI)
			}

			model.children.add(OpticsModels.laserEmitterModel.getModel)
			model.bindAll(OpticsTextures.laserEmitterTexture)
		}
	)

	override def update(deltaTime: Double) {
		super.update(deltaTime)

		if (EDX.network.isServer) {
			if (electricNode.power > 0) {
				val dir = orientation.orientation.toVector
				val beam = new ElectromagneticBeam()
				beam.world = world
				beam.source = new Ray(position + 0.5 + dir * 0.51, dir)
				beam.renderOffset = -dir * 0.31
				beam.power = electricNode.power
				optic.create(beam)
			}
			else {
				optic.destroy()
			}
		}
	}

	override def load(data: Data) {
		super.load(data)
		world.markStaticRender(position)
	}

	override def read(packet: Packet) {
		super.read(packet)
		world.markStaticRender(position)
	}

	override def getID: String = "laserEmitter"
}
