package com.calclavia.edx.mechanical.content

import java.util.Optional

import com.calclavia.edx.core.prefab.BlockEDX
import com.calclavia.edx.mechanical.MechanicContent
import com.calclavia.edx.mechanical.physic.MechanicalMaterial
import com.calclavia.edx.mechanical.physic.grid.MechanicalNodeGear
import nova.core.block.Block
import nova.core.block.Block.{PlaceEvent, RightClickEvent}
import nova.core.component.renderer.DynamicRenderer
import nova.core.network.{Packet, Sync, Syncable}
import nova.core.render.Color
import nova.core.render.model.{MeshModel, Model}
import nova.core.render.pipeline.{BlockRenderPipeline, StaticCubeTextureCoordinates}
import nova.core.retention.{Storable, Store}
import nova.core.util.Direction
import nova.core.util.math.{MatrixStack, Vector3DUtil}
import nova.core.util.shape.Cuboid
import nova.microblock.micro.{Microblock, MicroblockContainer}
import nova.scala.wrapper.FunctionalWrapper._
import nova.scala.wrapper.OptionWrapper._
import nova.scala.wrapper.VectorWrapper._
import org.apache.commons.math3.geometry.euclidean.threed.{Rotation, Vector3D}
import org.apache.commons.math3.linear.RealMatrix

import scala.collection.JavaConversions._

object BlockGear {
	val thickness = 2 / 16d
	val occlusionBounds = Array.ofDim[Cuboid](6)
	val matrixes = Array.ofDim[RealMatrix](6)

