package mffs.field.mobilize

import mffs.base.{PacketBlock, TileFieldMatrix}
import mffs.field.mobilize.event.{BlockPreMoveDelayedEvent, DelayedEvent}
import mffs.item.card.ItemCard
import mffs.render.FieldColor
import mffs.render.fx.IEffectController
import mffs.security.MFFSPermissions
import mffs.util.MFFSUtility
import mffs.{Content, ModularForceFieldSystem, Reference, Settings}

class BlockMobilizer extends TileFieldMatrix with IEffectController
{
  val packetRange = 60
  val animationTime = 20

	val failedPositions = mutable.Set.empty[Vector3d]
	var anchor = new Vector3d()

  /**
   * The display mode. 0 = none, 1 = minimal, 2 = maximal.
   */
  var previewMode = 1
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

  def markFailMove() = failedMove = true

  rotationMask = 63

  override def getSizeInventory = 1 + 25

  override def update()
  {
    super.update()

    if (getMode != null && Settings.enableForceManipulator)
    {
      if (delayedEvents.size == 0)
        performingMove = false

      checkActivation()
      whileMoving()

      executePreviews()
      executeFailures()
    }
	else if (Game.instance.networkManager.isServer && isActive)
    {
      setActive(false)
    }
  }

  def checkActivation()
  {
	  if (Game.instance.networkManager.isServer)
    {
      if (isActive && !performingMove)
      {
        if (calculatedField != null)
        {
          performingMove = true
          executeMovement()
          calculatedField = null

			if (Game.instance.networkManager.isServer)
          {
            setActive(false)
          }
        }
        else
        {
          calculateField()
        }
      }
    }
  }

  /**
   * @return True if we started moving.
   */
  def executeMovement(): Boolean =
  {
    /**
     * Check if there is a valid field that has been calculated. If so, we will move this field.
     */
    val movedBlocks = calculatedField filter moveBlock

    if (movedBlocks.size > 0)
    {
      /**
       * Queue an entity move event.
       */
      queueEvent(new DelayedEvent(this, getMoveTime, () =>
      {
        moveEntities
		  ModularForceFieldSystem.packetHandler.sendToAll(new PacketTile(BlockMobilizer.this, PacketBlock.field.id: Integer))

        if (!isTeleport && doAnchor)
        {
          anchor += getDirection
        }
      }))

      val renderBlocks = movedBlocks filter isVisibleToPlayer take Settings.maxForceFieldsPerTick

      if (renderBlocks.size > 0)
      {
        /**
         * If we have more than one block that is visible that was moved, we will tell the client to render it.
         *
         * Packet Params: id, Type1, Type2, Size, the coordinate
         */
        val coordPacketData = renderBlocks.toSeq flatMap (_.toIntList)

        val packet = new PacketTile(this)
		  packet <<< PacketBlock.effect.id

        if (!isTeleport)
        {
          packet <<< 1 <<< 2 <<< coordPacketData.size <<< coordPacketData

          if (getModuleCount(Content.moduleSilence) <= 0)
          {
            worldObj.playSoundEffect(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D, Reference.prefix + "fieldmove", 0.6f, 1 - this.worldObj.rand.nextFloat * 0.1f)
          }

          ModularForceFieldSystem.packetHandler.sendToAllAround(packet, world, position, packetRange)
        }
        else
        {
          packet <<< 2 <<< getMoveTime <<< (getAbsoluteAnchor + 0.5) <<< (getTargetPosition + 0.5) <<< false <<< coordPacketData.size <<< coordPacketData
          moveTime = getMoveTime
          ModularForceFieldSystem.packetHandler.sendToAllAround(packet, world, position, packetRange)
        }
      }

      return true
    }
    else
    {
      markFailMove()
    }

    return false
  }

  def whileMoving()
  {
	  if (Game.instance.networkManager.isServer && performingMove)
    {
		if (removeFortron(getFortronCost, false) >= getFortronCost)
      {
		  removeFortron(getFortronCost, true)

        if (moveTime > 0)
        {
          if (isTeleport)
          {
            if (getModuleCount(Content.moduleSilence) <= 0 && ticks % 10 == 0)
            {
              val moveTime = getMoveTime
              worldObj.playSoundEffect(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D, Reference.prefix + "fieldmove", 1.5f, 0.5f + 0.8f * (moveTime - this.moveTime) / moveTime)
            }

            moveTime -= 1

            if (moveTime <= 0)
            {
              worldObj.playSoundEffect(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D, Reference.prefix + "teleport", 0.6f, 1 - this.worldObj.rand.nextFloat * 0.1f)
            }
          }
        }

        return
      }

      markFailMove()
    }
  }

