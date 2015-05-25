package com.calclavia.edx.mffs.field.mobilize

import java.util.{Set => JSet}

import com.calclavia.edx.mffs.api.MFFSEvent.EventForceMobilize
import com.calclavia.edx.mffs.api.card.CoordLink
import com.calclavia.edx.mffs.api.{Blacklist, MFFSEvent}
import com.calclavia.edx.mffs.base.{BlockFieldMatrix, PacketBlock}
import com.calclavia.edx.mffs.content.{Content, Models, Textures}
import com.calclavia.edx.mffs.particle.{FXHologramProgress, FieldColor, IEffectController}
import com.calclavia.edx.mffs.security.{MFFSPermissions, PermissionHandler}
import com.calclavia.edx.mffs.util.MFFSUtility
import com.calclavia.edx.mffs.{ModularForceFieldSystem, Settings}
import com.resonant.core.prefab.block.InventorySimpleProvider
import nova.core.component.renderer.StaticRenderer
import nova.core.entity.Entity
import nova.core.entity.component.RigidBody
import nova.core.game.Game
import nova.core.inventory.InventorySimple
import nova.core.item.Item
import nova.core.network.{Packet, Sync}
import nova.core.render.model.Model
import nova.core.retention.{Data, Storable, Stored}
import nova.core.util.Direction
import nova.core.util.transform.matrix.MatrixStack
import nova.core.util.transform.shape.Cuboid
import nova.core.util.transform.vector.{Vector3d, Vector3i}
import nova.core.world.World

import scala.collection.convert.wrapAll._

class BlockMobilizer extends BlockFieldMatrix with IEffectController with InventorySimpleProvider with PermissionHandler with StaticRenderer {
	@Stored
	@Sync(ids = Array(PacketBlock.description, PacketBlock.inventory))
	override val inventory = new InventorySimple(1 + 25)

	val packetRange = 60
	val animationTime = 20
	var failedPositions = Set.empty[Vector3i]
	@Stored
	var anchor = new Vector3i()
	/**
	 * The display mode. 0 = none, 1 = minimal, 2 = maximal.
	 */
	@Stored
	var previewMode = 1
	@Stored
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

