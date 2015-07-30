package com.calclavia.edx.mechanical.content.gear

import java.util.Optional

import com.calclavia.edx.core.prefab.BlockEDX
import com.calclavia.edx.mechanical.{MechanicContent, Watch}
import nova.core.block.Block.{PlaceEvent, RightClickEvent}
import nova.core.component.renderer.{DynamicRenderer, ItemRenderer}
import nova.core.network.{Packet, Sync, Syncable}
import nova.core.render.model.{Model, MeshModel}
import nova.core.retention.{Storable, Store}
import nova.core.util.Direction
import nova.core.util.math.{MatrixStack, Vector3DUtil}
import nova.core.util.shape.Cuboid
import nova.microblock.micro.{Microblock, MicroblockContainer}
import nova.scala.wrapper.FunctionalWrapper._
import org.apache.commons.math3.geometry.euclidean.threed.{Rotation, Vector3D}
import org.apache.commons.math3.linear.RealMatrix

object BlockGear {
	val thickness = 2 / 16d
	val occlusionBounds = Array.ofDim[Cuboid](6)
	val matrixes = Array.ofDim[RealMatrix](6)

	{
		val oneByRoot2 = 1 / Math.sqrt(2)
		val center = new Cuboid(0, 0, 0, 1, thickness, 1) - 0.5

		for (dir <- Direction.DIRECTIONS) {
			val bySideRotation = dir match {
				case Direction.DOWN => Rotation.IDENTITY
				case Direction.UP => new Rotation(Vector3D.PLUS_I, Math.PI)
				case Direction.NORTH => new Rotation(Vector3D.PLUS_I, Math.PI / 2)
				case Direction.SOUTH => new Rotation(Vector3D.PLUS_I, -Math.PI / 2)
				case Direction.WEST => new Rotation(Vector3DUtil.FORWARD, Math.PI / 2)
				case Direction.EAST => new Rotation(Vector3DUtil.FORWARD, -Math.PI / 2)
				case Direction.UNKNOWN => throw new IllegalStateException()
			}

			occlusionBounds(dir.ordinal) = center transform bySideRotation add 0.5

			val matrix = new MatrixStack()
			// For some reason rotation of model works differently than in case of cuboids.
			matrix rotate new Rotation(Vector3D.PLUS_J, Math.PI)

			matrix rotate bySideRotation
			matrixes(dir.ordinal) = matrix.getMatrix

		}
	}
}

class BlockGear extends BlockEDX with Storable with Syncable {

	override def getID: String = "gear"

	@Sync
	@Store(key = "side")
	var _side: Byte = 0

	@Sync(ids = Array(0, 1))
	@Store(key = "isMaser")
	var isMaser: Boolean = true

	@Sync(ids = Array(0, 1))
	@Store(key = "masterOffset")
	var masterOffset = null

	def side = Direction.fromOrdinal(_side.asInstanceOf[Int])

	private[this] val microblock = add(new Microblock(this))
		.setOnPlace(
			(evt: PlaceEvent) => {
				this._side = evt.side.opposite.ordinal.asInstanceOf[Byte]
				Optional.of(MicroblockContainer.sidePosition(this.side))
			}
		)

	@Sync(ids = Array(1))
	var speed = Math.PI / 4
	@Sync(ids = Array(1))
	var rotation = 0D
	val watch = new Watch()

	private[this] val blockRenderer = add(new DynamicRenderer())
	blockRenderer.onRender((m: Model) => {
		m.addChild(model)
		rotation += speed * watch.update() / 1000
		rotation %= Math.PI * 2

		model.matrix popMatrix()
		model.matrix pushMatrix()
		model.matrix rotate new Rotation(side.toVector, rotation)
	})

	lazy val model = {
		val tmp = new MeshModel()
		/*val optional = MechanicContent.modelGear.getModel.stream().filter((model: Model) => "SmallGear".equals(model.name)).findFirst()

		tmp.addChild(optional.orElseThrow(() => new IllegalStateException("Model is missing")))
		tmp.matrix transform BlockGear.matrixes(_side)
		tmp.matrix pushMatrix()*/
		tmp
	}

	collider.setBoundingBox(() => {
		BlockGear.occlusionBounds(_side)
	})

	collider.isCube(false)
	collider.isOpaqueCube(false)

	private[this] val itemRenderer = add(new ItemRenderer(this))

	itemRenderer.onRender = (m: Model) => {
		m.addChild(model)
	}

	this.events.on(classOf[RightClickEvent]).bind((event: RightClickEvent) => {

	})

	override def read(packet: Packet): Unit = {
		super[Syncable].read(packet)
		world markStaticRender position
	}

}

