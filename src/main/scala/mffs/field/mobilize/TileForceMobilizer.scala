package mffs.field.mobilize

import cpw.mods.fml.common.network.ByteBufUtils
import cpw.mods.fml.relauncher.{Side, SideOnly}
import io.netty.buffer.ByteBuf
import mffs.base.{TileFieldMatrix, TilePacketType}
import mffs.field.mobilize.event.{BlockPreMoveDelayedEvent, DelayedEvent}
import mffs.render.fx.IEffectController
import mffs.security.access.MFFSPermissions
import mffs.util.MFFSUtility
import mffs.{ModularForceFieldSystem, Reference, Settings}
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.AxisAlignedBB
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.util.ForgeDirection
import resonant.api.mffs.card.ICoordLink
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

  private val failedPositions = Set.empty[Vector3]
  var anchor = new Vector3()

  /**
   * The display mode. 0 = none, 1 = minimal, 2 = maximal.
   */
  var renderMode = 1
  var doAnchor = true
  var clientMoveTime = 0

  /**
   * Marking failures
   */
  var markFailMove = false
  private var markActive = false

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
      if (getMode != null && Settings.enableForceManipulator)
      {
        if (!worldObj.isRemote)
        {
          /**
           * Check if there is a valid field that has been calculated. If so, we will move this field.
           */
          if (calculatedField != null && calculatedField.size > 0 && !isCalculating)
          {
            /**
             * The blocks that were queued to be moved
             */
            //TODO: Try Parallel and views
            val movedBlocks = calculatedField filter moveBlock

            /**
             * Queue an entity move event.
             */
            queueEvent(new DelayedEvent(this, getMoveTime, () =>
            {
              moveEntities
              ModularForceFieldSystem.packetHandler.sendToAll(new PacketTile(TileForceMobilizer.this, TilePacketType.FIELD.id: Integer))
            }))

            val renderBlocks = movedBlocks filter isBlockVisibleByPlayer take Settings.maxForceFieldsPerTick

            if (renderBlocks.size > 0)
            {
              /**
               * If we have more than one block that is visible that was moved, we will tell the client to render it.
               *
               * Params: ID, Type1, Type2, Size, the coordinate
               */
              //TODO: Parallel
              val coordPacketData = (renderBlocks flatMap (_.toIntList)).toSeq

              val packet = new PacketTile(this)
              packet <<< TilePacketType.FXS.id

              if (!isTeleport)
              {
                packet <<< 1
                packet <<< 2
                packet <<< coordPacketData.size
                packet <<< coordPacketData

                if (getModuleCount(ModularForceFieldSystem.Items.moduleSilence) <= 0)
                {
                  worldObj.playSoundEffect(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D, Reference.prefix + "fieldmove", 0.6f, (1 - this.worldObj.rand.nextFloat * 0.1f))
                }
                if (doAnchor)
                {
                  anchor += getDirection
                }
              }
              else
              {
                packet <<< 2
                packet <<< getMoveTime
                packet <<< (getAbsoluteAnchor + 0.5)
                packet <<< (getTargetPosition + 0.5)
                packet <<< false
                packet <<< coordPacketData.size
                packet <<< coordPacketData

                ModularForceFieldSystem.packetHandler.sendToAllAround(packet, worldObj, new Vector3(this), packetRange)
                moveTime = getMoveTime
              }

              ModularForceFieldSystem.packetHandler.sendToAllAround(packet, worldObj, new Vector3(this), packetRange)
            }
            else
            {
              this.markFailMove = true
            }

            calculatedField = null
            markDirty()
          }
        }
      }


      if (this.moveTime > 0)
      {
        if (this.isTeleport && this.requestFortron(this.getFortronCost, true) >= this.getFortronCost)
        {
          if (this.getModuleCount(ModularForceFieldSystem.Items.moduleSilence) <= 0 && this.ticks % 10 == 0)
          {
            val moveTime: Int = this.getMoveTime
            worldObj.playSoundEffect(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D, Reference.prefix + "fieldmove", 1.5f, 0.5f + 0.8f * (moveTime - this.moveTime) / moveTime)
          }

          moveTime -= 1

          if (moveTime <= 0)
          {
            worldObj.playSoundEffect(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D, Reference.prefix + "teleport", 0.6f, (1 - this.worldObj.rand.nextFloat * 0.1f))
          }
        }
        else
        {
          this.markFailMove = true
        }
      }
      if (isActive)
      {
        markActive = true
      }

      if (ticks % 20 == 0 && markActive)
      {
        /**
         * The mobilizer is activated. Calculate the move field and translate it!
         */
        if (moveTime <= 0 && requestFortron(this.getFortronCost, false) > 0)
        {
          if (!worldObj.isRemote)
          {
            requestFortron(getFortronCost, true)
            calculateField()
          }

          moveTime = 0
        }

        if (!worldObj.isRemote)
        {
          setActive(false)
        }

        markActive = false
      }

      if (!worldObj.isRemote)
      {
        if (calculatedField != null)
        {
          calculateField()
        }

        /**
         * Send preview field packet
         */
        if (ticks % 120 == 0 && !isCalculating && Settings.highGraphics && delayedEvents.size <= 0 && renderMode > 0)
        {
          val renderBlocks = getInteriorPoints.view filter isBlockVisibleByPlayer filter (pos => renderMode == 2 || !world.isAirBlock(pos.xi, pos.yi, pos.zi)) take Settings.maxForceFieldsPerTick
          val coordPacketData = (renderBlocks flatMap (_.toIntList)).toSeq

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

            packet <<< 2
            packet <<< 60
            packet <<< (getAbsoluteAnchor + 0.5)
            packet <<< (targetPosition + 0.5)
            packet <<< true

          }
          else
          {
            packet <<< 1
            packet <<< 1
          }

          packet <<< coordPacketData.size
          packet <<< coordPacketData

          ModularForceFieldSystem.packetHandler.sendToAllAround(packet, world, new Vector3(this), packetRange)
        }
      }

      if (markFailMove)
      {
        moveTime = 0
        delayedEvents.clear
        worldObj.playSoundEffect(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D, Reference.prefix + "powerdown", 0.6f, 1 - this.worldObj.rand.nextFloat * 0.1f)
        ModularForceFieldSystem.packetHandler.sendToAllAround(new PacketTile(this) <<< TilePacketType.RENDER.id, world, new Vector3(this), packetRange)

        markFailMove = false

        failedPositions.clear()

        val packetTile = new PacketTile(this)
        packetTile <<< TilePacketType.RENDER.id
        packetTile <<< 3
        packetTile <<< 1
        packetTile <<< (failedPositions flatMap (_.toIntList))
        ModularForceFieldSystem.packetHandler.sendToAllAround(packetTile, world, new Vector3(this), packetRange)
      }
    }
    else if (!worldObj.isRemote && isActive)
    {
      setActive(false)
    }
  }

  override def generateCalculatedField = getMoveField

  def getMoveField: mutable.Set[Vector3] =
  {
    isCalculating = true

    var moveField: mutable.Set[Vector3] = null

    if (canMove)
      moveField = getInteriorPoints
    else
      markFailMove = true

    return moveField
  }

  def isBlockVisibleByPlayer(position: Vector3): Boolean =
  {
    return (ForgeDirection.VALID_DIRECTIONS count ((dir: ForgeDirection) => (position + dir).getBlock(world).isOpaqueCube)) < 6
  }

  override def getPacketData(packetID: Int): List[AnyRef] =
  {
    if (packetID == TilePacketType.DESCRIPTION.id)
    {
      return super.getPacketData(packetID) ++ Seq(anchor, renderMode, doAnchor, if (moveTime > 0) moveTime else getMoveTime).toAnyRef
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

            val hologramRenderPoints = (0 until vecSize) map (i => data.readInt().toDouble + 0.5) grouped 3 map (new Vector3(_))

            /**
             * Movement Rendering
             */
            val direction = getDirection

            isTeleportPacket match
            {
              case 1 => hologramRenderPoints foreach (vector => ModularForceFieldSystem.proxy.renderHologram(world, vector, 1, 1, 1, 30, vector + direction))
              case 2 => hologramRenderPoints foreach (vector => ModularForceFieldSystem.proxy.renderHologram(world, vector, 0, 1, 0, 30, vector + direction))
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
              ModularForceFieldSystem.proxy.renderHologramOrbit(this, world, anchorPosition, vector, color._1, color._2, color._3, animationTime, 30f)

              if (targetPosition.world != null && targetPosition.world.getChunkProvider.chunkExists(targetPosition.xi, targetPosition.zi))
              {
                //Render teleport end
                val destination = vector - anchorPosition + targetPosition
                ModularForceFieldSystem.proxy.renderHologramOrbit(this, targetPosition.world, targetPosition, destination, color._1, color._2, color._3, animationTime, 30f)
              }
            })

            canRenderMove = true
          }
          case 3 =>
          {
            /**
             * Fail hologram rendering
             */
            val nbt = ByteBufUtils.readTag(data)
            val nbtList = nbt.getTagList("list", 10)
            (0 until nbtList.tagCount) map (dir => new Vector3(nbtList.getCompoundTagAt(dir)) + 0.5) foreach (ModularForceFieldSystem.proxy.renderHologram(this.worldObj, _, 1, 0, 0, 30, null))
          }
        }
      }
      else if (packetID == TilePacketType.RENDER.id)
      {
        this.canRenderMove = false
      }
      else if (packetID == TilePacketType.FIELD.id)
      {
        this.moveEntities
      }
      else if (packetID == TilePacketType.DESCRIPTION.id)
      {
        anchor = new Vector3(data)
        renderMode = data.readInt()
        doAnchor = data.readBoolean()
        clientMoveTime = data.readInt
      }
    }
    else
    {
      if (packetID == TilePacketType.TOGGLE_MODE.id)
      {
        this.anchor = new Vector3()
        markDirty()
      }
      else if (packetID == TilePacketType.TOGGLE_MODE_2.id)
      {
        this.renderMode = (this.renderMode + 1) % 3
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
    calculatedField = null
    clearCache()
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
          this.failedPositions.add(position)
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
      return getCard.getItem.asInstanceOf[ICoordLink].getLink(this.getCard)
    }
    return new VectorWorld(worldObj, getAbsoluteAnchor + getDirection)
  }

  /**
   * Gets the movement time required in TICKS.
   *
   * @return The time it takes to teleport (using a link card) to another coordinate OR
   *         ANIMATION_TIME for
   *         default move
   */
  def getMoveTime: Int =
  {
    if (this.isTeleport)
    {
      var time: Int = (20 * this.getTargetPosition.distance(this.getAbsoluteAnchor)).asInstanceOf[Int]
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
    if (this.getCard != null && Settings.allowForceManipulatorTeleport)
    {
      if (getCard.getItem.isInstanceOf[ICoordLink])
      {
        return getCard.getItem.asInstanceOf[ICoordLink].getLink(this.getCard) != null
      }
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
    this.renderMode = nbt.getInteger("displayMode")
    this.doAnchor = nbt.getBoolean("doAnchor")
  }

  override def writeToNBT(nbt: NBTTagCompound)
  {
    super.writeToNBT(nbt)

    if (anchor != null)
    {
      nbt.setTag("anchor", anchor.toNBT)
    }

    nbt.setInteger("displayMode", renderMode)
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