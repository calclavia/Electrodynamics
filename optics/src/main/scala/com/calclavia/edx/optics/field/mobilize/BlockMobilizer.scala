package com.calclavia.edx.optics.field.mobilize

import java.util.{Set => JSet}

import com.calclavia.edx.core.EDX
import com.calclavia.edx.optics.api.MFFSEvent.EventForceMobilize
import com.calclavia.edx.optics.api.card.CoordLink
import com.calclavia.edx.optics.api.{Blacklist, MFFSEvent}
import com.calclavia.edx.optics.component.{BlockFieldMatrix, BlockPacketID}
import com.calclavia.edx.optics.content.{OpticsContent, OpticsModels, OpticsTextures}
import com.calclavia.edx.optics.fx.{FXHologramProgress, IEffectController}
import com.calclavia.edx.optics.security.{MFFSPermissions, PermissionHandler}
import com.calclavia.edx.optics.util.OpticUtility
import com.calclavia.edx.optics.{Optics, Settings}
import nova.core.component.inventory.InventorySimple
import nova.core.component.misc.Collider
import nova.core.component.renderer.StaticRenderer
import nova.core.component.transform.Orientation
import nova.core.entity.Entity
import nova.core.entity.component.RigidBody
import nova.core.item.Item
import nova.core.network.NetworkTarget.Side
import nova.core.network.{Packet, Sync}
import nova.core.render.Color
import nova.core.render.model.Model
import nova.core.retention.{Data, Storable, Store}
import nova.core.util.Direction
import nova.core.util.shape.Cuboid
import nova.core.world.World
import nova.scala.wrapper.FunctionalWrapper._
import nova.scala.wrapper.VectorWrapper._
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D

import scala.collection.convert.wrapAll._

class BlockMobilizer extends BlockFieldMatrix with IEffectController with PermissionHandler {
	@Store
	@Sync(ids = Array(BlockPacketID.description, BlockPacketID.inventory))
	override val inventory = add(new InventorySimple(1 + 25))

	val packetRange = 60
	val animationTime = 20
	var failedPositions = Set.empty[Vector3D]
	@Store
	var anchor = Vector3D.ZERO
	/**
	 * The display mode. 0 = none, 1 = minimal, 2 = maximal.
	 */
	@Store
	var previewMode = 1
	@Store
	var doAnchor = true
	var clientMoveTime = 0
	var performingMove = false
	/**
	 * Marking failures
	 */
	private var failedMove = false
	/**
	 * Used ONLY for teleporting.
	 */
	private var moveTime = 0
	private var canRenderMove = true

	get(classOf[StaticRenderer])
		.onRender(
			(model: Model) => {
				model.matrix.rotate(get(classOf[Orientation]).orientation.rotation)
				val subModel = OpticsModels.mobilizer.getModel
				model.children.add(subModel)
				subModel.bindAll(if (isActive) OpticsTextures.mobilizerOn else OpticsTextures.mobilizerOff)
			}
		)

	get(classOf[Orientation]).setMask(63)
	get(classOf[Collider]).isCube(false)

	def markFailMove() = failedMove = true

	override def getID: String = "mobilizer"

	override def update(deltaTime: Double) {
		super.update(deltaTime)

		if (getShapeItem != null && Settings.enableForceManipulator) {
			//TODO: Prevent move when it's already moving?
			checkActivation()
			whileMoving()

			executePreviews()
			executeFailures()
		}
		else if (EDX.network.isServer && isActive) {
			setActive(false)
		}
	}

	def checkActivation() {
		if (EDX.network.isServer) {
			if (isActive && !performingMove) {
				if (calculatedField != null) {
					performingMove = true
					executeMovement()
					calculatedField = null

					if (EDX.network.isServer) {
						setActive(false)
					}
				}
				else {
					calculateField()
				}
			}
		}
	}

