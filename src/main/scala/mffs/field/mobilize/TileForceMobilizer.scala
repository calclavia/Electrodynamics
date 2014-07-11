package mffs.field.mobilize

import cpw.mods.fml.common.network.ByteBufUtils
import cpw.mods.fml.relauncher.{Side, SideOnly}
import io.netty.buffer.ByteBuf
import mffs.base.{TileFieldMatrix, TilePacketType}
import mffs.field.mobilize.event.{BlockPreMoveDelayedEvent, DelayedEvent}
import mffs.item.card.ItemCard
import mffs.render.FieldColor
import mffs.render.fx.IEffectController
import mffs.security.access.MFFSPermissions
import mffs.util.MFFSUtility
import mffs.{Content, ModularForceFieldSystem, Reference, Settings}
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.AxisAlignedBB
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.util.ForgeDirection
import resonant.api.mffs.card.ICoordLink
import resonant.api.mffs.modules.{IModule, IProjectorMode}
import resonant.api.mffs.{Blacklist, EventForceManipulate}
import resonant.lib.network.discriminator.PacketTile
import resonant.lib.wrapper.WrapVararg._
import universalelectricity.core.transform.region.Cuboid
import universalelectricity.core.transform.vector.{Vector3, VectorWorld}

import scala.collection.convert.wrapAll._
import scala.collection.mutable
import scala.math._

class TileForceMobilizer extends TileFieldMatrix with IEffectController
{
  val packetRange = 60
  val animationTime = 20

  val failedPositions = mutable.Set.empty[Vector3]
  var anchor = new Vector3()

  /**
   * The display mode. 0 = none, 1 = minimal, 2 = maximal.
   */
  var previewMode = 1
  var doAnchor = true
  var clientMoveTime = 0

  /**
   * Marking failures
   */
  private var failedMove = false

  def markFailMove() = failedMove = true

  var performingMove = false

