package com.calclavia.edx.mechanical.content

import java.util.Optional

import com.calclavia.edx.core.prefab.BlockEDX
import com.calclavia.edx.mechanical.MechanicContent
import com.calclavia.edx.mechanical.physic.MechanicalMaterial
import com.calclavia.edx.mechanical.physic.grid.MechanicalNodeAxle
import nova.core.block.Block.PlaceEvent
import nova.core.component.renderer.DynamicRenderer
import nova.core.network.{Packet, Sync, Syncable}
import nova.core.render.Color
import nova.core.render.model.{MeshModel, Model}
import nova.core.render.pipeline.{BlockRenderStream, StaticCubeTextureCoordinates}
import nova.core.retention.{Storable, Store}
import nova.core.util.Direction
import nova.core.util.shape.Cuboid
import nova.microblock.micro.{Microblock, MicroblockContainer}
import nova.scala.wrapper.FunctionalWrapper._
import org.apache.commons.math3.geometry.euclidean.threed.{Rotation, Vector3D}

import scala.collection.JavaConversions._

object BlockAxle {
	val thickness = 2 / 16d

	def occlusionBounds = {
		val center = {
			val tmp = new Cuboid(0, 0, 0, thickness, thickness, 1)
			tmp - tmp.center
		}

		val array = for (dir <- Direction.VALID_DIRECTIONS if dir.ordinal() % 2 == 0) yield {
			val rotation = dir match {
				case Direction.DOWN => new Rotation(Vector3D.MINUS_I, Math.PI / 2)
				case Direction.NORTH => Rotation.IDENTITY
				case Direction.WEST => new Rotation(Vector3D.MINUS_J, Math.PI / 2)

				case _ => throw new IllegalStateException() // should not happen
			}
			(dir, center transform rotation add 0.5)
		}

		array.toMap
	}

	def normalizeDir(dir: Direction) = Direction.fromOrdinal(dir.ordinal() & 0xFE)

	class Stone extends BlockAxle {
		override def material = MechanicalMaterial.stone
	}

	class Wood extends BlockAxle {
		override def material = MechanicalMaterial.wood
	}

	class Metal extends BlockAxle {
		override def material = MechanicalMaterial.metal
	}

}

abstract class BlockAxle extends BlockEDX with Storable with Syncable {

	add(material)
	dir = Direction.NORTH

	def material: MechanicalMaterial

	override def getID: String = s"axle-$material"

	@Sync
	@Store
	var _dir: Byte = 0

	def dir = Direction.fromOrdinal(_dir.asInstanceOf[Int])

	def dir_=(direction: Direction): Unit = {
		_dir = BlockAxle.normalizeDir(direction).ordinal().asInstanceOf[Byte]
	}

	val microblock = add(new Microblock(this))
		.setOnPlace(
			(evt: PlaceEvent) => {
				this.dir = evt.side
				Optional.of(MicroblockContainer.centerPosition)
			}
		)

	private[this] val blockRenderer = add(new DynamicRenderer())

	private[this] val rotational = add(new MechanicalNodeAxle(this))

	blockRenderer.onRender((m: Model) => {
		m.addChild(model)

		model.matrix popMatrix()
		model.matrix pushMatrix()
		model.matrix rotate new Rotation(dir.toVector, rotational.rotation)
	})

	lazy val model = {
		val res = new MeshModel("gearshaft")
		BlockRenderStream.drawCube(res, BlockAxle.occlusionBounds(dir) - 0.5, StaticCubeTextureCoordinates.instance)
		res.bind(MechanicContent.gearshaftTexture)
		// TODO: Remove that after textures are made.
		res.faces.foreach(face => face.vertices.foreach(v => v.color = Color.rgb(material.hashCode())))
		res.matrix pushMatrix()
		res
	}

	collider.setBoundingBox(() => {
		BlockAxle.occlusionBounds(dir)
	})

	collider.isCube(false)
	collider.isOpaqueCube(false)

	override def read(packet: Packet): Unit = {
		super[Syncable].read(packet)
		world markStaticRender position
	}
}
