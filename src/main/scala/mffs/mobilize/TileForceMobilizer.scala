package mffs.mobilize

import cpw.mods.fml.common.network.ByteBufUtils
import cpw.mods.fml.relauncher.{Side, SideOnly}
import io.netty.buffer.ByteBuf
import mffs.base.{TileFieldInteraction, TilePacketType}
import mffs.field.RenderElectromagneticProjector
import mffs.field.thread.ManipulatorCalculationThread
import mffs.item.card.ItemCard
import mffs.mobilize.event.{BlockPreMoveDelayedEvent, DelayedEvent}
import mffs.render.fx.IEffectController
import mffs.security.access.MFFSPermissions
import mffs.util.MFFSUtility
import mffs.{ModularForceFieldSystem, Reference, Settings}
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.item.ItemStack
import net.minecraft.nbt.{NBTTagCompound, NBTTagList}
import net.minecraft.util.AxisAlignedBB
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.util.ForgeDirection
import resonant.api.mffs.card.ICoordLink
import resonant.api.mffs.modules.{IModule, IProjectorMode}
import resonant.api.mffs.{Blacklist, EventForceManipulate}
import resonant.lib.network.PacketTile
import universalelectricity.core.transform.region.Cuboid
import universalelectricity.core.transform.vector.{Vector3, VectorWorld}

import scala.collection.convert.wrapAll._
import scala.math._

class TileForceMobilizer extends TileFieldInteraction with IEffectController
{
  val packetRange = 60
  val animationTime = 20

  private val failedPositions = Set.empty[Vector3]
  var anchor = new Vector3()
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

  rotationMask = 63
  maxSlots = 3 + 18