  /**
   * Used ONLY for teleporting.
   */
  private var moveTime = 0
  private var canRenderMove = true

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
    else if (!worldObj.isRemote && isActive)
    {
      setActive(false)
    }
  }

  def checkActivation()
  {
    if (!world.isRemote)
    {
      if (isActive && !performingMove)
      {
        if (calculatedField != null)
        {
          performingMove = true
          executeMovement()
          calculatedField = null

          if (!worldObj.isRemote)
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
        ModularForceFieldSystem.packetHandler.sendToAll(new PacketTile(TileForceMobilizer.this, TilePacketType.FIELD.id: Integer))

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
         * Packet Params: ID, Type1, Type2, Size, the coordinate
         */
        val coordPacketData = renderBlocks.toSeq flatMap (_.toIntList)

        val packet = new PacketTile(this)
        packet <<< TilePacketType.FXS.id

        if (!isTeleport)
        {
          packet <<< 1 <<< 2 <<< coordPacketData.size <<< coordPacketData

          if (getModuleCount(Content.moduleSilence) <= 0)
          {
            worldObj.playSoundEffect(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D, Reference.prefix + "fieldmove", 0.6f, 1 - this.worldObj.rand.nextFloat * 0.1f)
          }
        }
        else
        {
          packet <<< 2 <<< getMoveTime <<< (getAbsoluteAnchor + 0.5) <<< (getTargetPosition + 0.5) <<< false <<< coordPacketData.size <<< coordPacketData
          moveTime = getMoveTime
        }

        ModularForceFieldSystem.packetHandler.sendToAllAround(packet, world, new Vector3(this), packetRange)

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
    if (!worldObj.isRemote && performingMove)
    {
      if (requestFortron(getFortronCost, false) >= getFortronCost)
      {
        requestFortron(getFortronCost, true)

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
    if (!world.isRemote)
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
          packet <<< TilePacketType.FXS.id

          if (isTeleport)
          {
            var targetPosition: Vector3 = null

            if (getTargetPosition.world == null)
            {
              targetPosition = new Vector3(getTargetPosition)
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

          ModularForceFieldSystem.packetHandler.sendToAllAround(packet, world, new Vector3(this), packetRange)
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
      ModularForceFieldSystem.packetHandler.sendToAllAround(new PacketTile(this) <<< TilePacketType.RENDER.id, world, new Vector3(this), packetRange)


      if (failedPositions.size > 0)
      {
        /**
         * Send failure coordinates to client
         */
        val coords = failedPositions.toSeq flatMap (_.toIntList)
        val packetTile = new PacketTile(this) <<< TilePacketType.FXS.id <<< 3 <<< coords.size <<< coords
        ModularForceFieldSystem.packetHandler.sendToAllAround(packetTile, world, new Vector3(this), packetRange)
      }

      failedMove = false
      failedPositions.clear()
    }
  }

  override def generateCalculatedField: mutable.Set[Vector3] =
  {
    var moveField: mutable.Set[Vector3] = null

    if (canMove)
      moveField = getInteriorPoints
    /*else
        markFailMove()*/

    return moveField
  }

  def isVisibleToPlayer(position: Vector3): Boolean =
  {
    return (ForgeDirection.VALID_DIRECTIONS count ((dir: ForgeDirection) => (position + dir).getBlock(world).isOpaqueCube)) < 6
  }

  override def getPacketData(packetID: Int): List[AnyRef] =
  {
    if (packetID == TilePacketType.DESCRIPTION.id)
    {
      return super.getPacketData(packetID) ++ Seq(anchor, previewMode, doAnchor, if (moveTime > 0) moveTime else getMoveTime).toAnyRef
    }

    return super.getPacketData(packetID)
  }

  override def onReceivePacket(packetID: Int, data: ByteBuf)
  {
    super.onReceivePacket(packetID, data)

    if (world.isRemote)
    {
      if (packetID == TilePacketType.FXS.id)
      {
        data.readInt() match
        {
          case 1 =>
          {
            /**
             * If we have more than one block that is visible that was moved, we will tell the client to render it.
             *
             * Params: ID, Type1, Type2, Size, the coordinate
             */
            val isTeleportPacket = data.readInt()
            val vecSize = data.readInt()

            val hologramRenderPoints = ((0 until vecSize) map (i => data.readInt().toDouble + 0.5)).toList grouped 3 map (new Vector3(_))

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
            //TODO: Fix packet
            val animationTime = data.readInt
            val anchorPosition = new Vector3(data)
            val targetPosition = new VectorWorld(data)
            val isPreview = data.readBoolean
            val nbt = ByteBufUtils.readTag(data)
            val nbtList = nbt.getTagList("list", 10)

            val hologramRenderPoints = (0 until nbtList.tagCount) map (dir => new Vector3(nbtList.getCompoundTagAt(dir)) + 0.5)
            val color = if (isPreview) (1f, 1f, 1f) else (0.1f, 1f, 0f)

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
            val vecSize = data.readInt()
            val hologramRenderPoints = ((0 until vecSize) map (i => data.readInt().toDouble + 0.5)).toList grouped 3 map (new Vector3(_))

            hologramRenderPoints foreach (ModularForceFieldSystem.proxy.renderHologram(world, _, FieldColor.red, 30, null))
          }
        }
      }
      else if (packetID == TilePacketType.RENDER.id)
      {
        canRenderMove = false
      }
      else if (packetID == TilePacketType.FIELD.id)
      {
        this.moveEntities
      }
      else if (packetID == TilePacketType.DESCRIPTION.id)
      {
        anchor = new Vector3(data)
        previewMode = data.readInt()
        doAnchor = data.readBoolean()
        clientMoveTime = data.readInt
      }
    }
    else
    {
      if (packetID == TilePacketType.TOGGLE_MODE.id)
      {
        anchor = new Vector3()
        markDirty()
      }
      else if (packetID == TilePacketType.TOGGLE_MODE_2.id)
      {
        this.previewMode = (this.previewMode + 1) % 3
      }
      else if (packetID == TilePacketType.TOGGLE_MODE_3.id)
      {
        this.doAnchor = !this.doAnchor
      }
    }
  }

  override def doGetFortronCost: Int = round(super.doGetFortronCost + (if (this.anchor != null) this.anchor.magnitude * 1000 else 0)).toInt

  override def markDirty()
  {
    super.markDirty()

    if (world != null)
    {
      clearCache()
      calculateField()
    }
  }

  override def isItemValidForSlot(slotID: Int, itemStack: ItemStack): Boolean =
  {
    if (slotID == 0)
    {
      return itemStack.getItem.isInstanceOf[ItemCard]
    }
    else if (slotID == modeSlotID)
    {
      return itemStack.getItem.isInstanceOf[IProjectorMode]
    }

    return itemStack.getItem.isInstanceOf[IModule] || itemStack.getItem.isInstanceOf[ICoordLink]
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
    val evt = new EventForceManipulate.EventCheckForceManipulate(position.world, position.xi, position.yi, position.zi, target.xi, target.yi, target.zi)
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
   * Called to queue a block move from its position to a target.
   * @param position - The position of the block to be moved.
   * @return True if move is successful.
   */
  protected def moveBlock(position: Vector3): Boolean =
  {
    if (!world.isRemote)
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

  def getSearchBounds: AxisAlignedBB =
  {
    val positiveScale = position + getTranslation + getPositiveScale + 1
    val negativeScale = position + getTranslation - getNegativeScale
    val minScale = positiveScale.min(negativeScale)
    val maxScale = positiveScale.max(negativeScale)
    return new Cuboid(minScale, maxScale).toAABB
  }

  /**
   * Gets the position in which the manipulator will try to translate the field into.
   *
   * @return A vector of the target position.
   */
  def getTargetPosition: VectorWorld =
  {
    if (isTeleport)
    {
      val cardStack = getLinkCard

      if (cardStack != null)
        return cardStack.getItem.asInstanceOf[ICoordLink].getLink(cardStack)
    }

    return new VectorWorld(worldObj, getAbsoluteAnchor + getDirection)
  }

  def getLinkCard: ItemStack =
  {
    getInventory().getContainedItems filter (_ != null) find (_.getItem.isInstanceOf[ICoordLink]) match
    {
      case Some(itemStack) => return itemStack
      case _ => return null
    }
  }

  /**
   * Gets the movement time required in TICKS.
   *
   * @return The time it takes to teleport (using a link card) to another coordinate OR
   *         ANIMATION_TIME for default move.
   */
  def getMoveTime: Int =
  {
    if (isTeleport)
    {
      var time = (20 * this.getTargetPosition.distance(this.getAbsoluteAnchor)).toInt
      if (this.getTargetPosition.world ne this.worldObj)
      {
        time += 20 * 60
      }
      return time
    }
    return animationTime
  }

  private def isTeleport: Boolean =
  {
    if (Settings.allowForceManipulatorTeleport)
    {
      val cardStack = getLinkCard

      if (cardStack != null)
        return cardStack.getItem.asInstanceOf[ICoordLink].getLink(cardStack) != null
    }
    return false
  }

  def getAbsoluteAnchor: Vector3 = new Vector3(this).add(this.anchor)

  protected def moveEntities
  {
    val targetLocation = getTargetPosition
    val bounds = getSearchBounds

    if (bounds != null)
    {
      val entities = this.worldObj.getEntitiesWithinAABB(classOf[Entity], bounds)
      entities map (_.asInstanceOf[Entity]) foreach (entity => moveEntity(entity, targetLocation + 0.5 + new Vector3(entity) - (getAbsoluteAnchor + 0.5)))
    }
  }

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
        (entity.asInstanceOf[EntityPlayerMP]).playerNetServerHandler.setPlayerLocation(location.x, location.y, location.z, entity.rotationYaw, entity.rotationPitch)
      }
      else
      {
        entity.setPositionAndRotation(location.x, location.y, location.z, entity.rotationYaw, entity.rotationPitch)
      }
    }
  }

  /**
   * NBT Methods
   */
  override def readFromNBT(nbt: NBTTagCompound)
  {
    super.readFromNBT(nbt)
    this.anchor = new Vector3(nbt.getCompoundTag("anchor"))
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

  override def getTranslation: Vector3 = super.getTranslation + anchor

  def canContinueEffect = canRenderMove

  /*
   def getMethodNames: Array[String] =
  {
    return Array[String]("isActivate", "setActivate", "resetAnchor", "canMove")
  }

  def callMethod(computer: Vector3, context: Vector3, method: Int, arguments: Array[AnyRef]): Array[AnyRef] =
  {
    method match
    {
      case 2 =>
      {
        this.anchor = new Vector3
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
  override def renderStatic(renderer: RenderBlocks, pos: Vector3, pass: Int): Boolean =
  {
    return false
  }

  @SideOnly(Side.CLIENT)
  override def renderDynamic(pos: Vector3, frame: Float, pass: Int)
  {
    RenderForceMobilizer.render(this, pos.x, pos.y, pos.z, frame, isActive)
  }

  @SideOnly(Side.CLIENT)
  override def renderInventory(itemStack: ItemStack)
  {
    RenderForceMobilizer.render(this, -0.5, -0.5, -0.5, 0, true)
  }
}