	rotationMask = 63

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
		else if (Game.instance.networkManager.isServer && isActive) {
			setActive(false)
		}
	}

	def checkActivation() {
		if (Game.instance.networkManager.isServer) {
			if (isActive && !performingMove) {
				if (calculatedField != null) {
					performingMove = true
					executeMovement()
					calculatedField = null

					if (Game.instance.networkManager.isServer) {
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
			Game.instance.syncTicker.preQueue(new DelayedEvent(getMoveTime, () => {
				moveEntities()
				Game.instance.networkManager.sync(PacketBlock.field, this)

				if (!isTeleport && doAnchor) {
					anchor += direction.toVector
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
				val packet = Game.instance.networkManager.newPacket()
				packet.setID(PacketBlock.effect)

				if (!isTeleport) {
					packet <<< 1
					packet <<< 2
					packet <<< renderBlocks

					if (getModuleCount(Content.moduleSilence) <= 0) {
						//worldObj.playSoundEffect(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D, Reference.prefix + "fieldmove", 0.6f, 1 - this.worldObj.rand.nextFloat * 0.1f)
					}

					Game.instance.networkManager.sendPacket(this, packet)
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
					Game.instance.networkManager.sendPacket(this, packet)
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
		if (Game.instance.networkManager.isServer && performingMove) {
			if (removeFortron(getFortronCost, false) >= getFortronCost) {
				removeFortron(getFortronCost, true)

				if (moveTime > 0) {
					if (isTeleport) {
						if (getModuleCount(Content.moduleSilence) <= 0 && ticks % 10 == 0) {
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
			}

			markFailMove()
		}
	}

	def executePreviews() {
		if (Game.instance.networkManager.isServer) {
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
						.filter(pos => previewMode == 2 || !Game.instance.blockManager.getAirBlock.sameType(world.getBlock(pos).get()))
						.take(Settings.maxForceFieldsPerTick)

					val packet = Game.instance.networkManager.newPacket()
					packet <<< PacketBlock.effect

					if (isTeleport) {
						val targetPosition: Vector3i = {
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
					Game.instance.networkManager.sendPacket(this, packet)
					markDirty()
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
				val packet = Game.instance.networkManager.newPacket()
				packet <<< PacketBlock.effect
				packet <<< 3
				packet <<< failedPositions.asInstanceOf[JSet[Vector3i]]
				Game.instance.networkManager.sendPacket(this, packet)
				//				ModularForceFieldSystem.packetHandler.sendToAllAround(packet, world, position, packetRange)
			}

			failedMove = false
			failedPositions.clear()
		}
	}

	override def generateField: JSet[Vector3i] = {
		if (!canMove) {
			return Set.empty[Vector3i]
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
			if (Game.instance.blockManager.getAirBlock.sameType(world.getBlock(position).get())) {
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
	def canMove(startWorld: World, position: Vector3i, targetWorld: World, target: Vector3i): Boolean = {
		if (Blacklist.mobilizerBlacklist.contains(startWorld.getBlock(position).get())) {
			return false
		}
		val evt = new EventForceMobilize(startWorld, position, targetWorld, target)
		MFFSEvent.instance.checkMobilize.publish(evt)

		if (evt.isCanceled) {
			return false
		}

		//TODO: Check the username ID for the Mobilizer
		if (!MFFSUtility.hasPermission(startWorld, position.toDouble, MFFSPermissions.blockAlter, ModularForceFieldSystem.tempID) && !MFFSUtility.hasPermission(targetWorld, target.toDouble, MFFSPermissions.blockAlter, ModularForceFieldSystem.tempID)) {
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

		return Game.instance.blockManager.getAirBlock.sameType(targetBlock)
	}

	def isVisibleToPlayer(position: Vector3i): Boolean = {
		return Direction.DIRECTIONS.count(dir => world.getBlock(position + dir.toVector).get.isOpaqueCube) < 6
	}

	override def read(packet: Packet) {
		super.read(packet)

		if (Game.instance.networkManager.isClient) {
			packet.getID match {
				case PacketBlock.effect => {
					packet.readInt() match {
						case 1 =>

							/**
							 * If we have more than one block that is visible that was moved, we will tell the client to render it.
							 *
							 * Params: id, Type1, Type2, Size, the coordinate
							 */
							val isTeleportPacket = packet.readInt()
							val vecSize = packet.readInt()

							val hologramRenderPoints = packet.readSet[Vector3i]()

							/**
							 * Movement Rendering
							 */
							val direction = getDirection

							hologramRenderPoints.foreach(
								pos =>
									world
										.addClientEntity(Content.fxHologramProgress).asInstanceOf[FXHologramProgress]
										.setColor(
									    isTeleportPacket match {
										    case 1 => FieldColor.blue
										    case 2 => FieldColor.green
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
							val hologramRenderPoints = packet.readSet[Vector3i]()

							val color = if (isPreview) FieldColor.blue else FieldColor.green

							hologramRenderPoints.foreach(pos => {
								//Render teleport start
								val hologramA = world.addClientEntity(Content.fxHologramProgress).asInstanceOf[FXHologramProgress]
								hologramA.setColor(color)
								hologramA.setPosition(pos.toDouble + 0.5)
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
							val hologramRenderPoints = packet.readSet[Vector3i]()
							hologramRenderPoints.foreach(p => world.addClientEntity(new FXHologramProgress(FieldColor.red, 30)).setPosition(p.toDouble + 0.5))
						}
					}
				}

				case PacketBlock.render =>
					canRenderMove = false
				case PacketBlock.field =>
					moveEntities()
				case PacketBlock.description =>
					anchor = packet.readStorable().asInstanceOf[Vector3i]
					previewMode = packet.readInt()
					doAnchor = packet.readBoolean()
					clientMoveTime = packet.readInt
				case _ =>
			}
		}
		else {
			packet.getID match {
				case PacketBlock.toggleMode =>
					anchor = new Vector3i()
					markDirty()
				case PacketBlock.toggleMode2 =>
					previewMode = (previewMode + 1) % 3
				case PacketBlock.toggleMode3 =>
					doAnchor = !doAnchor
			}
		}
	}

	override def markDirty() {
		super.markDirty()

		if (world != null) {
			clearCache()
			calculateField()
		}
	}

	protected def moveEntities() {
		val targetLocation = getTargetPosition
		val bounds = getSearchBounds

		if (bounds != null) {
			val entities = world.getEntities(bounds)
			entities.foreach(entity => moveEntity(entity, targetLocation._1, targetLocation._2.toDouble + 0.5 + entity.position() - (getAbsoluteAnchor.toDouble + 0.5)))
		}
	}

	/**
	 * Gets the position in which the manipulator will try to translate the field into.
	 *
	 * @return A vector of the target position.
	 */
	def getTargetPosition: (World, Vector3i) = {
		if (isTeleport) {
			val cardStack = getLinkCard

			if (cardStack != null) {
				val link = cardStack.asInstanceOf[CoordLink].getLink
				return (link._1, link._2)
			}
		}

		return (world(), getAbsoluteAnchor + direction.toVector)
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

	def getAbsoluteAnchor: Vector3i = position + anchor

	def getSearchBounds: Cuboid = {
		val positiveScale = position + getTranslation + getPositiveScale + 1
		val negativeScale = position + getTranslation - getNegativeScale
		val minScale = positiveScale.min(negativeScale)
		val maxScale = positiveScale.max(negativeScale)
		return new Cuboid(minScale, maxScale)
	}

	override def getTranslation: Vector3i = super.getTranslation + anchor

	protected def moveEntity(entity: Entity, targetWorld: World, targetPos: Vector3d) {
		if (entity != null && targetPos != null) {
			if (!entity.world().sameType(targetWorld)) {
				entity.setWorld(targetWorld)
				//entity.travelToDimension(targetPos.world.provider.dimensionId)
			}

			entity.getComponent(classOf[RigidBody]).get().setVelocity(Vector3d.zero)
		}
	}

	override def write(packet: Packet) {
		super.write(packet)

		if (packet.getID == PacketBlock.description) {
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

	override def doGetFortronCost: Int = Math.round(super.doGetFortronCost + (if (this.anchor != null) this.anchor.magnitude * 1000 else 0)).toInt

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

	override def isCube: Boolean = false

	override def renderStatic(model: Model) {
		model.matrix = new MatrixStack()
			.loadMatrix(model.matrix)
			.rotate(direction.rotation)
			.getMatrix

		model.children.add(Models.mobilizer.getModel)
		model.bindAll(if (isActive) Textures.mobilizerOn else Textures.mobilizerOff)
	}

	/**
	 * Called to start the block moving operation to move a set of blocks to a target.
	 * @param blockPositions - The set of block positions to be moved.
	 * @return The set of block positions actually moved
	 */
	protected def moveBlocks(blockPositions: Set[Vector3i]): Set[Vector3i] = {
		if (Game.instance.networkManager.isServer) {
			val actualMovables = blockPositions
				.filter(blockPos => {
				val opBlock = world.getBlock(blockPos)
				opBlock.isPresent && Game.instance.blockManager.getAirBlock.sameType(opBlock.get()) && !sameType(opBlock.get())
			})

			var moveMap = Map.empty[(World, Vector3i), (World, Vector3i)]

			actualMovables.foreach(blockPos => {
				val relativePosition = blockPos - getAbsoluteAnchor
				val newPosition = getTargetPosition._2 + relativePosition
				moveMap += (world, blockPos) ->(getTargetPosition._1, newPosition)
			})

			Game.instance.syncTicker.preQueue(new DelayedEvent(getMoveTime, () => doMove(moveMap)))
		}

		return Set.empty
	}

	private def doMove(moveMap: Map[(World, Vector3i), (World, Vector3i)]) {

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

		var newDataMap = Map.empty[(World, Vector3i), (String, Data)]

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

				ModularForceFieldSystem.movementManager.setSneaky(fromWorld, fromPos, Game.instance.blockManager.getAirBlock)
		}

		//Do the post-move, which sets all blocks to what they should be
		newDataMap.foreach {
			case ((world, pos), (id, data)) =>
				ModularForceFieldSystem.movementManager.setSneaky(world, pos, Game.instance.blockManager.get(id).get(), data)
		}

		//Notify block chang in both the old and new positions
		moveMap.foreach {
			case ((fromWorld, fromPos), (toWorld, toPos)) =>
				fromWorld.markChange(fromPos)
				toWorld.markChange(toPos)
		}
	}
}