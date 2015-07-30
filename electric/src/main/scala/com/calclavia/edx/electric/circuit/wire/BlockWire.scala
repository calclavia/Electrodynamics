package com.calclavia.edx.electric.circuit.wire

import java.lang.{Iterable => JIterable}
import java.util.{Optional, Set => JSet}

import com.calclavia.edx.core.EDX
import com.calclavia.edx.core.prefab.BlockEDX
import com.calclavia.edx.electric.ElectricContent
import com.calclavia.edx.electric.api.Electric
import com.calclavia.edx.electric.api.Electric.GraphBuiltEvent
import com.calclavia.edx.electric.grid.NodeElectricJunction
import nova.core.block.Block.{PlaceEvent, RightClickEvent}
import nova.core.component.misc.Collider
import nova.core.component.renderer.{ItemRenderer, StaticRenderer}
import nova.core.network.{Packet, Sync, Syncable}
import nova.core.render.model.{Model, VertexModel}
import nova.core.render.pipeline.{BlockRenderStream, StaticCubeTextureCoordinates}
import nova.core.retention.{Storable, Store}
import nova.core.util.Direction
import nova.core.util.math.RotationUtil
import nova.core.util.shape.Cuboid
import nova.microblock.micro.{Microblock, MicroblockContainer}
import nova.scala.wrapper.FunctionalWrapper._
import nova.scala.wrapper.VectorWrapper._
import org.apache.commons.math3.geometry.euclidean.threed.{Rotation, Vector3D}

import scala.collection.convert.wrapAll._

/**
 * This is the class for all flat wire
 *
 * A flat wire can have 4 adjacent connections.
 * Each side the wire connected two can have 4 different states.
 *
 * A wire's adjacent connections are dependent on the orientation of the wire.
 *
 * @author Calclavia
 */
object BlockWire {
	val thickness = 2 / 16d
	val width = 1 / 3d
	var occlusionBounds = Array.ofDim[Cuboid](6, 9)
	init()

	def init() {
		for (s <- 0 until 6) {
			val rot = s match {
				case 0 => Rotation.IDENTITY
				case 1 => new Rotation(Vector3D.PLUS_I, Math.PI)
				case 2 => new Rotation(RotationUtil.DEFAULT_ORDER, Math.PI, -Math.PI / 2, 0)
				case 3 => new Rotation(Vector3D.PLUS_I, -Math.PI / 2)
				case 4 => new Rotation(RotationUtil.DEFAULT_ORDER, -Math.PI / 2, -Math.PI / 2, 0)
				case 5 => new Rotation(RotationUtil.DEFAULT_ORDER, Math.PI / 2, -Math.PI / 2, 0)
			}

			val center = new Cuboid(width, 0, width, 1 - width, thickness, 1 - width)

			//Short sides
			val sides = (0 until 4)
				.map(RotationUtil.rotateSide(0, _))
				.map(Direction.fromOrdinal)
				.map(d => center + (d.toVector * width))
				.toSeq

			//Long sides
			val sideExtension = (0 until 4)
				.map(RotationUtil.rotateSide(0, _))
				.map(Direction.fromOrdinal)
				.map(
					d => {
						val dir = d.toVector
						val min = if (d.toVector.x < 0 || d.toVector.y < 0 || d.toVector.z < 0) dir * thickness else Vector3D.ZERO
						val max = if (d.toVector.x > 0 || d.toVector.y > 0 || d.toVector.z > 0) dir * thickness else Vector3D.ZERO
						(center + (dir * width)) + new Cuboid(min, max)
					}
				)
				.toSeq

			val face = Array((sides :+ center) ++ sideExtension: _*)
			occlusionBounds(s) = face
				.map(_ - 0.5)
				.map(_.transform(rot))
				.map(_ + 0.5)
		}
	}

}

// withPriority TWire withPriority TFacePart withPriority TNormalOcclusion
class BlockWire extends BlockEDX with Storable with Syncable {

	/**
	 * The side the wire is placed on.
	 */
	@Sync
	@Store
	private var side: Byte = 0