	/**
	 * @return True if we started moving.
	 */
	def executeMovement(): Boolean = {
		/**
		 * Check if there is a valid field that has been calculated. If so, we will move this field.
		 */
		val movedBlocks = moveBlocks(calculatedField)

		if (movedBlocks.size > 0) {
			/**
			 * Queue an entity move event.
			 */
			EDX.syncTicker.preQueue(new DelayedEvent(getMoveTime, () => {
				moveEntities()
				EDX.network.sync(BlockPacketID.field, this)

				if (!isTeleport && doAnchor) {
					anchor += get(classOf[Orientation]).orientation.toVector
				}
			}))

			val renderBlocks = movedBlocks
				.filter(isVisibleToPlayer)
				.take(Settings.maxForceFieldsPerTick)

			if (renderBlocks.size > 0) {
				/**
				 * If we have more than one block that is visible that was moved, we will tell the client to render it.
				 *
				 * Packet Params: id, Type1, Type2, Size, the coordinate
				 */
				val packet = EDX.network.newPacket()
				packet.setID(BlockPacketID.effect)

				if (!isTeleport) {
					packet <<< 1
					packet <<< 2
					packet <<< renderBlocks

					if (getModuleCount(OpticsContent.moduleSilence) <= 0) {
						//worldObj.playSoundEffect(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D, Reference.prefix + "fieldmove", 0.6f, 1 - this.worldObj.rand.nextFloat * 0.1f)
					}

					EDX.network.sendPacket(this, packet)
					//ModularForceFieldSystem.packetHandler.sendToAllAround(packet, world, position, packetRange)
				}
				else {
					packet <<< 2
					packet <<< getMoveTime
					packet <<< (getAbsoluteAnchor + 0.5)
					packet <<< (getTargetPosition._2 + 0.5)
					packet <<< false
					packet <<< renderBlocks

					moveTime = getMoveTime
					EDX.network.sendPacket(this, packet)
				}
			}

			return true
		}
		else {
			markFailMove()
		}

		return false
	}

	def whileMoving() {
		if (EDX.network.isServer && performingMove) {
			//if (removeFortron(getFortronCost, false) >= getFortronCost) {

			if (moveTime > 0) {
				if (isTeleport) {
					if (getModuleCount(OpticsContent.moduleSilence) <= 0 && ticks % 10 == 0) {
						val moveTime = getMoveTime
						//worldObj.playSoundEffect(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D, Reference.prefix + "fieldmove", 1.5f, 0.5f + 0.8f * (moveTime - this.moveTime) / moveTime)
					}

					moveTime -= 1

					if (moveTime <= 0) {
						//worldObj.playSoundEffect(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D, Reference.prefix + "teleport", 0.6f, 1 - this.worldObj.rand.nextFloat * 0.1f)
					}
				}
			}

			return
			//	}

			markFailMove()
		}
	}

	def executePreviews() {
		if (EDX.network.isServer) {
			if (previewMode > 0 && Settings.highGraphics && !performingMove) {
				if (calculatedField == null) {
					calculateField()
				}

				/**
				 * Send preview field packet
				 */
				if (ticks % 120 == 0 && calculatedField != null) {
					val renderBlocks = getInteriorPoints.view
						.filter(isVisibleToPlayer)
						.filter(pos => previewMode == 2 || !EDX.blocks.getAirBlock.sameType(world.getBlock(pos).get()))
						.take(Settings.maxForceFieldsPerTick)

					val packet = EDX.network.newPacket()
					packet <<< BlockPacketID.effect

					if (isTeleport) {
						val targetPosition: Vector3D = {
							if (getTargetPosition._1 == null) {
								getTargetPosition._2
							}
							else {
								getTargetPosition._2
							}
						}

						packet <<< 2
						packet <<< 60
						packet <<< (getAbsoluteAnchor + 0.5)
						packet <<< (targetPosition + 0.5)
						packet <<< true

					}
					else {
						packet <<< 1
						packet <<< 1
					}

					packet <<< renderBlocks
					EDX.network.sendPacket(this, packet)
				}
			}
		}
	}

	def executeFailures() {
		/**
		 * Check if the move failed. If so, we tell the client which positions were the cause of failure.
		 */
		if (failedMove) {
			/**
			 * Stop teleportation field
			 */
			moveTime = 0
			performingMove = false

			//			delayedEvents.clear()
			//			worldObj.playSoundEffect(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D, Reference.prefix + "powerdown", 0.6f, 1 - this.worldObj.rand.nextFloat * 0.1f)
			//			val playPoint = position + anchor + 0.5
			//			worldObj.playSoundEffect(playPoint.x, playPoint.y, playPoint.z, Reference.prefix + "powerdown", 0.6f, 1 - this.worldObj.rand.nextFloat * 0.1f)
			//			ModularForceFieldSystem.packetHandler.sendToAllAround(new PacketTile(this) <<< PacketBlock.render.id, world, position, packetRange)


			if (failedPositions.size > 0) {
				/**
				 * Send failure coordinates to client
				 */
				val packet = EDX.network.newPacket()
				packet <<< BlockPacketID.effect
				packet <<< 3
				packet <<< failedPositions.asInstanceOf[JSet[Vector3D]]
				EDX.network.sendPacket(this, packet)
				//				ModularForceFieldSystem.packetHandler.sendToAllAround(packet, world, position, packetRange)
			}

			failedMove = false
			failedPositions.clear()
		}
	}

