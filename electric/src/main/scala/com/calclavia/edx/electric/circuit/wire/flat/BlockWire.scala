package com.calclavia.edx.electric.circuit.wire.flat

import java.lang.{Iterable => JIterable}
import java.util.{Optional, Set => JSet}

import com.calclavia.edx.core.component.Material
import com.calclavia.edx.electrical.circuit.wire.base.WireMaterial
import com.calclavia.graph.api.energy.NodeElectric
import com.calclavia.graph.core.electric.NodeElectricJunction
import com.calclavia.microblock.api.Microblock
import com.resonant.lib.util.RotationUtility
import com.resonant.lib.wrapper.WrapFunctions._
import nova.core.block.Block
import nova.core.block.Block.BlockPlaceEvent
import nova.core.block.component.{BlockCollider, StaticBlockRenderer}
import nova.core.entity.Entity
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
class BlockWire extends Block with Storable with PacketHandler {

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

	add(new Microblock(this))
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

	placeEvent.add((evt: BlockPlaceEvent) => {
		evt.side.ifPresent((side: Direction) => this.side = side.opposite.ordinal.toByte)
		//TODO: Get material from item
		//evt.item.ifPresent((item:Item) => get(classOf[Material[WireMaterial]]).material = WireMaterial.values()(item))
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

		for (s <- 0 until 4) {
			val absDir = RotationUtility.rotateSide(side, s)
			//Check inner connection
			if (get(classOf[Microblock]).)


			//Check straight connection

			//Check corner connection
		}

		return connections
	}

	/**
	 * Check if there's a cover on a specific side
	 */
	def maskOpen(absDir: Int): Boolean = {
		//TODO: Implement me
		return false
	}
}