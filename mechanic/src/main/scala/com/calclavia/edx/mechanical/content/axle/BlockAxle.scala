package com.calclavia.edx.mechanical.content.axle

import java.util.Optional

import com.calclavia.edx.core.prefab.BlockEDX
import com.calclavia.edx.mechanical.{Watch, MechanicContent}
import nova.core.block.Block.PlaceEvent
import nova.core.block.component.StaticBlockRenderer
import nova.core.component.renderer.{DynamicRenderer, ItemRenderer, StaticRenderer}
import nova.core.network.{Sync, Packet, Syncable}
import nova.core.render.pipeline.StaticCubeTextureCoordinates
import nova.core.render.model.{BlockModelUtil, Model}
import nova.core.retention.{Store, Storable}
import nova.core.util.Direction
import nova.core.util.math.Vector3DUtil
import nova.core.util.shape.Cuboid
import nova.internal.core.tick.UpdateTicker
import nova.microblock.micro.{MicroblockContainer, Microblock}
import nova.microblock.multi.Multiblock
import org.apache.commons.math3.geometry.euclidean.threed.{Vector3D, Rotation}
import nova.scala.wrapper.FunctionalWrapper._
import nova.scala.wrapper.VectorWrapper._

object BlockAxle {
	val thickness = 2 / 16d
	def occlusionBounds = {
		val center = {
			val small = 0.0001d
			val tmp = new Cuboid(0, 0, 0 + small, thickness, thickness, 1 - small)
			tmp - tmp.center
		}

		val array = for (dir <- Direction.DIRECTIONS if dir.ordinal() % 2 == 0) yield {
			val rotation = dir match {
				case Direction.DOWN => new Rotation(Vector3D.MINUS_I, Math.PI / 2)
				case Direction.NORTH => Rotation.IDENTITY
				case Direction.WEST => new Rotation(Vector3D.MINUS_J, Math.PI / 2)

				case _ => throw new IllegalStateException()// should not happen
			}
			(dir, center transform rotation add 0.5)
		}

		array.toMap
	}

	def normalizeDir(dir: Direction) = Direction.fromOrdinal(dir.ordinal() & 0xFE)

}

class BlockAxle extends BlockEDX with Storable with Syncable {
	override def getID: String = "axle"

	@Sync
	@Store
	var _dir: Byte = 2  // For item renderer

	def dir = Direction.fromOrdinal(_dir.asInstanceOf[Int])
	def dir_=(direction: Direction): Unit = {
		_dir = BlockAxle.normalizeDir(direction).ordinal().asInstanceOf[Byte]
	}

	private[this] val microblock = add(new Microblock(this))
		.setOnPlace(
			(evt: PlaceEvent) => {
				this.dir = evt.side
				Optional.of(MicroblockContainer.centerPosition)
			}
		)

	private[this] val blockRenderer = add(new DynamicRenderer())

	@Sync(ids = Array(1))
	var speed = Math.PI / 4
	@Sync(ids = Array(1))
	var rotation = 0D
	private[this] val watch = new Watch()

	blockRenderer.setOnRender((m: Model) => {
		m.addChild(model)
		rotation += speed * watch.update() / 1000
		rotation %= Math.PI * 2

		model.matrix popMatrix()
		model.matrix pushMatrix()
		model.matrix rotate new Rotation(dir.toVector, rotation)
	})

	lazy val model = {
		val res = new Model("gearshaft")
		BlockModelUtil.drawCube(res, BlockAxle.occlusionBounds(dir) - 0.5, StaticCubeTextureCoordinates.instance)
		res.bind(MechanicContent.gearshaftTexture)
		res.matrix pushMatrix()
		res
	}

	collider.setBoundingBox(() => {
		BlockAxle.occlusionBounds(dir)
	})

	collider.isCube(false)
	collider.isOpaqueCube(false)

	add(new ItemRenderer(this))

	override def read(packet: Packet): Unit = {
		super[Syncable].read(packet)
		world markStaticRender position
	}
}
