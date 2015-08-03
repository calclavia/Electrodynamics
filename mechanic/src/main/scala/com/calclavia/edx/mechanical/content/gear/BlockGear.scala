package com.calclavia.edx.mechanical.content.gear

import java.util.Optional

import com.calclavia.edx.core.prefab.BlockEDX
import com.calclavia.edx.mechanical.physic.grid.MechanicalNodeGear


import com.calclavia.edx.mechanical.physic.{ MechanicalMaterial}
import com.calclavia.edx.mechanical.{Watch, MechanicContent}
import nova.core.block.Block
import nova.core.block.Block.{RightClickEvent, PlaceEvent}
import nova.core.component.renderer.{DynamicRenderer, StaticRenderer, ItemRenderer}
import nova.core.network.{Packet, Syncable, Sync}
import nova.core.render.model.{MeshModel, Model}
import nova.core.render.pipeline.{BlockRenderStream, StaticCubeTextureCoordinates}
import nova.core.retention.{Store, Storable}

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

	@Sync
	@Store(key = "size")
	var size = 1

	def side = Direction.fromOrdinal(_side.asInstanceOf[Int])

	val microblock = add(new Microblock(this))
		.setOnPlace(
			(evt: PlaceEvent) => {
				this._side = evt.side.opposite.ordinal.asInstanceOf[Byte]
				Optional.of(MicroblockContainer.sidePosition(this.side))
			}
		)


	add(MechanicalMaterial.metal)

	private[this] val rotational = add(new MechanicalNodeGear(this))
	
	private[this] val blockRenderer = add(new DynamicRenderer())
	blockRenderer.onRender((m: Model) => {
		m.addChild(model)

		model.matrix popMatrix()
		model.matrix pushMatrix()
		println(rotational.grid)
		println(rotational.rotation)
		model.matrix rotate new Rotation(side.toVector, rotational.rotation)
	})

	lazy val model = {
		val tmp = new MeshModel()
		//val optional = MechanicContent.modelGear.getModel.stream().filter((model: Model) => "SmallGear".equals(model.name)).findFirst()

		//tmp.addChild(optional.orElseThrow(() => new IllegalStateException("Model is missing")))
		BlockRenderStream.drawCube(tmp, BlockGear.occlusionBounds(_side) - 0.5 , StaticCubeTextureCoordinates.instance)
		tmp.bind(MechanicContent.gearTexture)
		//tmp.matrix transform BlockGear.matrixes(_side)
		tmp.matrix pushMatrix()
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

	this.events.on(classOf[Block.RightClickEvent]).bind((event: RightClickEvent) => {

	})

	override def read(packet: Packet): Unit = {
		super[Syncable].read(packet)
		world markStaticRender position
	}

}