  def executePreviews()
  {
	  if (Game.instance.networkManager.isServer)
    {
      if (previewMode > 0 && Settings.highGraphics && !performingMove)
      {
        if (calculatedField == null)
          calculateField()

        /**
         * Send preview field packet
         */
        if (ticks % 120 == 0 && calculatedField != null)
        {
          val renderBlocks = getInteriorPoints.view filter isVisibleToPlayer filter (pos => previewMode == 2 || !world.isAirBlock(pos.xi, pos.yi, pos.zi)) take Settings.maxForceFieldsPerTick
          val coordPacketData = renderBlocks.toSeq flatMap (_.toIntList)

          val packet = new PacketTile(this)
			packet <<< PacketBlock.effect.id

          if (isTeleport)
          {
			  var targetPosition: Vector3d = null

            if (getTargetPosition.world == null)
            {
				targetPosition = new Vector3d(getTargetPosition)
            }
            else
            {
              targetPosition = getTargetPosition
            }

            packet <<< 2 <<< 60 <<< (getAbsoluteAnchor + 0.5) <<< (targetPosition + 0.5) <<< true

          }
          else
          {
            packet <<< 1 <<< 1
          }

          packet <<< coordPacketData.size <<< coordPacketData

          ModularForceFieldSystem.packetHandler.sendToAllAround(packet, world, position, packetRange)
          markDirty()
        }
      }
    }
  }

  def executeFailures()
  {
    /**
     * Check if the move failed. If so, we tell the client which positions were the cause of failure.
     */
    if (failedMove)
    {
      /**
       * Stop teleportation field
       */
      moveTime = 0
      performingMove = false

      delayedEvents.clear()
      worldObj.playSoundEffect(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D, Reference.prefix + "powerdown", 0.6f, 1 - this.worldObj.rand.nextFloat * 0.1f)
      val playPoint = position + anchor + 0.5
      worldObj.playSoundEffect(playPoint.x, playPoint.y, playPoint.z, Reference.prefix + "powerdown", 0.6f, 1 - this.worldObj.rand.nextFloat * 0.1f)
		ModularForceFieldSystem.packetHandler.sendToAllAround(new PacketTile(this) <<< PacketBlock.render.id, world, position, packetRange)


      if (failedPositions.size > 0)
      {
        /**
         * Send failure coordinates to client
         */
        val coords = failedPositions.toSeq flatMap (_.toIntList)
		  val packetTile = new PacketTile(this) <<< PacketBlock.effect.id <<< 3 <<< coords.size <<< coords
        ModularForceFieldSystem.packetHandler.sendToAllAround(packetTile, world, position, packetRange)
      }

      failedMove = false
      failedPositions.clear()
    }
  }

	override def generateCalculatedField: mutable.Set[Vector3d] =
  {
	  var moveField: mutable.Set[Vector3d] = null

    if (canMove)
      moveField = getInteriorPoints
    /*else
        markFailMove()*/

    return moveField
  }