	override def generateField: JSet[Vector3D] = {
		if (!canMove) {
			return Set.empty[Vector3D]
		}
		/*else
			markFailMove()*/

		return getInteriorPoints
	}

	/**
	 * Scan target field area to see if we can move this block. Called on a separate thread.
	 */
	def canMove: Boolean = {
		val mobilizationPoints = getInteriorPoints
		val targetCenterPosition = getTargetPosition

		for (position <- mobilizationPoints) {
			if (EDX.blocks.getAirBlock.sameType(world.getBlock(position).get())) {
				val relativePosition = position - getAbsoluteAnchor
				val targetPosition = targetCenterPosition._2 + relativePosition

				if (!canMove(world(), position, targetCenterPosition._1, targetPosition)) {
					failedPositions.add(position)
					return false
				}
			}
		}
		return true
	}

	/**
	 * Checks if a specific block can be moved from its position to a target
	 * @param position - The position of the block to be moved.
	 * @param target - The target position
	 * @return True if the block can be moved.
	 */
	def canMove(startWorld: World, position: Vector3D, targetWorld: World, target: Vector3D): Boolean = {
		if (Blacklist.mobilizerBlacklist.contains(startWorld.getBlock(position).get())) {
			return false
		}
		val evt = new EventForceMobilize(startWorld, position, targetWorld, target)
		MFFSEvent.instance.checkMobilize.publish(evt)

		if (evt.isCanceled) {
			return false
		}

		//TODO: Check the username ID for the Mobilizer
		if (!OpticUtility.hasPermission(startWorld, position, MFFSPermissions.blockAlter, Optics.tempID) && !OpticUtility.hasPermission(targetWorld, target, MFFSPermissions.blockAlter, Optics.tempID)) {
			return false
		}

		val targetBlock = targetWorld.getBlock(target).get()

		if (targetBlock.equals(this)) {
			return false
		}
		for (checkPos <- this.getInteriorPoints) {
			if (checkPos == target) {
				return true
			}
		}

		return EDX.blocks.getAirBlock.sameType(targetBlock)
	}

	def isVisibleToPlayer(position: Vector3D): Boolean = Direction.VALID_DIRECTIONS.count(dir => world.getBlock(position + dir.toVector).get.get(classOf[Collider]).isOpaqueCube.get()) < 6

	override def read(packet: Packet) {
		super.read(packet)

		if (Side.get().isClient) {
			packet.getID match {
				case BlockPacketID.effect => {
					packet.readInt() match {
						case 1 =>

							/**
							 * If we have more than one block that is visible that was moved, we will tell the client to render it.
							 *
							 * Params: id, Type1, Type2, Size, the coordinate
							 */
							val isTeleportPacket = packet.readInt()
							val vecSize = packet.readInt()

							val hologramRenderPoints = packet.readSet[Vector3D]()

							/**
							 * Movement Rendering
							 */
							val direction = get(classOf[Orientation]).orientation

							hologramRenderPoints.foreach(
								pos =>
									world
										.addClientEntity(OpticsContent.fxHologramProgress).asInstanceOf[FXHologramProgress]
										.color = (
										isTeleportPacket match {
											case 1 => Color.blue
											case 2 => Color.green
										}
										))
						case 2 => {
							/**
							 * Teleportation Rendering
							 */
							val animationTime = packet.readInt()
							val anchorPosition = packet.readStorable()
							val targetPosition = packet.readStorable()
							val isPreview = packet.readBoolean()
							val vecSize = packet.readInt()
							val hologramRenderPoints = packet.readSet[Vector3D]()

							val color = if (isPreview) Color.blue else Color.green

							hologramRenderPoints.foreach(pos => {
								//Render teleport start
								val hologramA = world.addClientEntity(OpticsContent.fxHologramProgress).asInstanceOf[FXHologramProgress]
								hologramA.color = (color)
								hologramA.transform.setPosition(pos + 0.5)
								//TODO: Not clean
								/*
								if (targetPosition.world != null && targetPosition.world.getChunkProvider.chunkExists(targetPosition.xi, targetPosition.zi)) {
									//Render teleport end
									val destination = pos - anchorPosition + targetPosition
									ModularForceFieldSystem.proxy.renderHologramOrbit(this, targetPosition.world, targetPosition, destination, color, animationTime, 30f)
								}*/
							})

							canRenderMove = true
						}
						case 3 => {
							/**
							 * Fail hologram rendering
							 */
							val vecSize = packet.readInt()
							val hologramRenderPoints = packet.readSet[Vector3D]()
							hologramRenderPoints.foreach(p => world.addClientEntity(new FXHologramProgress(Color.red, 30)).transform.setPosition(p + 0.5))
						}
					}
				}

				case BlockPacketID.render =>
					canRenderMove = false
				case BlockPacketID.field =>
					moveEntities()
				case BlockPacketID.description =>
					anchor = packet.readStorable().asInstanceOf[Vector3D]
					previewMode = packet.readInt()
					doAnchor = packet.readBoolean()
					clientMoveTime = packet.readInt
				case _ =>
			}
		}
		else {
			packet.getID match {
				case BlockPacketID.toggleMode =>
					anchor = Vector3D.ZERO
				case BlockPacketID.toggleMode2 =>
					previewMode = (previewMode + 1) % 3
				case BlockPacketID.toggleMode3 =>
					doAnchor = !doAnchor
			}
		}
	}

