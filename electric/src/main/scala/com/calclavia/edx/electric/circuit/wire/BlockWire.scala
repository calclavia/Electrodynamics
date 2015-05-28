package com.calclavia.edx.electric.circuit.wire

import java.lang.{Iterable => JIterable}
import java.util.{Optional, Set => JSet}

import com.calclavia.edx.core.CategoryEDX
import com.calclavia.edx.core.component.Material
import com.calclavia.graph.api.energy.NodeElectric
import com.calclavia.graph.core.electric.NodeElectricJunction
import com.calclavia.microblock.core.micro.{MicroblockContainer, Microblock}
import com.resonant.lib.util.RotationUtility
import com.resonant.lib.wrapper.WrapFunctions._
import nova.core.block.Block
import nova.core.block.Block.BlockPlaceEvent
import nova.core.block.component.{BlockCollider, StaticBlockRenderer}
import nova.core.entity.Entity
import nova.core.game.Game
import nova.core.network.{PacketHandler, Sync}
import nova.core.render.model.{BlockModelUtil, Model, StaticCubeTextureCoordinates}
import nova.core.retention.{Storable, Stored}
import nova.core.util.Direction
import nova.core.util.transform.shape.Cuboid

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
	var selectionBounds = Array.ofDim[Cuboid](3, 6)
	var occlusionBounds = Array.ofDim[Cuboid](3, 6)
	init()

	def init() {
		for (t <- 0 until 3) {
			val selection = new Cuboid(0, 0, 0, 1, (t + 2) / 16D, 1).expand(-0.005)
			val occlusion = new Cuboid(2 / 8D, 0, 2 / 8D, 6 / 8D, (t + 2) / 16D, 6 / 8D)

			for (s <- 0 until 6) {
				selectionBounds(t)(s) = selection.transform(Direction.fromOrdinal(s).rotation)
				occlusionBounds(t)(s) = occlusion.transform(Direction.fromOrdinal(s).rotation)
			}
		}
	}
}

// with TWire with TFacePart with TNormalOcclusion
class BlockWire extends Block with Storable with PacketHandler with CategoryEDX {

	private val electricNode = new NodeElectricJunction(this)

	/**
	 * The side the wire is placed on.
	 */
	@Sync
	@Stored
	var side: Byte = 0

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
	 */
	@Sync(ids = Array(0, 1))
	var connectionMask = 0x00000000

	/**
	 * Add components
	 */
	add(electricNode)
		.setConnectionHandler(computeConnection)

	add(new Microblock(this,
		(evt: BlockPlaceEvent) => {
			this.side = evt.side.opposite.ordinal.toByte
			//get(classOf[Material[WireMaterial]]).material = WireMaterial.values()(evt.item)
			MicroblockContainer.sidePosition(Direction.fromOrdinal(this.side))
		}))
		.setSlotMask(supplier(() => 1 << side))

	add(new BlockCollider(this))
		.collidingBoxes(biFunc((cuboid: Cuboid, entity: Optional[Entity]) => Set[Cuboid](BlockWire.occlusionBounds(1)(side))))

	add(new Material[WireMaterial])

	add(new StaticBlockRenderer(this))
		.onRender((model: Model) => {
		//TODO Bind material texture.
		get(classOf[BlockCollider]).collisionBoxes.foreach(cuboid => BlockModelUtil.drawCube(model, cuboid, StaticCubeTextureCoordinates.instance))
		//model.bindAll()
	})

	/*
	override def getSubParts: JIterable[IndexedCuboid6] = Seq(new IndexedCuboid6(0, BlockWire.selectionBounds(getThickness)(side)))
	def getOcclusionBoxes: JIterable[Cuboid6] =
	override def solid(arg0: Int) = false
	*/

	/**
	 * Return the connections the block currently is connected to
	 */
	def computeConnection(): JSet[NodeElectric] = {
		//The new 8-bit connection mask
		var newConnectionMask = 0x00000000
		var connections = Set.empty[NodeElectric]

		for (relativeSide <- 0 until 4) {
			val absSide = RotationUtility.rotateSide(relativeSide, relativeSide)

			if (maskOpen(absSide)) {
				if (!computeInnerConnection(relativeSide, absSide)) {
					if (!computeStraightConnection(relativeSide, absSide)) {
						computeStraightConnection(relativeSide, absSide)
					}
				}
			}
		}

		//Apply connection masks
		if (newConnectionMask != connectionMask) {
			connectionMask = newConnectionMask
			//Update client render
			Game.instance.networkManager.sync(1, this)
		}

		/**
		 * Check inner connection (01)
		 * @return True if a connection is found
		 */
		def computeInnerConnection(relativeSide: Int, absSide: Int): Boolean = {
			val opMicroblock = get(classOf[MicroblockContainer]).get(absSide)
			if (opMicroblock.isPresent) {
				val otherMicroblock = opMicroblock.get()
				val opElectric = otherMicroblock.block.getOp(classOf[NodeElectric])

				if (opElectric.isPresent) {
					connections += opElectric.get
					newConnectionMask |= 0x01 << (relativeSide * 2)
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
					val opMicroblock = opMicroblockHolder.get().get(this.side)
					if (opMicroblock.isPresent) {
						val opElectric = opMicroblock.get.block.getOp(classOf[NodeElectric])

						if (opElectric.isPresent) {
							connections += opElectric.get
							newConnectionMask |= 0x10 << (relativeSide * 2)
							return true
						}
					}
				}

				//A microblock is not present. Try checking if the block is electric
				val opElectric = checkBlock.get.getOp(classOf[NodeElectric])

				if (opElectric.isPresent) {
					connections += opElectric.get
					newConnectionMask |= 0x10 << (relativeSide * 2)
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
					val opMicroblock = opMicroblockHolder.get().get(absSide ^ -1)
					if (opMicroblock.isPresent) {
						val opElectric = opMicroblock.get.block.getOp(classOf[NodeElectric])

						if (opElectric.isPresent) {
							connections += opElectric.get
							newConnectionMask |= 0x11 << (relativeSide * 2)
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
			//Check bounding space (cuboid)
			return get(classOf[MicroblockContainer]).get(absSide).isPresent
		}

		return connections
	}

	override def getID: String = "electricWire"
}