	/**
	 * A map of the connections relative to the {@link side}. Split into four 2-bits.
	 *
	 * Each 2 bit represents a state:
	 * 00 - Not connected
	 * 01 - Internal connection (connect upward to a wire within this block space)
	 * 10 - Straight connection (connect directly forward)
	 * 11 - Corner connection (wrapper around a block)
	 *
	 * Format of bitmask:
	 * 00-00-00-00
	 *
	 * 2 Nibbles of data
	 */
	@Sync(ids = Array(0, 1))
	private var connectionMask = 0x00

	/**
	 * Caches the sidem ask and electric nodes
	 */
	private var connectionCache = Map.empty[Electric, Int].withDefaultValue(0)

	/**
	 * Add components
	 */
	private val electricNode = add(new NodeElectricJunction(this))

	private val microblock = add(new Microblock(this))
		.setOnPlace(
			(evt: PlaceEvent) => {
				this.side = evt.side.opposite.ordinal.toByte
				//TODO: Fix wire material
				get(classOf[MaterialWire]).material = WireMaterial.COPPER
				Optional.of(MicroblockContainer.sidePosition(Direction.fromOrdinal(this.side)))
			}
		)
	@Sync
	@Store
	private val material = add(new MaterialWire)

	private val blockRenderer = add(new StaticRenderer(this))

	blockRenderer.onRender(
		(model: Model) => {
			val subModel = new VertexModel()
			get(classOf[Collider]).occlusionBoxes.apply(Optional.empty()).foreach(cuboid => {
				BlockRenderStream.drawCube(subModel, cuboid - 0.5, StaticCubeTextureCoordinates.instance)
			})

			subModel.faces.foreach(_.vertices.map(_.color = get(classOf[MaterialWire]).material.color))
			subModel.bindAll(ElectricContent.wireTexture)
			model.addChild(subModel)
		}
	)
	private val itemRenderer = add(new ItemRenderer(this))

	itemRenderer.setTexture(ElectricContent.wireTexture)

	itemRenderer.onRender(
		(model: Model) => {
			val subModel = new VertexModel()
			(0 until 5)
				.map(dir => BlockWire.occlusionBounds(side)(dir))
				.foreach(cuboid => {
				BlockRenderStream.drawCube(subModel, cuboid - 0.5, StaticCubeTextureCoordinates.instance)
			})

			//TODO: Change color
			subModel.faces.foreach(_.vertices.map(_.color = get(classOf[MaterialWire]).material.color))
			subModel.bindAll(ElectricContent.wireTexture)
			model.addChild(subModel)
		}
	)

	electricNode.setConnections(() => computeConnection)
	electricNode.onGridBuilt.add((evt: GraphBuiltEvent) => {
		//The new 8-bit connection mask
		val newConnectionMask = evt.connections
			.map(connectionCache)
			.foldLeft(0)(_ | _)

		//Apply connection masks
		if (newConnectionMask != connectionMask) {
			connectionMask = newConnectionMask
			//Update client render
			EDX.network.sync(1, microblock)
		}
	})

	events.add((evt: RightClickEvent) => if (EDX.network.isServer) System.out.println(electricNode), classOf[RightClickEvent])

	collider.setBoundingBox(() => {
		BlockWire.occlusionBounds(side)(4)
	})
		.setOcclusionBoxes(func(entity => {
		var cuboids = Set.empty[Cuboid]
		cuboids += BlockWire.occlusionBounds(side)(4)
		cuboids ++= (0 until 4)
			.collect {
			case dir if (connectionMask & (1 << (dir * 2))) != 0 && (connectionMask & (1 << (dir * 2 + 1))) != 0 && (side % 2 == 0 && dir % 2 != 0) =>
				BlockWire.occlusionBounds(side)(dir + 5) //long connection TODO: Fix overlap rendering
			case dir if (connectionMask & (1 << (dir * 2))) != 0 || (connectionMask & (1 << (dir * 2 + 1))) != 0 =>
				BlockWire.occlusionBounds(side)(dir)
		}
		cuboids
	}))

	collider.isCube(false)
	collider.isOpaqueCube(false)

	override def read(packet: Packet) {
		super[Syncable].read(packet)
		world.markStaticRender(position)
	}