	protected def moveEntities() {
		val targetLocation = getTargetPosition
		val bounds = getSearchBounds

		if (bounds != null) {
			val entities = world.getEntities(bounds)
			entities.foreach(entity => moveEntity(entity, targetLocation._1, targetLocation._2 + 0.5 + entity.transform.position() - (getAbsoluteAnchor + 0.5)))
		}
	}

	/**
	 * Gets the position in which the manipulator will try to translate the field into.
	 *
	 * @return A vector of the target position.
	 */
	def getTargetPosition: (World, Vector3D) = {
		if (isTeleport) {
			val cardStack = getLinkCard

			if (cardStack != null) {
				val link = cardStack.asInstanceOf[CoordLink].getLink
				return (link._1, link._2)
			}
		}

		return (world(), getAbsoluteAnchor + get(classOf[Orientation]).orientation.toVector)
	}

	private def isTeleport: Boolean = {
		if (Settings.allowForceManipulatorTeleport) {
			val cardStack = getLinkCard

			if (cardStack != null) {
				return cardStack.asInstanceOf[CoordLink].getLink != null
			}
		}
		return false
	}

	def getLinkCard: Item = {
		inventory
			.filter(_ != null)
			.find(_.isInstanceOf[CoordLink]) match {
			case Some(item) => return item
			case _ => return null
		}
	}

	def getAbsoluteAnchor: Vector3D = transform.position + anchor

	def getSearchBounds: Cuboid = {
		val positiveScale = transform.position + getTranslation + getPositiveScale + 1
		val negativeScale = transform.position + getTranslation - getNegativeScale
		val minScale = positiveScale.min(negativeScale)
		val maxScale = positiveScale.max(negativeScale)
		return new Cuboid(minScale, maxScale)
	}

	override def getTranslation: Vector3D = super.getTranslation + anchor

	protected def moveEntity(entity: Entity, targetWorld: World, targetPos: Vector3D) {
		if (entity != null && targetPos != null) {
			if (!entity.transform.world().sameType(targetWorld)) {
				entity.transform.setWorld(targetWorld)
				//entity.travelToDimension(targetPos.world.block.dimensionId)
			}

			entity.get(classOf[RigidBody]).setVelocity(Vector3D.ZERO)
		}
	}

	override def write(packet: Packet) {
		super.write(packet)

		if (packet.getID == BlockPacketID.description) {
			packet <<< anchor
			packet <<< previewMode
			packet <<< doAnchor
			packet <<< (if (moveTime > 0) moveTime else getMoveTime)
		}
	}

	/**
	 * Gets the movement time required in TICKS.
	 *
	 * @return The time it takes to teleport (using a link card) to another coordinate OR
	 *         ANIMATION_TIME for default move.
	 */
	def getMoveTime: Int = {
		if (isTeleport) {
			var time = (20 * getTargetPosition._2.distance(this.getAbsoluteAnchor)).toInt
			if (this.getTargetPosition._1 != world) {
				time += 20 * 60
			}
			return time
		}
		return animationTime
	}