  /**
   * Scan target field area to see if we can move this block. Called on a separate thread.
   */
  def canMove: Boolean =
  {
    val mobilizationPoints = getInteriorPoints
    val targetCenterPosition = getTargetPosition

    for (position <- mobilizationPoints)
    {
      if (world.isAirBlock(position.xi, position.yi, position.zi))
      {
        val relativePosition = position - getAbsoluteAnchor
        val targetPosition = (targetCenterPosition + relativePosition)

        if (!canMove(new VectorWorld(this.worldObj, position), targetPosition))
        {
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
  def canMove(position: VectorWorld, target: VectorWorld): Boolean =
  {
    if (Blacklist.mobilizerBlacklist.contains(position.getBlock))
    {
      return false
    }
    val evt = new EventForceMobilize.EventCheckForceManipulate(position.world, position.xi, position.yi, position.zi, target.xi, target.yi, target.zi)
    MinecraftForge.EVENT_BUS.post(evt)

    if (evt.isCanceled)
    {
      return false
    }

    if (!MFFSUtility.hasPermission(worldObj, position, MFFSPermissions.blockAlter, ModularForceFieldSystem.fakeProfile) && !MFFSUtility.hasPermission(target.world, target, MFFSPermissions.blockAlter, ModularForceFieldSystem.fakeProfile))
    {
      return false
    }

    if (target.getTileEntity == this)
    {
      return false
    }
    for (checkPos <- this.getInteriorPoints)
    {
      if (checkPos == target)
      {
        return true
      }
    }

    val targetBlock = target.getBlock
    return target.world.isAirBlock(target.xi, target.yi, target.zi) || (targetBlock.isReplaceable(target.world, target.xi, target.yi, target.zi))
  }

	/**
	 * Gets the position in which the manipulator will try to translate the field into.
	 *
	 * @return A vector of the target position.
	 */
	def getTargetPosition: VectorWorld = {
		if (isTeleport) {
			val cardStack = getLinkCard

			if (cardStack != null) {
				return cardStack.getItem.asInstanceOf[ICoordLink].getLink(cardStack)
			}
		}

		return new VectorWorld(worldObj, getAbsoluteAnchor + getDirection)
	}

	private def isTeleport: Boolean = {
		if (Settings.allowForceManipulatorTeleport) {
			val cardStack = getLinkCard

			if (cardStack != null) {
				return cardStack.getItem.asInstanceOf[ICoordLink].getLink(cardStack) != null
			}
		}
		return false
	}

	def getLinkCard: Item = {
		getInventory().getContainedItems filter (_ != null) find (_.getItem.isInstanceOf[ICoordLink]) match {
			case Some(Item) => return Item
			case _ => return null
		}
	}

	def getAbsoluteAnchor: Vector3d = position.add(this.anchor)

	def isVisibleToPlayer(position: Vector3d): Boolean =
  {
    return (ForgeDirection.VALID_DIRECTIONS count ((dir: ForgeDirection) => (position + dir).getBlock(world).isOpaqueCube)) < 6
  }

	override def write(buf: Packet, id: Int)
  {
    super.write(buf, id)

	  if (id == PacketBlock.description.id)
    {
      buf <<< anchor
      buf <<< previewMode
      buf <<< doAnchor
      buf <<< (if (moveTime > 0) moveTime else getMoveTime)
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
			var time = (20 * this.getTargetPosition.distance(this.getAbsoluteAnchor)).toInt
			if (this.getTargetPosition.world ne this.worldObj) {
				time += 20 * 60
			}
			return time
		}
		return animationTime
	}

	override def read(buf: Packet, id: Int, packetType: PacketType)
  {
    super.read(buf, id, packetType)

    if (world.isRemote)
    {
		if (id == PacketBlock.effect.id)
      {
        buf.readInt() match
        {
          case 1 =>
          {
            /**
             * If we have more than one block that is visible that was moved, we will tell the client to render it.
             *
             * Params: id, Type1, Type2, Size, the coordinate
             */
            val isTeleportPacket = buf.readInt()
            val vecSize = buf.readInt()

			  val hologramRenderPoints = ((0 until vecSize) map (i => buf.readInt().toDouble + 0.5)).toList grouped 3 map (new Vector3d(_))

            /**
             * Movement Rendering
             */
            val direction = getDirection

            isTeleportPacket match
            {
              case 1 => hologramRenderPoints foreach (vector => ModularForceFieldSystem.proxy.renderHologram(world, vector, FieldColor.blue, 30, vector + direction))
              case 2 => hologramRenderPoints foreach (vector => ModularForceFieldSystem.proxy.renderHologram(world, vector, FieldColor.green, 30, vector + direction))
            }
          }
          case 2 =>
          {
            /**
             * Teleportation Rendering
             */
            val animationTime = buf.readInt()
			  val anchorPosition = new Vector3d(buf)
            val targetPosition = new VectorWorld(buf)
            val isPreview = buf.readBoolean()
            val vecSize = buf.readInt()
			  val hologramRenderPoints = ((0 until vecSize) map (i => buf.readInt().toDouble + 0.5)).toList grouped 3 map (new Vector3d(_))
            val color = if (isPreview) FieldColor.blue else FieldColor.green

            hologramRenderPoints foreach (vector =>
            {
              //Render teleport start
              ModularForceFieldSystem.proxy.renderHologramOrbit(this, world, anchorPosition, vector, color, animationTime, 30f)

              if (targetPosition.world != null && targetPosition.world.getChunkProvider.chunkExists(targetPosition.xi, targetPosition.zi))
              {
                //Render teleport end
                val destination = vector - anchorPosition + targetPosition
                ModularForceFieldSystem.proxy.renderHologramOrbit(this, targetPosition.world, targetPosition, destination, color, animationTime, 30f)
              }
            })

            canRenderMove = true
          }
          case 3 =>
          {
            /**
             * Fail hologram rendering
             */
            val vecSize = buf.readInt()
			  val hologramRenderPoints = ((0 until vecSize) map (i => buf.readInt().toDouble + 0.5)).toList grouped 3 map (new Vector3d(_))

            hologramRenderPoints foreach (ModularForceFieldSystem.proxy.renderHologram(world, _, FieldColor.red, 30, null))
          }
        }
      }
		else if (id == PacketBlock.render.id)
      {
        canRenderMove = false
      }
		else if (id == PacketBlock.field.id)
      {
        this.moveEntities
      }
		else if (id == PacketBlock.description.id)
      {
		  anchor = new Vector3d(buf)
        previewMode = buf.readInt()
        doAnchor = buf.readBoolean()
        clientMoveTime = buf.readInt
      }
    }
    else
    {
		if (id == PacketBlock.toggleMode.id)
      {
		  anchor = new Vector3d()
        markDirty()
      }
		else if (id == PacketBlock.toggleMode2.id)
      {
        previewMode = (previewMode + 1) % 3
      }
		else if (id == PacketBlock.toggleMode3.id)
      {
        doAnchor = !doAnchor
      }
    }
  }

  override def markDirty()
  {
    super.markDirty()

    if (world != null)
    {
      clearCache()
      calculateField()
    }
  }

  protected def moveEntities
  {
    val targetLocation = getTargetPosition
    val bounds = getSearchBounds

    if (bounds != null)
    {
      val entities = this.worldObj.getEntitiesWithinAABB(classOf[Entity], bounds)
		entities map (_.asInstanceOf[Entity]) foreach (entity => moveEntity(entity, targetLocation + 0.5 + new Vector3d(entity) - (getAbsoluteAnchor + 0.5)))
    }
  }

  def getSearchBounds: AxisAlignedBB =
  {
    val positiveScale = position + getTranslation + getPositiveScale + 1
    val negativeScale = position + getTranslation - getNegativeScale
    val minScale = positiveScale.min(negativeScale)
    val maxScale = positiveScale.max(negativeScale)
    return new Cuboid(minScale, maxScale).toAABB
  }

	override def getTranslation: Vector3d = super.getTranslation + anchor

  protected def moveEntity(entity: Entity, location: VectorWorld)
  {
    if (entity != null && location != null)
    {
      if (entity.worldObj.provider.dimensionId != location.world.provider.dimensionId)
      {
        entity.travelToDimension(location.world.provider.dimensionId)
      }
      entity.motionX = 0
      entity.motionY = 0
      entity.motionZ = 0

      if (entity.isInstanceOf[EntityPlayerMP])
      {
        entity.asInstanceOf[EntityPlayerMP].playerNetServerHandler.setPlayerLocation(location.x, location.y, location.z, entity.rotationYaw, entity.rotationPitch)
      }
      else
      {
        entity.setPositionAndRotation(location.x, location.y, location.z, entity.rotationYaw, entity.rotationPitch)
      }
    }
  }

  override def doGetFortronCost: Int = round(super.doGetFortronCost + (if (this.anchor != null) this.anchor.magnitude * 1000 else 0)).toInt

	override def isItemValidForSlot(slotID: Int, Item: Item): Boolean =
  {
    if (slotID == 0)
    {
		return Item.getItem.isInstanceOf[ItemCard]
    }
    else if (slotID == modeSlotID)
    {
		return Item.getItem.isInstanceOf[IProjectorMode]
    }

	  return Item.getItem.isInstanceOf[IModule] || Item.getItem.isInstanceOf[ICoordLink]
  }

  /**
   * NBT Methods
   */
  override def readFromNBT(nbt: NBTTagCompound)
  {
    super.readFromNBT(nbt)
	  this.anchor = new Vector3d(nbt.getCompoundTag("anchor"))
    this.previewMode = nbt.getInteger("displayMode")
    this.doAnchor = nbt.getBoolean("doAnchor")
  }

  override def writeToNBT(nbt: NBTTagCompound)
  {
    super.writeToNBT(nbt)

    if (anchor != null)
    {
      nbt.setTag("anchor", anchor.toNBT)
    }

    nbt.setInteger("displayMode", previewMode)
    nbt.setBoolean("doAnchor", doAnchor)
  }

  def canContinueEffect = canRenderMove

  @SideOnly(Side.CLIENT)
  override def renderStatic(renderer: RenderBlocks, pos: Vector3d, pass: Int): Boolean =
  {
    return false
  }

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

  @SideOnly(Side.CLIENT)
  override def renderDynamic(pos: Vector3d, frame: Float, pass: Int)
  {
    RenderForceMobilizer.render(this, pos.x, pos.y, pos.z, frame, isActive, false)
  }

  @SideOnly(Side.CLIENT)
  override def renderInventory(Item: Item)
  {
    RenderForceMobilizer.render(this, -0.5, -0.5, -0.5, 0, true, true)
  }

  /**
   * Called to queue a block move from its position to a target.
   * @param position - The position of the block to be moved.
   * @return True if move is successful.
   */
  protected def moveBlock(position: Vector3d): Boolean =
  {
	  if (Game.instance.networkManager.isServer)
    {
      val relativePosition = position - getAbsoluteAnchor
      val newPosition = getTargetPosition + relativePosition
      val tileEntity = position.getTileEntity(world)

      if (!world.isAirBlock(position.xi, position.yi, position.zi) && tileEntity != this)
      {
        queueEvent(new BlockPreMoveDelayedEvent(this, getMoveTime, new VectorWorld(world, position), newPosition))
        return true
      }
    }

    return false
  }
}