	/**
	 * Return the connections the block currently is connected to
	 */
	def computeConnection: Set[Electric] = {
		connectionCache = Map.empty.withDefaultValue(0)
		var connections = Set.empty[Electric]

		for (relativeSide <- 0 until 4) {
			val absSide = RotationUtil.rotateSide(side, relativeSide)

			if (maskOpen(absSide)) {
				if (!computeInnerConnection(relativeSide, absSide)) {
					if (!computeStraightConnection(relativeSide, absSide)) {
						computeCornerConnection(relativeSide, absSide)
					}
				}
			}
		}

		/**
		 * Check inner connection (01)
		 * @return True if a connection is found
		 */
		def computeInnerConnection(relativeSide: Int, absSide: Int): Boolean = {
			val opMicroblock = get(classOf[Microblock]).containers.head.get(Direction.fromOrdinal(absSide))
			if (opMicroblock.isPresent) {
				val otherMicroblock = opMicroblock.get()
				val opElectric = otherMicroblock.block.getOp(classOf[Electric])

				if (opElectric.isPresent) {
					val electric = opElectric.get
					connections += electric
					connectionCache += (electric -> (0x1 << (relativeSide * 2)))
					return true
				}
			}
			return false
		}

		/**
		 * Check straight connection (11)
			@return True if a connection is found
		 */
		def computeStraightConnection(relativeSide: Int, absSide: Int): Boolean = {
			//The position to check for another wire or a device.
			val checkPos = position + Direction.fromOrdinal(absSide).toVector
			val checkBlock = world.getBlock(checkPos)

			if (checkBlock.isPresent) {
				//First check for microblocks for another wire
				val opMicroblockHolder = checkBlock.get.getOp(classOf[MicroblockContainer])
				if (opMicroblockHolder.isPresent) {
					//Try to find the microblock that is has the component NodeElectric
					val opMicroblock = opMicroblockHolder.get().get(Direction.fromOrdinal(this.side))
					if (opMicroblock.isPresent) {
						val opElectric = opMicroblock.get.block.getOp(classOf[Electric])

						if (opElectric.isPresent) {
							val electric = opElectric.get
							connections += electric
							connectionCache += (electric -> (0x2 << (relativeSide * 2)))
							return true
						}
					}
				}

				//A microblock is not present. Try checking if the block is electric
				val opElectric = checkBlock.get.getOp(classOf[Electric])

				if (opElectric.isPresent) {
					val electric = opElectric.get
					connections += electric
					connectionCache += (electric -> (0x2 << (relativeSide * 2)))
					return true
				}
			}
			return false
		}

		/**
		 * Check corner connection (11)
		 * @return True if a connection is found
		 */
		def computeCornerConnection(relativeSide: Int, absSide: Int): Boolean = {
			/**
			 * The position to check for another wire or a device.
			 * Our position check has to move one block towards the side, then one block towards the side the wire is attached to.
			 */
			val checkPos = position + Direction.fromOrdinal(absSide).toVector + Direction.fromOrdinal(side).toVector
			val checkBlock = world.getBlock(checkPos)

			if (checkBlock.isPresent) {
				val opMicroblockHolder = checkBlock.get.getOp(classOf[MicroblockContainer])
				if (opMicroblockHolder.isPresent) {
					//Try to find the microblock that is has the component NodeElectric
					//We look for opposite of the side we are checking, as the block has to be flat placed onto the same block this wire is flat-placed on.
					val opMicroblock = opMicroblockHolder.get().get(Direction.fromOrdinal(absSide).opposite())
					if (opMicroblock.isPresent) {
						val opElectric = opMicroblock.get.block.getOp(classOf[Electric])

						if (opElectric.isPresent) {
							val electric = opElectric.get
							connections += electric
							connectionCache += (electric -> (0x3 << (relativeSide * 2)))
							return true
						}
					}
				}
			}
			return false
		}

		/**
		 * Check if there's a cover on a specific side
		 */
		def maskOpen(absSide: Int): Boolean = {
			//TODO: Check bounding space (cuboid)
			//TODO:Multiple containers?
			//return !get(classOf[Microblock]).containers.head.get(Direction.fromOrdinal(absSide)).isPresent
			return true
		}

		return connections
	}

	override def getID: String = "electricWire"
}