	/*
	override def isItemValidForSlot(slotID: Int, item: Item): Boolean = {
		if (slotID == 0) {
			return item.isInstanceOf[ItemCard]
		}
		else if (slotID == modeSlotID) {
			return item.isInstanceOf[ProjectorMode]
		}

		return item.isInstanceOf[Module] || item.isInstanceOf[CoordLink]
	}
	*/

	def canContinueEffect = canRenderMove

	/*
	   def getMethodNames: Array[String] =
	  {
		return Array[String]("isActivate", "setActivate", "resetAnchor", "canMove")
	  }
	
	  def callMethod(computer: Vector3d, context: Vector3d, method: Int, arguments: Array[AnyRef]): Array[AnyRef] =
	  {
		method match
		{
		  case 2 =>
		  {
			this.anchor = new Vector3d
			return null
		  }
		  case 3 =>
		  {
			val result: Array[AnyRef] = Array(false)
			if (this.isActive || this.isCalculatingManipulation)
			{
			  return result
			}
			else
			{
			  result(0) = this.canMove
			  this.failedPositions.clear
			  return result
			}
		  }
		}
		return super.callMethod(computer, context, method, arguments)
	  }
	*/

	/**
	 * Called to start the block moving operation to move a set of blocks to a target.
	 * @param blockPositions - The set of block positions to be moved.
	 * @return The set of block positions actually moved
	 */
	protected def moveBlocks(blockPositions: Set[Vector3D]): Set[Vector3D] = {
		if (EDX.network.isServer) {
			val actualMovables = blockPositions
				.filter(blockPos => {
				val opBlock = world.getBlock(blockPos)
				opBlock.isPresent && EDX.blocks.getAirBlock.sameType(opBlock.get()) && !sameType(opBlock.get())
			})

			var moveMap = Map.empty[(World, Vector3D), (World, Vector3D)]

			actualMovables.foreach(blockPos => {
				val relativePosition = blockPos - getAbsoluteAnchor
				val newPosition = getTargetPosition._2 + relativePosition
				moveMap += (world, blockPos) ->(getTargetPosition._1, newPosition)
			})

			EDX.syncTicker.preQueue(new DelayedEvent(getMoveTime, () => doMove(moveMap)))
		}

		return Set.empty
	}

	private def doMove(moveMap: Map[(World, Vector3D), (World, Vector3D)]) {

		//TODO: Check if parallel is ok.
		//Time has passed. Check if we can still move the blocks.
		val failedPos = moveMap
			.par
			.filterNot {
			case ((fromWorld, fromPos), (toWorld, toPos)) =>
				val evt = new EventForceMobilize(fromWorld, fromPos, toWorld, toPos)
				MFFSEvent.instance.preMobilize.publish(evt)
				!evt.isCanceled && canMove(fromWorld, fromPos, toWorld, toPos)
		}
			.map { case ((fromWorld, fromPos), (toWorld, toPos)) => fromPos }

		if (failedPos.size > 0) {
			failedPositions ++= failedPos
			markFailMove()
			return
		}

		var newDataMap = Map.empty[(World, Vector3D), (String, Data)]

		//Do the pre-move, which sets all blocks to air first.
		moveMap.foreach {
			case ((fromWorld, fromPos), (toWorld, toPos)) =>

				//Serialize the block
				val fromBlock = fromWorld.getBlock(fromPos).get()
				val id = fromBlock.getID

				if (fromBlock.isInstanceOf[Storable]) {
					val data = Data.serialize(fromBlock.asInstanceOf[Storable])
					newDataMap += (toWorld, toPos) ->(id, data)
				}
				else {
					newDataMap += (toWorld, toPos) ->(id, null)
				}

			//Optics.movementManager.setSneaky(fromWorld, fromPos, EDX.blocks.getAirBlock)
		}

		//Do the post-move, which sets all blocks to what they should be
		newDataMap.foreach {
			case ((world, pos), (id, data)) =>
			//Optics.movementManager.setSneaky(world, pos, EDX.blocks.get(id).get(), data)
		}

		//Notify block chang in both the old and new positions
		moveMap.foreach {
			case ((fromWorld, fromPos), (toWorld, toPos)) =>
				fromWorld.markChange(fromPos)
				toWorld.markChange(toPos)
		}
	}
}