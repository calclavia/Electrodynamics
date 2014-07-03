package mffs.mobilize

import java.util.{ArrayList, LinkedHashSet, List, Set}

import com.google.common.io.ByteArrayDataInput
import mffs.base.TileFieldInteraction
import mffs.item.card.ItemCard
import mffs.mobilize.event.{BlockPreMoveDelayedEvent, DelayedEvent}
import mffs.field.thread.ManipulatorCalculationThread
import mffs.render.fx.IEffectController
import mffs.security.access.MFFSPermissions
import mffs.{MFFSHelper, ModularForceFieldSystem, Settings}
import net.minecraft.block.Block
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.item.ItemStack
import net.minecraft.nbt.{NBTTagCompound, NBTTagList}
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.AxisAlignedBB
import net.minecraftforge.common.MinecraftForge
import resonant.api.mffs.Blacklist
import resonant.api.mffs.card.ICoordLink
import resonant.api.mffs.modules.{IModule, IProjectorMode}
import universalelectricity.core.transform.vector.Vector3

class TileForceMobilizer extends TileFieldInteraction with IEffectController
{
  val PACKET_DISTANCE: Int = 60
  val ANIMATION_TIME: Int = 20

  rotationMask = 63

  override def updateEntity
  {
    super.updateEntity
    if (this.anchor == null)
    {
      this.anchor = new Vector3
    }
    if (this.getMode != null && Settings.ENABLE_MANIPULATOR)
    {
      if (!this.worldObj.isRemote)
      {
        if (this.manipulationVectors != null && this.manipulationVectors.size > 0 && !this.isCalculatingManipulation)
        {
          val nbt: NBTTagCompound = new NBTTagCompound
          val nbtList: NBTTagList = new NBTTagList
          var i: Int = 0
          import scala.collection.JavaConversions._
          for (position <- this.manipulationVectors)
          {
            if (this.moveBlock(position) && this.isBlockVisibleByPlayer(position) && i < Settings.maxForceFieldsPerTick)
            {
              nbtList.appendTag(position.writeToNBT(new NBTTagCompound))
              i += 1
            }
          }
          if (i > 0)
          {
            queueEvent(new DelayedEvent((this, getMoveTime))
            {
              protected def onEvent
              {
                moveEntities
                PacketHandler.sendPacketToClients(ModularForceFieldSystem.PACKET_TILE.getPacket(TileForceMobilizer.this, TilePacketType.FIELD.ordinal))
              }
            })
            nbt.setByte("type", 2.asInstanceOf[Byte])
            nbt.setTag("list", nbtList)
            if (!this.isTeleport)
            {
              PacketHandler.sendPacketToClients(ModularForceFieldSystem.PACKET_TILE.getPacket(this, TilePacketType.FXS.ordinal, 1.asInstanceOf[Byte], nbt), worldObj, new Vector3(this), PACKET_DISTANCE)
              if (this.getModuleCount(ModularForceFieldSystem.itemModuleSilence) <= 0)
              {
                this.worldObj.playSoundEffect(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D, ModularForceFieldSystem.PREFIX + "fieldmove", 0.6f, (1 - this.worldObj.rand.nextFloat * 0.1f))
              }
              if (this.doAnchor)
              {
                this.anchor = this.anchor.translate(this.getDirection)
              }
            }
            else
            {
              PacketHandler.sendPacketToClients(ModularForceFieldSystem.PACKET_TILE.getPacket(this, TilePacketType.FXS.ordinal, 2.asInstanceOf[Byte], this.getMoveTime, this.getAbsoluteAnchor.translate(0.5), this.getTargetPosition.translate(0.5).writeToNBT(new NBTTagCompound), false, nbt), worldObj, new Vector3(this), PACKET_DISTANCE)
              this.moveTime = this.getMoveTime
            }
          }
          else
          {
            this.markFailMove = true
          }
          this.manipulationVectors = null
          this.onInventoryChanged
        }
      }
      if (this.moveTime > 0)
      {
        if (this.isTeleport && this.requestFortron(this.getFortronCost, true) >= this.getFortronCost)
        {
          if (this.getModuleCount(ModularForceFieldSystem.itemModuleSilence) <= 0 && this.ticks % 10 eq 0)
          {
            val moveTime: Int = this.getMoveTime
            this.worldObj.playSoundEffect(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D, ModularForceFieldSystem.PREFIX + "fieldmove", 1.5f, 0.5f + 0.8f * (moveTime - this.moveTime) / moveTime)
          }
          if (({
            this.moveTime -= 1;
            this.moveTime
          }) == 0)
          {
            this.worldObj.playSoundEffect(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D, ModularForceFieldSystem.PREFIX + "teleport", 0.6f, (1 - this.worldObj.rand.nextFloat * 0.1f))
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
      if (ticks % 20 eq 0 && markActive)
      {
        if (moveTime <= 0 && requestFortron(this.getFortronCost, false) > 0)
        {
          if (!worldObj.isRemote)
          {
            requestFortron(getFortronCost, true)
            (new ManipulatorCalculationThread(this)).start
          }
          moveTime = 0
        }
        if (!worldObj.isRemote)
        {
          setActive(false)
        }
        markActive = false
      }
      if (!this.worldObj.isRemote)
      {
        if (!this.isCalculated)
        {
          this.calculateForceField
        }
        if (this.ticks % 120 eq 0 && !this.isCalculating && Settings.HIGH_GRAPHICS && this.delayedEvents.size <= 0 && this.displayMode > 0)
        {
          val nbt: NBTTagCompound = new NBTTagCompound
          val nbtList: NBTTagList = new NBTTagList
          var i: Int = 0
          import scala.collection.JavaConversions._
          for (position <- this.getInteriorPoints)
          {
            if (this.isBlockVisibleByPlayer(position) && (this.displayMode == 2 || !this.worldObj.isAirBlock(position.xi, position.yi, position.zi) && i < Settings.MAX_FORCE_FIELDS_PER_TICK))
            {
              i += 1
              nbtList.appendTag(new Vector3(position).writeToNBT(new NBTTagCompound))
            }
          }
          nbt.setByte("type", 1.asInstanceOf[Byte])
          nbt.setTag("list", nbtList)
          if (this.isTeleport)
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
            PacketHandler.sendPacketToClients(ModularForceFieldSystem.PACKET_TILE.getPacket(this, TilePacketType.FXS.ordinal, 2.asInstanceOf[Byte], 60, getAbsoluteAnchor.translate(0.5), targetPosition.translate(0.5).writeToNBT(new NBTTagCompound), true, nbt), worldObj, new Vector3(this), PACKET_DISTANCE)
          }
          else
          {
            PacketHandler.sendPacketToClients(ModularForceFieldSystem.PACKET_TILE.getPacket(this, TilePacketType.FXS.ordinal, 1.asInstanceOf[Byte], nbt), worldObj, new Vector3(this), PACKET_DISTANCE)
          }
        }
      }
      if (this.markFailMove)
      {
        this.moveTime = 0
        delayedEvents.clear
        this.worldObj.playSoundEffect(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D, ModularForceFieldSystem.PREFIX + "powerdown", 0.6f, (1 - this.worldObj.rand.nextFloat * 0.1f))
        PacketHandler.sendPacketToClients(ModularForceFieldSystem.PACKET_TILE.getPacket(this, TilePacketType.RENDER.ordinal), this.worldObj, new Vector3(this), PACKET_DISTANCE)
        this.markFailMove = false
        val nbt: NBTTagCompound = new NBTTagCompound
        val nbtList: NBTTagList = new NBTTagList
        import scala.collection.JavaConversions._
        for (position <- this.failedPositions)
        {
          nbtList.appendTag(position.writeToNBT(new NBTTagCompound))
        }
        nbt.setByte("type", 1.asInstanceOf[Byte])
        nbt.setTag("list", nbtList)
        this.failedPositions.clear
        PacketHandler.sendPacketToClients(ModularForceFieldSystem.PACKET_TILE.getPacket(this, TilePacketType.FXS.ordinal, 3.asInstanceOf[Byte], nbt), this.worldObj, new Vector3(this), PACKET_DISTANCE)
      }
    }
    else if (!worldObj.isRemote && isActive)
    {
      setActive(false)
    }
  }

  def isBlockVisibleByPlayer(position: Vector3): Boolean =
  {
    var i: Int = 0
    for (direction <- ForgeDirection.VALID_DIRECTIONS)
    {
      val checkPos: Vector3 = position.clone.translate(direction)
      val blockID: Int = checkPos.getBlockID(this.worldObj)
      if (blockID > 0)
      {
        if (Block.blocksList(blockID) != null)
        {
          if (Block.blocksList(blockID).isOpaqueCube)
          {
            i += 1
          }
        }
      }
    }
    return !(i >= 6)
  }

  override def getPacketData(packetID: Int): ArrayList[_] =
  {
    val objects: ArrayList[_] = super.getPacketData(packetID)
    objects.add(if (this.moveTime > 0) this.moveTime else this.getMoveTime)
    return objects
  }

  override def onReceivePacket(packetID: Int, dataStream: ByteArrayDataInput)
  {
    super.onReceivePacket(packetID, dataStream)
    if (this.worldObj.isRemote)
    {
      if (packetID == TilePacketType.FXS.ordinal)
      {
        dataStream.readByte match
        {
          case 1 =>
          {
            val nbt: NBTTagCompound = PacketHandler.readNBTTagCompound(dataStream)
            val `type`: Byte = nbt.getByte("type")
            val nbtList: NBTTagList = nbt.getTag("list").asInstanceOf[NBTTagList]
            {
              var i: Int = 0
              while (i < nbtList.tagCount)
              {
                {
                  val vector: Vector3 = new Vector3(nbtList.tagAt(i).asInstanceOf[NBTTagCompound]).translate(0.5)
                  if (`type` == 1)
                  {
                    ModularForceFieldSystem.proxy.renderHologram(this.worldObj, vector, 1, 1, 1, 30, vector.clone.translate(this.getDirection))
                  }
                  else if (`type` == 2)
                  {
                    ModularForceFieldSystem.proxy.renderHologram(this.worldObj, vector, 0, 1, 0, 30, vector.clone.translate(this.getDirection))
                  }
                }
                ({
                  i += 1;
                  i - 1
                })
              }
            }
            break //todo: break is not supported
          }
          case 2 =>
          {
            val animationTime: Int = dataStream.readInt
            val anchorPosition: Vector3 = new Vector3(dataStream.readDouble, dataStream.readDouble, dataStream.readDouble)
            val targetPosition: Nothing = new Nothing(PacketHandler.readNBTTagCompound(dataStream))
            val isPreview: Boolean = dataStream.readBoolean
            val nbt: NBTTagCompound = PacketHandler.readNBTTagCompound(dataStream)
            val nbtList: NBTTagList = nbt.getTag("list").asInstanceOf[NBTTagList]
            {
              var i: Int = 0
              while (i < nbtList.tagCount)
              {
                {
                  val vector: Vector3 = new Vector3(nbtList.tagAt(i).asInstanceOf[NBTTagCompound]).translate(0.5)
                  if (isPreview)
                  {
                    ModularForceFieldSystem.proxy.renderHologramOrbit(this, this.worldObj, anchorPosition, vector, 1, 1, 1, animationTime, 30f)
                  }
                  else
                  {
                    ModularForceFieldSystem.proxy.renderHologramOrbit(this, this.worldObj, anchorPosition, vector, 0.1f, 1, 0, animationTime, 30f)
                  }
                  if (targetPosition.world != null && targetPosition.world.getChunkProvider.chunkExists(targetPosition.xi, targetPosition.zi))
                  {
                    val destination: Vector3 = vector.clone.difference(anchorPosition).add(targetPosition)
                    if (isPreview)
                    {
                      ModularForceFieldSystem.proxy.renderHologramOrbit(this, targetPosition.world, targetPosition, destination, 1, 1, 1, animationTime, 30f)
                    }
                    else
                    {
                      ModularForceFieldSystem.proxy.renderHologramOrbit(this, targetPosition.world, targetPosition, destination, 0.1f, 1, 0, animationTime, 30f)
                    }
                  }
                }
                ({
                  i += 1;
                  i - 1
                })
              }
            }
            this.canRenderMove = true
            break //todo: break is not supported
          }
          case 3 =>
          {
            val nbt: NBTTagCompound = PacketHandler.readNBTTagCompound(dataStream)
            val nbtList: NBTTagList = nbt.getTag("list").asInstanceOf[NBTTagList]
            {
              var i: Int = 0
              while (i < nbtList.tagCount)
              {
                {
                  val vector: Vector3 = new Vector3(nbtList.tagAt(i).asInstanceOf[NBTTagCompound]).translate(0.5)
                  ModularForceFieldSystem.proxy.renderHologram(this.worldObj, vector, 1, 0, 0, 30, null)
                }
                ({
                  i += 1;
                  i - 1
                })
              }
            }
            break //todo: break is not supported
          }
        }
      }
      else if (packetID == TilePacketType.RENDER.ordinal)
      {
        this.canRenderMove = false
      }
      else if (packetID == TilePacketType.FIELD.ordinal)
      {
        this.moveEntities
      }
      else if (packetID == TilePacketType.DESCRIPTION.ordinal)
      {
        this.clientMoveTime = dataStream.readInt
      }
    }
    else
    {
      if (packetID == TilePacketType.TOGGLE_MODE.ordinal)
      {
        this.anchor = null
        this.onInventoryChanged
      }
      else if (packetID == TilePacketType.TOGGLE_MODE_2.ordinal)
      {
        this.displayMode = (this.displayMode + 1) % 3
      }
      else if (packetID == TilePacketType.TOGGLE_MODE_3.ordinal)
      {
        this.doAnchor = !this.doAnchor
      }
    }
  }

  override def doGetFortronCost: Int =
  {
    return Math.round(super.doGetFortronCost + (if (this.anchor != null) this.anchor.getMagnitude * 1000 else 0)).asInstanceOf[Int]
  }

  override def onInventoryChanged
  {
    super.onInventoryChanged
    this.isCalculated = false
  }

  /**
   * Scan target area...
   */
  protected def canMove: Boolean =
  {
    val mobilizationPoints: Set[Vector3] = this.getInteriorPoints
    val targetCenterPosition: Nothing = this.getTargetPosition
    import scala.collection.JavaConversions._
    for (position <- mobilizationPoints)
    {
      if (!this.worldObj.isAirBlock(position.xi, position.yi, position.zi))
      {
        val relativePosition: Vector3 = position.clone.subtract(this.getAbsoluteAnchor)
        val targetPosition: Nothing = targetCenterPosition.clone.add(relativePosition).asInstanceOf[Nothing]
        if (!this.canMove(new Nothing(this.worldObj, position), targetPosition))
        {
          this.failedPositions.add(position)
          return false
        }
      }
    }
    return true
  }

  def canMove(position: Nothing, target: Nothing): Boolean =
  {
    if (Blacklist.mobilizerBlacklist.contains(position.getBlockID))
    {
      return false
    }
    val evt: EventForceManipulate.EventCheckForceManipulate = new EventForceManipulate.EventCheckForceManipulate(position.world, position.xi, position.yi, position.zi, target.xi, target.yi, target.zi)
    MinecraftForge.EVENT_BUS.post(evt)
    if (evt.isCanceled)
    {
      return false
    }
    val tileEntity: TileEntity = position.getTileEntity
    if (this.getBiometricIdentifier != null)
    {
      if (!MFFSHelper.hasPermission(this.worldObj, position, MFFSPermissions.blockAlter, this.getBiometricIdentifier.getOwner) && !MFFSHelper.hasPermission(target.world, target, MFFSPermissions.blockAlter, this.getBiometricIdentifier.getOwner))
      {
        return false
      }
    }
    else if (!MFFSHelper.hasPermission(this.worldObj, position, MFFSPermissions.blockAlter, "") || !MFFSHelper.hasPermission(target.world, target, MFFSPermissions.blockAlter, ""))
    {
      return false
    }
    if (target.getTileEntity eq this)
    {
      return false
    }
    import scala.collection.JavaConversions._
    for (checkPos <- this.getInteriorPoints)
    {
      if (checkPos == target)
      {
        return true
      }
    }
    val targetBlockID: Int = target.getBlockID
    if (!(target.world.isAirBlock(target.xi, target.yi, target.zi) || (targetBlockID > 0 && (Block.blocksList(targetBlockID).isBlockReplaceable(target.world, target.xi, target.yi, target.zi)))))
    {
      return false
    }
    return true
  }

  protected def moveBlock(position: Vector3): Boolean =
  {
    if (!this.worldObj.isRemote)
    {
      val relativePosition: Vector3 = position.clone.subtract(this.getAbsoluteAnchor)
      val newPosition: Nothing = this.getTargetPosition.clone.add(relativePosition).asInstanceOf[Nothing]
      val tileEntity: TileEntity = position.getTileEntity(this.worldObj)
      val blockID: Int = position.getBlockID(this.worldObj)
      if (!this.worldObj.isAirBlock(position.xi, position.yi, position.zi) && tileEntity ne this)
      {
        queueEvent(new BlockPreMoveDelayedEvent(this, getMoveTime, this.worldObj, position, newPosition))
        return true
      }
    }
    return false
  }

  def getSearchAxisAlignedBB: AxisAlignedBB =
  {
    val positiveScale: Vector3 = new Vector3(this).translate(this.getTranslation).add(this.getPositiveScale).add(1)
    val negativeScale: Vector3 = new Vector3(this).translate(this.getTranslation).subtract(this.getNegativeScale)
    val minScale: Vector3 = positiveScale.min(negativeScale)
    val maxScale: Vector3 = positiveScale.max(negativeScale)
    return AxisAlignedBB.getAABBPool.getAABB(minScale.xi, minScale.yi, minScale.zi, maxScale.xi, maxScale.yi, maxScale.zi)
  }

  /**
   * Gets the position in which the manipulator will try to translate the field into.
   *
   * @return A vector of the target position.
   */
  def getTargetPosition: Nothing =
  {
    if (this.isTeleport)
    {
      return (this.getCard.getItem.asInstanceOf[ICoordLink]).getLink(this.getCard)
    }
    return new Nothing(this.worldObj, this.getAbsoluteAnchor).clone.translate(this.getDirection).asInstanceOf[Nothing]
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
    return ANIMATION_TIME
  }

  private def isTeleport: Boolean =
  {
    if (this.getCard != null && Settings.allowForceManipulatorTeleport)
    {
      if (this.getCard.getItem.isInstanceOf[ICoordLink])
      {
        return (this.getCard.getItem.asInstanceOf[ICoordLink]).getLink(this.getCard) != null
      }
    }
    return false
  }

  def getAbsoluteAnchor: Vector3 =
  {
    if (this.anchor != null)
    {
      return new Vector3(this).add(this.anchor)
    }
    return new Vector3(this)
  }

  protected def moveEntities
  {
    val targetLocation: Nothing = this.getTargetPosition
    val axisalignedbb: AxisAlignedBB = this.getSearchAxisAlignedBB
    if (axisalignedbb != null)
    {
      val entities: List[Entity] = this.worldObj.getEntitiesWithinAABBExcludingEntity(null, axisalignedbb)
      import scala.collection.JavaConversions._
      for (entity <- entities)
      {
        val relativePosition: Vector3 = new Nothing(entity).clone.subtract(this.getAbsoluteAnchor.translate(0.5))
        val newLocation: Nothing = targetLocation.clone.translate(0.5).add(relativePosition).asInstanceOf[Nothing]
        moveEntity(entity, newLocation)
      }
    }
  }

  protected def moveEntity(entity: Entity, location: Nothing)
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

  override def isItemValidForSlot(slotID: Int, itemStack: ItemStack): Boolean =
  {
    if (slotID == 0 || slotID == 1)
    {
      return itemStack.getItem.isInstanceOf[ItemCard]
    }
    else if (slotID == moduleSlotID)
    {
      return itemStack.getItem.isInstanceOf[IProjectorMode]
    }
    else if (slotID >= 15)
    {
      return true
    }
    return itemStack.getItem.isInstanceOf[IModule]
  }

  /**
   * NBT Methods
   */
  override def readFromNBT(nbt: NBTTagCompound)
  {
    super.readFromNBT(nbt)
    this.anchor = new Vector3(nbt.getCompoundTag("anchor"))
    this.displayMode = nbt.getInteger("displayMode")
    this.doAnchor = nbt.getBoolean("doAnchor")
  }

  override def writeToNBT(nbt: NBTTagCompound)
  {
    super.writeToNBT(nbt)
    if (this.anchor != null)
    {
      nbt.setCompoundTag("anchor", this.anchor.writeToNBT(new NBTTagCompound))
    }
    nbt.setInteger("displayMode", this.displayMode)
    nbt.setBoolean("doAnchor", this.doAnchor)
  }

  override def getTranslation: Vector3 =
  {
    return super.getTranslation.clone.add(this.anchor)
  }

  override def getSizeInventory: Int =
  {
    return 3 + 18
  }

  def getMethodNames: Array[String] =
  {
    return Array[String]("isActivate", "setActivate", "resetAnchor", "canMove")
  }

  def callMethod(computer: Nothing, context: Nothing, method: Int, arguments: Array[AnyRef]): Array[AnyRef] =
  {
    method match
    {
      case 2 =>
      {
        this.anchor = null
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

  def canContinueEffect: Boolean =
  {
    return this.canRenderMove
  }

  private final val failedPositions: Set[Vector3] = new LinkedHashSet[Vector3]
  var anchor: Vector3 = null
  /**
   * The display mode. 0 = none, 1 = minimal, 2 = maximal.
   */
  var displayMode: Int = 1
  var isCalculatingManipulation: Boolean = false
  var manipulationVectors: Set[Vector3] = null
  var doAnchor: Boolean = true
  var clientMoveTime: Int = 0
  /**
   * Marking failures
   */
  var markFailMove: Boolean = false
  private var markActive: Boolean = false
  /**
   * Used ONLY for teleporting.
   */
  private var moveTime: Int = 0
  private var canRenderMove: Boolean = true
}