	{
		val oneByRoot2 = 1 / Math.sqrt(2)
		val center = new Cuboid(0, 0, 0, 1, thickness, 1) - 0.5

		for (dir <- Direction.VALID_DIRECTIONS) {
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

	val bigGearMap: Map[Direction, Seq[Vector3D]] = {
		val (templateVector, templateList) = {
			var list = List.empty[Vector3D]
			for (x <- -1 to 1; z <- -1 to 1 if !(x == 0 && z == 0)) {
				list = new Vector3D(x, 0, z) :: list
			}
			(new Vector3D(0, -1, 0), list)
		}
		var res: Map[Direction, Seq[Vector3D]] = Map.empty

		for (i <- Direction.VALID_DIRECTIONS) {
			val rot = new Rotation(templateVector, i.toVector)
			res += i -> templateList.map(rot.applyTo)
		}
		res
	}

	def at(block: Option[Block], side: Direction): Option[BlockGear] = {
		block match {
			case Some(gear: BlockGear) if gear.side == side => Some(gear)
			case Some(block: Block) if block.components.has(classOf[MicroblockContainer]) =>
				block.components.getOp(classOf[MicroblockContainer]).toOption.flatMap(_.get(side)).map(_.block).collect(gearCollector(side))
			case _ => None
		}
	}

	def gearCollector(side: Direction = Direction.UNKNOWN): PartialFunction[Block, BlockGear] = {
		case gear: BlockGear if side == Direction.UNKNOWN || gear.side == side => gear
	}

	class Stone extends BlockGear {
		override def material = MechanicalMaterial.stone
	}

	class Wood extends BlockGear {
		override def material = MechanicalMaterial.wood
	}

	class Metal extends BlockGear {
		override def material = MechanicalMaterial.metal
	}

}

object Test extends App {
	println(BlockGear.bigGearMap)
}

abstract class BlockGear extends BlockEDX with Storable with Syncable {

	def material: MechanicalMaterial

	components.add(material)

	@Sync
	@Store(key = "side")
	var _side: Byte = 0

	@Sync
	@Store(key = "size")
	var size = 1

	@Sync
	@Store(key = "isMaster")
	var isMaster = true

	@Sync
	@Store(key = "masterOffset")
	var masterOffset = Vector3D.ZERO

	def master = BlockGear.at(world.getBlock(position + masterOffset), side)

	def side = Direction.fromOrdinal(_side.asInstanceOf[Int])

	val microblock = components.add(new Microblock(this))
		.setOnPlace(
	    (evt: PlaceEvent) => {
		    this._side = evt.side.opposite.ordinal.asInstanceOf[Byte]
		    Optional.of(MicroblockContainer.sidePosition(this.side))
	    }
		)

	private[this] val rotational = components.add(new MechanicalNodeGear(this))

	private[this] val blockRenderer = components.add(new DynamicRenderer())
	blockRenderer.onRender((m: Model) => {
		if (this.isMaster) {
			m.addChild(model)

			model.matrix popMatrix()
			model.matrix pushMatrix()
			model.matrix scale(size, size, size)

			model.matrix rotate new Rotation(side.toVector, rotational.rotation)
		}
	})

	var _model: Option[Model] = None

	def model: Model = {
		_model match {
			case None => _model = Some(createModel); _model.get
			case Some(model) => model
		}
	}

	def createModel = {
		val tmp = new MeshModel()
		//val optional = MechanicContent.modelGear.getModel.stream().filter((model: Model) => "SmallGear".equals(model.name)).findFirst()

		//tmp.addChild(optional.orElseThrow(() => new IllegalStateException("Model is missing")))
		var box = collider.boundingBox() //.multiply(Math.sqrt(size))
		box = box - box.center()


		BlockRenderPipeline.drawCube(tmp, box, StaticCubeTextureCoordinates.instance)
		// TODO: Remove that after textures are made.
		tmp.faces.foreach(face => face.vertices.foreach(v => v.color = Color.rgb(material.hashCode())))
		tmp.bind(MechanicContent.gearTexture)
		//tmp.matrix transform BlockGear.matrixes(_side)
		tmp.matrix translate ((side.toVector / 2d) - side.toVector * 2 / 16d)
		tmp.matrix pushMatrix()
		tmp
	}

	collider.setBoundingBox(() => {
		BlockGear.occlusionBounds(_side)
	})

	collider.isCube(false)
	collider.isOpaqueCube(false)

	def checkBigGear(validate: Boolean = true): Unit = {
		val gears = possibleSubGears()

		val res = gears.map(_.filter(gear => gear.material == this.material && (gear.isMaster || gear.master.contains(this)))).forall(_.isDefined)
		res match {
			case true if validate && isMaster => this.validateBigGear()
			case false if isMaster => this.invalidateBigGear()
			case _ =>
		}
	}

	private[this] def possibleSubGears() = {
		for (offset <- BlockGear.bigGearMap(side)) yield {
			val pos = this.position + offset
			val blockOp = this.world.getBlock(pos).toOption
			val part = blockOp.flatMap(_.components.getOp(classOf[MicroblockContainer])).flatMap(_.get(side))
			part.map(_.block).collect(BlockGear.gearCollector(side))
		}
	}

	private[this] def validateBigGear(): Unit = {
		this.isMaster = true
		this.masterOffset = Vector3D.ZERO
		this.size = 3
		this._model = None
		possibleSubGears().flatten.collect(BlockGear.gearCollector()).foreach {
			gear =>
				gear.masterOffset = this.position - gear.position
				gear.isMaster = false
		}
	}

	private[this] def invalidateBigGear(): Unit = {
		println("Invalidate")
		this.isMaster = true
		this.masterOffset = Vector3D.ZERO
		this.size = 1
		this._model = None
		possibleSubGears().flatten.collect(BlockGear.gearCollector()).filter(_.master.contains(this))
			.foreach {
			gear =>
				gear.masterOffset = Vector3D.ZERO
				gear.isMaster = true
		}
	}

	this.events.on(classOf[Block.RightClickEvent]).bind((event: RightClickEvent) => {
		checkBigGear()
	})

	this.events.on(classOf[Block.NeighborChangeEvent]).bind((event: Block.NeighborChangeEvent) => {
		if (!this.isMaster) {
			world.getBlock(this.position + this.masterOffset).collect { case b: BlockGear => b } foreach (_.checkBigGear(validate = false))
		}
	})

	override def read(packet: Packet): Unit = {
		super[Syncable].read(packet)
		world markStaticRender position
	}

}

