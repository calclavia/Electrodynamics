package com.calclavia.edx.electrical.circuit.wire.flat

import java.lang.{Iterable => JIterable}

import com.calclavia.edx.electrical.circuit.wire.base.TWire
import com.calclavia.graph.core.electric.{NodeElectricComponent, NodeElectricJunction}
import com.calclavia.microblock.api.Microblock
import nova.core.block.Block
import nova.core.block.component.BlockCollider
import nova.core.network.{PacketHandler, Sync}
import nova.core.retention.{Storable, Stored}
import nova.core.util.Direction
import nova.core.util.transform.shape.Cuboid
import nova.core.util.transform.vector.Vector3

import scala.collection.convert.wrapAll._

/**
 * This is the class for all flat wire/
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
	add(new Microblock(this))

	add(new BlockCollider(this))
		.collidingBoxes()

	def preparePlacement(side: Int, meta: Int) {
		this.side = (side ^ 1).toByte
		setMaterial(meta)
	}

	override def setMaterial(i: Int) {
		super.setMaterial(i)
		electricNode.resistance = material.resistance
	}

	override def update() {
		super.update()
	}

	override def activate(player: EntityPlayer, hit: MovingObjectPosition, item: ItemStack): Boolean = {
		if (!world.isRemote) {
			println(electricNode)
		}

		return true
	}

	def renderThisCorner(part: BlockWire): Boolean = {
		if (!(part.isInstanceOf[BlockWire])) {
			return false
		}
		val wire: BlockWire = part
		if (wire.getThickness == getThickness) {
			return side < wire.side
		}
		return wire.getThickness > getThickness
	}

	def getThickness: Int = if (insulated) 1 else 0

	/**
	 * Events
	 */
	override def onRemoved() {
		super.onRemoved()

		if (!world.isRemote) {
			for (r <- 0 until 4) {
				if (maskConnects(r)) {
					if ((connectionMask & 1 << r) != 0)
						notifyCornerChange(r)
					else if ((connectionMask & 0x10 << r) != 0)
						notifyStraightChange(r)
				}
			}
		}
	}

	def notifyCornerChange(r: Int) {
		val absDir = Rotation.rotateSide(side, r)
		val pos = new BlockCoord(tile).offset(absDir).offset(side)
		world.notifyBlockOfNeighborChange(pos.x, pos.y, pos.z, tile.getBlockType)
	}

	def notifyStraightChange(r: Int) {
		val absDir = Rotation.rotateSide(side, r)
		val pos = new BlockCoord(tile).offset(absDir)
		world.notifyBlockOfNeighborChange(pos.x, pos.y, pos.z, tile.getBlockType)
	}

	def maskConnects(r: Int): Boolean = {
		return (connectionMask & 0x111 << r) != 0
	}

	override def onChunkLoad() {
		if ((connectionMask & 0x80000000) != 0) {
			if (dropIfCantStay)
				return

			connectionMask = 0
			tile.markDirty()
		}

		super.onChunkLoad()
	}

	def dropIfCantStay: Boolean = {
		if (!canStay) {
			drop
			return true
		}
		return false
	}

	def canStay: Boolean = {
		val pos: BlockCoord = new BlockCoord(tile).offset(side)
		return MultipartUtil.canPlaceWireOnSide(world, pos.x, pos.y, pos.z, Direction.getOrientation(side ^ 1), false)
	}

	def drop {
		TileMultipart.dropItem(getItem, world, Vector3.fromTileEntityCenter(tile))
		tile.remPart(this)
	}

	override def onAdded() {
		super.onAdded()

		if (!world.isRemote)
			sendPacket(3)
	}

	override def onPartChanged(part: TMultiPart) {
		super.onPartChanged(part)

		if (!world.isRemote)
			sendPacket(3)
	}

	override def onNeighborChanged() {
		if (!world.isRemote)
			if (dropIfCantStay)
				return

		super.onNeighborChanged()

		if (!world.isRemote)
			sendPacket(3)
	}

	def maskOpen(r: Int): Boolean = {
		return (connectionMask & 0x1000 << r) != 0
	}

	/**
	 * Multipart Methods
	 */
	override def getStrength(hit: MovingObjectPosition, player: EntityPlayer): Float = 4

	def getSlotMask: Int = 1 << side

	override def getSubParts: JIterable[IndexedCuboid6] = Seq(new IndexedCuboid6(0, BlockWire.selectionBounds(getThickness)(side)))

	def getOcclusionBoxes: JIterable[Cuboid6] = Seq(BlockWire.occlusionBounds(getThickness)(side))

	override def solid(arg0: Int) = false

	/**
	 * Rendering
	 */
	@SideOnly(Side.CLIENT)
	def getIcon: IIcon = RenderFlatWire.wireIcon

	@SideOnly(Side.CLIENT)
	override def renderStatic(pos: Vector3, pass: Int): Boolean = {
		if (pass == 0 && useStaticRenderer) {
			CCRenderState.setBrightness(world, x, y, z)
			RenderFlatWire.render(this, pos)
			CCRenderState.setColour(-1)
			return true
		}
		return false
	}

	@SideOnly(Side.CLIENT)
	override def renderDynamic(pos: Vector3, frame: Float, pass: Int) {
		if (pass == 0 && !useStaticRenderer) {
			GL11.glDisable(GL11.GL_LIGHTING)
			TextureUtils.bindAtlas(0)
			CCRenderState.startDrawing(7)
			RenderFlatWire.render(this, pos)
			CCRenderState.draw()
			CCRenderState.setColour(-1)
			GL11.glEnable(GL11.GL_LIGHTING)
		}
	}

	def useStaticRenderer: Boolean = true

	@SideOnly(Side.CLIENT)
	override def drawBreaking(renderBlocks: RenderBlocks) {
		CCRenderState.reset()
		RenderFlatWire.renderBreakingOverlay(renderBlocks.overrideBlockTexture, this)
	}

	/**
	 * Flat wire node handles all the connection logic
	 * TODO: Direction may NOT be suitable. Integers are better.
	 * @param provider
	 */
	class NodeFlatWire(provider: INodeProvider) extends NodeElectricJunction(provider) with TMultipartNode[NodeElectricComponent] {
		override def reconstruct() {
			if (!world.isRemote) {
				directionMap.clear()
				updateOpenConnections()

				/**
				 * 6 bit bitmask to mark sides that are already calculated to determine which side to disconnect.
				 * TODO: Check if the bitshifting is correct
				 * E.g: 000010
				 */
				var calculatedMask = 0x00

				/**
				 * External connections
				 */
				for (r <- 0 until 4) {
					if (maskOpen(r)) {
						val absDir = Rotation.rotateSide(side, r)

						if (setExternalConnection(r, absDir) || setCornerConnection(r, absDir))
							calculatedMask |= 1 << absDir

						if ((calculatedMask & (1 << absDir)) == 0)
							disconnect(absDir)
					}
				}

				/**
				 * Internal connections
				 */
				for (r <- 0 until 4) {
					var skip = false

					val absDir = Rotation.rotateSide(side, r)

					if (tile.partMap(PartMap.edgeBetween(absDir, side)) == null) {
						val part = tile.partMap(absDir)
						val to = Direction.getOrientation(absDir)
						val from = to.getOpposite

						if (part != null) {
							val node = part.asInstanceOf[INodeProvider].getNode(classOf[NodeElectricComponent], from)

							if (canConnect(node, to)) {
								//TODO: Check dir
								connect(node, to)
								skip = true
							}
						}
					}

					if ((calculatedMask & (1 << absDir)) == 0 && !skip) {
						disconnect(absDir)
					}
				}

				//External connection
				setExternalConnection(-1, side)

				updateExternalConnections()
				updateInternalConnections()

				//Reconstruct the grid
				grid.reconstruct(this)
			}
		}

		def setExternalConnection(r: Int, absDir: Int): Boolean = {
			val pos = new BlockCoord(tile).offset(absDir)
			val tilePart = MultipartUtil.getMultipartTile(world, pos)

			val toDir = Direction.getOrientation(absDir)
			val fromDir = Direction.getOrientation(absDir).getOpposite

			if (tilePart != null && r != -1) {
				val part = tilePart.partMap(side)
				val dcNode = getComponent(part, fromDir)

				//Check if it's another flat wire.
				if (canConnect(dcNode, toDir)) {
					val otherR = (r + 2) % 4

					if (part.isInstanceOf[BlockWire]) {
						val wire = part.asInstanceOf[BlockWire]

						//Check other wire connectability
						if (dcNode.canConnect(this, fromDir) && wire.maskOpen(otherR)) {
							connect(dcNode, toDir)
							return true
						}
					}
					else if (canConnect(dcNode, toDir)) {
						connect(dcNode, toDir)
						return true
					}
				}

				disconnect(absDir)
			}

			/**
			 * Can't find another wire. Try TileEntity.
			 */
			val tileEntity = pos.getTileEntity(world)
			val dcNode = getComponent(tileEntity, fromDir)

			if (canConnect(dcNode, toDir)) {
				if (dcNode.canConnect(this, Direction.UNKNOWN)) {
					connect(dcNode, toDir)
					return true
				}
			}

			disconnect(absDir)
			return false
		}

		def setCornerConnection(r: Int, absDir: Int): Boolean = {
			val cornerPos = new BlockCoord(tile)
			cornerPos.offset(absDir)

			if (canConnectThroughCorner(cornerPos, absDir ^ 1, side)) {
				cornerPos.offset(side)

				val tpCorner = MultipartUtil.getMultipartTile(world, cornerPos)

				if (tpCorner != null) {
					val part = tpCorner.partMap(absDir ^ 1)

					if (part != null) {
						val absToDir = Direction.getOrientation(absDir)
						val absFromDir = Direction.getOrientation(absDir).getOpposite
						val node = part.asInstanceOf[INodeProvider].getNode(classOf[NodeElectricComponent], absFromDir)

						if (canConnect(node, absFromDir)) {
							//TODO: Check dir
							connect(node, absToDir)
							return true
						}
					}
				}
			}
			return false
		}

		def connectionOpen(r: Int): Boolean = {
			val absDir = Rotation.rotateSide(side, r)
			val facePart = tile.partMap(absDir)
			val toDir = Direction.getOrientation(absDir)

			if (facePart != null && (!facePart.isInstanceOf[BlockWire] || !canConnect(facePart.asInstanceOf[INodeProvider].getNode(classOf[NodeElectricComponent], toDir.getOpposite), toDir.getOpposite))) {
				return false
			}
			if (tile.partMap(PartMap.edgeBetween(side, absDir)) != null) {
				return false
			}

			return true
		}

		/**
		 * Return a corner connection state. 0 = No connection 1 = Physical connection 2 = Render
		 * connection
		 */
		def connectCorner(r: Int): Int = {
			val absDir: Int = Rotation.rotateSide(side, r)
			val pos: BlockCoord = new BlockCoord(tile)
			pos.offset(absDir)

			if (!canConnectThroughCorner(pos, absDir ^ 1, side)) {
				return 0
			}

			pos.offset(side)
			val t: TileMultipart = MultipartUtil.getMultipartTile(world, pos)

			if (t != null) {
				val part = t.partMap(absDir ^ 1)
				val dcNode = getComponent(part, Direction.getOrientation(absDir))

				if (canConnect(dcNode, Direction.getOrientation(absDir))) {
					if (dcNode.isInstanceOf[NodeFlatWire]) {
						if (dcNode.asInstanceOf[NodeFlatWire].connectCorner(BlockWire.this, Rotation.rotationTo(absDir ^ 1, side ^ 1))) {
							if (!renderThisCorner(part.asInstanceOf[BlockWire])) {
								return 1
							}
							return 2
						}
					}
					return 2
				}
			}
			return 0
		}

		def canConnectThroughCorner(pos: BlockCoord, side1: Int, side2: Int): Boolean = {
			if (world.isAirBlock(pos.x, pos.y, pos.z)) {
				return true
			}
			val t: TileMultipart = MultipartUtil.getMultipartTile(world, pos)
			if (t != null) {
				return t.partMap(side1) == null && t.partMap(side2) == null && t.partMap(PartMap.edgeBetween(side1, side2)) == null
			}
			return false
		}

		def connectStraight(r: Int): Boolean = {
			val absDir = Rotation.rotateSide(side, r)
			val pos = new BlockCoord(tile).offset(absDir)
			val t = MultipartUtil.getMultipartTile(world, pos)
			val toDir = Direction.getOrientation(absDir)
			val fromDir = toDir.getOpposite

			if (t != null) {
				val part = t.partMap(side)
				val dcNode = getComponent(part, Direction.getOrientation(absDir))

				if (canConnect(dcNode, Direction.getOrientation(absDir))) {
					if (dcNode.isInstanceOf[NodeFlatWire]) {
						return dcNode.asInstanceOf[NodeFlatWire].connectStraight(BlockWire.this, (r + 2) % 4)
					}

					return true
				}
			}
			else {
				val tileEntity = world.getTileEntity(pos.x, pos.y, pos.z)
				val dcNode = getComponent(tileEntity, fromDir)

				if (dcNode != null)
					return canConnect(dcNode, toDir) && dcNode.canConnect(this, fromDir)
			}

			return false
		}

		def connectInternal(r: Int): Boolean = {
			val absDir = Rotation.rotateSide(side, r)
			val toDir = Direction.getOrientation(absDir)

			if (tile.partMap(PartMap.edgeBetween(absDir, side)) != null) {
				return false
			}

			val part = tile.partMap(absDir)
			val dcNode = getComponent(part, toDir)

			if (canConnect(dcNode, toDir)) {
				if (dcNode.isInstanceOf[NodeFlatWire])
					return dcNode.asInstanceOf[NodeFlatWire].connectInternal(BlockWire.this, Rotation.rotationTo(absDir, side))
			}

			return false
		}

		def connectCenter: Boolean = {
			val tp = tile.partMap(6)

			//TODO: Check other appliances that may connect to center but are not wires?
			if (tp.isInstanceOf[BlockWire]) {
				val dcNode = tp.asInstanceOf[BlockWire].getNode(classOf[NodeFlatWire], null)

				if (canConnect(dcNode, null)) {
					return dcNode.connectInternal(BlockWire.this, side)
				}
			}

			return false
		}

		def connectCorner(wire: BlockWire, r: Int): Boolean = {
			val absDir = Rotation.rotateSide(side, r)
			val toDir = Direction.getOrientation(absDir)
			val fromDir = toDir.getOpposite
			val dcNode = getComponent(wire, fromDir)

			if (canConnect(dcNode, fromDir) && maskOpen(r)) {
				val oldConn: Int = BlockWire.this.connectionMask
				BlockWire.this.connectionMask |= 0x1 << r
				if (renderThisCorner(wire)) {
					BlockWire.this.connectionMask |= 0x100000 << r
				}
				if (oldConn != BlockWire.this.connectionMask) {
					sendPacket(3)
				}
				return true
			}
			return false
		}

		def connectStraight(wire: BlockWire, r: Int): Boolean = {
			val absDir = Rotation.rotateSide(side, r)
			val toDir = Direction.getOrientation(absDir)
			val fromDir = toDir.getOpposite
			val dcNode = getComponent(wire, fromDir)

			if (canConnect(dcNode, fromDir) && maskOpen(r)) {
				val oldConn: Int = BlockWire.this.connectionMask
				BlockWire.this.connectionMask |= 0x10 << r
				if (oldConn != BlockWire.this.connectionMask) {
					sendPacket(3)
				}
				return true
			}
			return false
		}

		def connectInternal(wire: BlockWire, r: Int): Boolean = {
			val absDir = Rotation.rotateSide(side, r)
			val toDir = Direction.getOrientation(absDir)
			val fromDir = toDir.getOpposite
			val dcNode = getComponent(wire, fromDir)

			if (canConnect(dcNode, fromDir)) {
				val oldConn: Int = BlockWire.this.connectionMask
				BlockWire.this.connectionMask |= 0x100 << r
				if (oldConn != BlockWire.this.connectionMask) {
					sendPacket(3)
				}
				return true
			}
			return false
		}

		override def canConnect[B <: NodeElectricComponent](node: B, from: Direction): Boolean = {
			if (node.isInstanceOf[NodeFlatWire]) {
				val wire = node.asInstanceOf[NodeFlatWire].getParent.asInstanceOf[TWire]

				if (material == wire.material) {
					if (insulated && wire.insulated)
						return BlockWire.this.getColor == wire.getColor || (getColor == TColorable.defaultColor || wire.getColor == TColorable.defaultColor)

					return true
				}
			}

			return super.canConnect(node, from)
		}

		/**
		 * Recalculates connections to blocks outside this space
		 *
		 * @return true if a new connection was added or one was removed
		 */
		protected def updateExternalConnections(): Boolean = {
			var newConn: Int = 0

			for (r <- 0 until 4) {
				if (maskOpen(r)) {
					if (connectStraight(r)) {
						newConn |= 0x10 << r
					}
					else {
						val cnrMode: Int = connectCorner(r)
						if (cnrMode != 0) {
							newConn |= 1 << r
							if (cnrMode == 2) {
								newConn |= 0x100000 << r
							}
						}
					}
				}
			}

			if (newConn != (BlockWire.this.connectionMask & 0xF000FF)) {
				val diff: Int = BlockWire.this.connectionMask ^ newConn
				BlockWire.this.connectionMask = BlockWire.this.connectionMask & ~0xF000FF | newConn

				for (r <- 0 until 4) {
					if ((diff & 1 << r) != 0) {
						notifyCornerChange(r)
					}
				}

				return true
			}
			return false
		}

		/**
		 * Recalculates connections to other parts within this space
		 *
		 * @return true if a new connection was added or one was removed
		 */
		protected def updateInternalConnections(): Boolean = {
			var newConn: Int = 0

			for (r <- 0 until 4) {
				if (connectInternal(r)) {
					newConn |= 0x100 << r
				}
			}

			if (connectCenter) {
				newConn |= 0x10000
			}
			if (newConn != (BlockWire.this.connectionMask & 0x10F00)) {
				BlockWire.this.connectionMask = BlockWire.this.connectionMask & ~0x10F00 | newConn
				return true
			}
			return false
		}

		/**
		 * Recalculates connections that can be made to other parts outside of this space
		 *
		 * @return true if external connections should be recalculated
		 */
		protected def updateOpenConnections(): Boolean = {
			var newConn: Int = 0

			for (r <- 0 until 4) {
				if (connectionOpen(r)) {
					newConn |= 0x1000 << r
				}
			}

			if (newConn != (BlockWire.this.connectionMask & 0xF000)) {
				BlockWire.this.connectionMask = BlockWire.this.connectionMask & ~0xF000 | newConn
				return true
			}

			return false
		}

		private def disconnect(i: Int) {
			if (!world.isRemote) {
				//TODO: Refine this. It's very hacky and may cause errors when the wire connects to a block both ways
				val inverseCon = directionMap.map(_.swap)
				val forgeDir = Direction.getOrientation(i)

				if (inverseCon.contains(forgeDir)) {
					val connected = inverseCon(forgeDir)

					if (connected != null) {
						disconnect(connected)
					}
				}
			}
		}

		/**
		 * Gets a potential DCNode from an object.
		 */
		private def getComponent(obj: AnyRef, from: Direction): NodeElectricComponent = {
			if (obj.isInstanceOf[INodeProvider])
				return obj.asInstanceOf[INodeProvider].getNode(classOf[NodeElectricComponent], from)

			return null
		}
	}

}