  override def update()
  {
    super.update()

    if (this.getMode != null && Settings.enableForceManipulator)
    {
      if (!this.worldObj.isRemote)
      {
        if (this.manipulationVectors != null && this.manipulationVectors.size > 0 && !this.isCalculatingManipulation)
        {
          val nbt: NBTTagCompound = new NBTTagCompound
          val nbtList: NBTTagList = new NBTTagList
          var i: Int = 0
          for (position <- this.manipulationVectors)
          {
            if (this.moveBlock(position) && this.isBlockVisibleByPlayer(position) && i < Settings.maxForceFieldsPerTick)
            {
              nbtList.appendTag(position.toNBT)
              i += 1
            }
          }
          if (i > 0)
          {
            queueEvent(new DelayedEvent(this, getMoveTime, () => ({
              moveEntities
              ModularForceFieldSystem.packetHandler.sendToAll(new PacketTile(TileForceMobilizer.this, TilePacketType.FIELD.id: Integer))
            })))

            nbt.setByte("type", 2.asInstanceOf[Byte])
            nbt.setTag("list", nbtList)

            if (!this.isTeleport)
            {
              ModularForceFieldSystem.packetHandler.sendToAllAround(new PacketTile(this, TilePacketType.FXS.id: Integer, 1.toByte: java.lang.Byte, nbt), worldObj, new Vector3(this), packetRange)

              if (this.getModuleCount(ModularForceFieldSystem.Items.moduleSilence) <= 0)
              {
                this.worldObj.playSoundEffect(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D, Reference.PREFIX + "fieldmove", 0.6f, (1 - this.worldObj.rand.nextFloat * 0.1f))
              }
              if (this.doAnchor)
              {
                anchor += this.getDirection
              }
            }
            else
            {
              ModularForceFieldSystem.packetHandler.sendToAllAround(new PacketTile(this, TilePacketType.FXS.id: Integer, 2.toByte: java.lang.Byte, getMoveTime: Integer, (getAbsoluteAnchor + 0.5).toNBT, (getTargetPosition + 0.5).toNBT, false: java.lang.Boolean, nbt), worldObj, new Vector3(this), packetRange)
              this.moveTime = this.getMoveTime
            }
          }
          else
          {
            this.markFailMove = true
          }
          this.manipulationVectors = null
          markDirty()
        }
      }
      if (this.moveTime > 0)
      {
        if (this.isTeleport && this.requestFortron(this.getFortronCost, true) >= this.getFortronCost)
        {
          if (this.getModuleCount(ModularForceFieldSystem.Items.moduleSilence) <= 0 && this.ticks % 10 == 0)
          {
            val moveTime: Int = this.getMoveTime
            this.worldObj.playSoundEffect(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D, Reference.PREFIX + "fieldmove", 1.5f, 0.5f + 0.8f * (moveTime - this.moveTime) / moveTime)
          }

          moveTime -= 1

          if (moveTime <= 0)
          {
            this.worldObj.playSoundEffect(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D, Reference.PREFIX + "teleport", 0.6f, (1 - this.worldObj.rand.nextFloat * 0.1f))
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
        if (this.ticks % 120 == 0 && !this.isCalculating && Settings.highGraphics && this.delayedEvents.size <= 0 && this.displayMode > 0)
        {
          val nbt: NBTTagCompound = new NBTTagCompound
          val nbtList: NBTTagList = new NBTTagList
          var i: Int = 0
          for (position <- this.getInteriorPoints)
          {
            if (this.isBlockVisibleByPlayer(position) && (this.displayMode == 2 || !this.worldObj.isAirBlock(position.xi, position.yi, position.zi) && i < Settings.maxForceFieldsPerTick))
            {
              i += 1
              nbtList.appendTag(position.toNBT)
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

            ModularForceFieldSystem.packetHandler.sendToAllAround(new PacketTile(this, TilePacketType.FXS.id: Integer, 2.toByte: java.lang.Byte, 60: Integer, getAbsoluteAnchor + 0.5, targetPosition + 0.5, true: java.lang.Boolean, nbt), worldObj, new Vector3(this), packetRange)
          }
          else
          {
            ModularForceFieldSystem.packetHandler.sendToAllAround(new PacketTile(this, TilePacketType.FXS.id: Integer, 1.toByte: java.lang.Byte), worldObj, new Vector3(this), packetRange)
          }
        }
      }

      if (this.markFailMove)
      {
        moveTime = 0
        delayedEvents.clear
        worldObj.playSoundEffect(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D, Reference.PREFIX + "powerdown", 0.6f, (1 - this.worldObj.rand.nextFloat * 0.1f))
        ModularForceFieldSystem.packetHandler.sendToAllAround(new PacketTile(this, TilePacketType.RENDER.id: Integer), this.worldObj, new Vector3(this), packetRange)

        markFailMove = false
        val nbt = new NBTTagCompound
        val nbtList = new NBTTagList

        failedPositions foreach (pos => nbtList.appendTag(pos.toNBT))

        nbt.setByte("type", 1.toByte)
        nbt.setTag("list", nbtList)
        this.failedPositions.clear
        ModularForceFieldSystem.packetHandler.sendToAllAround(new PacketTile(this, TilePacketType.FXS.id: Integer, 3.toByte: java.lang.Byte, nbt), this.worldObj, new Vector3(this), packetRange)
      }
    }
    else if (!worldObj.isRemote && isActive)
    {
      setActive(false)
    }
  }

  def isBlockVisibleByPlayer(position: Vector3): Boolean =
  {
    return (ForgeDirection.VALID_DIRECTIONS count ((dir: ForgeDirection) => (position + dir).getBlock(world).isOpaqueCube)) < 6
  }

  override def getPacketData(packetID: Int): List[AnyRef] =
  {
    return super.getPacketData(packetID) :+ ((if (moveTime > 0) moveTime else getMoveTime): Integer)
  }

  override def onReceivePacket(packetID: Int, dataStream: ByteBuf)
  {
    super.onReceivePacket(packetID, dataStream)

    if (this.worldObj.isRemote)
    {
      if (packetID == TilePacketType.FXS.id)
      {
        dataStream.readByte match
        {
          case 1 =>
          {
            /**
             * Movement Rendering
             */
            val nbt = ByteBufUtils.readTag(dataStream)
            val nbtList = nbt.getTagList("list", 10)

            val hologramRenderPoints = (0 until nbtList.tagCount) map (dir => new Vector3(nbtList.getCompoundTagAt(dir)) + 0.5)
            val direction = getDirection

            nbt.getByte("type") match
            {
              case 1 => hologramRenderPoints foreach (vector => ModularForceFieldSystem.proxy.renderHologram(this.worldObj, vector, 1, 1, 1, 30, vector + direction))
              case 2 => hologramRenderPoints foreach (vector => ModularForceFieldSystem.proxy.renderHologram(this.worldObj, vector, 0, 1, 0, 30, vector + direction))
            }
          }
          case 2 =>
          {
            /**
             * Teleportation Rendering
             */
            val animationTime = dataStream.readInt
            val anchorPosition = new Vector3(dataStream)
            val targetPosition = new VectorWorld(dataStream)
            val isPreview = dataStream.readBoolean
            val nbt = ByteBufUtils.readTag(dataStream)
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
            val nbt = ByteBufUtils.readTag(dataStream)
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
        this.clientMoveTime = dataStream.readInt
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
        this.displayMode = (this.displayMode + 1) % 3
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
    this.isCalculated = false
  }

  /**
   * Scan target area to see if we can mvoe. Called on a separate thread.
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
        val targetPosition = (targetCenterPosition + relativePosition).asInstanceOf[VectorWorld]

        if (!canMove(new VectorWorld(this.worldObj, position), targetPosition))
        {
          this.failedPositions.add(position)
          return false
        }
      }
    }
    return true
  }

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

  protected def moveBlock(position: Vector3): Boolean =
  {
    if (!this.worldObj.isRemote)
    {
      val relativePosition = position.clone.subtract(getAbsoluteAnchor)
      val newPosition = getTargetPosition + relativePosition
      val tileEntity = position.getTileEntity(world)

      if (!worldObj.isAirBlock(position.xi, position.yi, position.zi) && tileEntity != this)
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

    if (anchor != null)
    {
      nbt.setTag("anchor", anchor.toNBT)
    }

    nbt.setInteger("displayMode", displayMode)
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
  override def renderDynamic(pos: Vector3, frame: Float, pass: Int)
  {
    RenderForceMobilizer.render(this, pos.x, pos.y, pos.z, frame)
  }
}