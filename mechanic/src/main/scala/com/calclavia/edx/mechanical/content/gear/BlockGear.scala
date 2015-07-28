package com.calclavia.edx.mechanical.content.gear

import java.util.Optional

import com.calclavia.edx.core.prefab.BlockEDX
import com.calclavia.edx.mechanical.{Watch, MechanicContent}
import nova.core.block.Block.{RightClickEvent, PlaceEvent}
import nova.core.component.renderer.{DynamicRenderer, StaticRenderer, ItemRenderer}
import nova.core.network.{Packet, Syncable, Sync}
import nova.core.render.model.{StaticCubeTextureCoordinates, BlockModelUtil, Model}
import nova.core.retention.{Store, Storable}
import nova.core.util.Direction
import nova.core.util.math.{Vector3DUtil, RotationUtil}
import nova.core.util.shape.Cuboid
import nova.microblock.micro.{MicroblockContainer, Microblock}
import nova.scala.wrapper.FunctionalWrapper._
import org.apache.commons.math3.geometry.euclidean.threed.{Vector3D, Rotation}


object BlockGear {
	val thickness = 2 / 16d
	val occlusionBounds = Array.ofDim[Cuboid](6)
	val models = Array.ofDim[Model](6)

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
				case Direction.EAST => new Rotation(Vector3DUtil.FORWARD, - Math.PI / 2)
				case Direction.UNKNOWN => throw new IllegalStateException()
			}

			occlusionBounds(dir.ordinal) = center transform bySideRotation add 0.5

			val model = new Model(s"Side $dir")
			// For some reason rotation of model works differently than in case of cuboids.
			model.matrix rotate new Rotation(Vector3D.PLUS_J, Math.PI)
			
			model.matrix rotate bySideRotation
			model.matrix rotate new Rotation(Vector3D.PLUS_J, Math.PI / 4)
			model.matrix scale(oneByRoot2, 1, oneByRoot2)

			BlockModelUtil.drawCube(model, center, StaticCubeTextureCoordinates.instance)
			model bind MechanicContent.gearTexture


			models(dir.ordinal) = model

		}
	}
}

class BlockGear extends BlockEDX with Storable with Syncable{

	override def getID: String = "gear"

	@Sync
	@Store(key = "side")
	var _side: Byte = 0

	@Sync(ids = Array(0, 1))
	@Store(key = "isMaser")
	var isMaser: Boolean = true


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
	blockRenderer.setOnRender((m: Model) => {
		m.addChild(model)
		rotation += speed * watch.update() / 1000
		rotation %= Math.PI * 2

		model.matrix popMatrix()
		model.matrix pushMatrix()
		model.matrix rotate new Rotation(side.toVector, rotation)
	})
	lazy val model = {
		val tmp = BlockGear.models(_side).clone()
		tmp.matrix pushMatrix()
		tmp
	}


	collider.setBoundingBox(() => {
		BlockGear.occlusionBounds(_side)
	})

	collider.isCube(false)
	collider.isOpaqueCube(false)

	private[this] val itemRenderer = add(new ItemRenderer(this))

	itemRenderer.onRender = (model: Model) => {
		model.addChild(BlockGear.models(3))
	}

	this.events.on(classOf[RightClickEvent]).bind((event: RightClickEvent) => {

	})

	override def read(packet: Packet): Unit = {
		super[Syncable].read(packet)
		world markStaticRender position
	}

}

