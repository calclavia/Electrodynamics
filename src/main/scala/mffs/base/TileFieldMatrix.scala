package mffs.base

import java.util.{Set => JSet}

import io.netty.buffer.ByteBuf
import mffs.Content
import mffs.field.mobilize.event.{DelayedEvent, IDelayedEventHandler}
import mffs.field.module.ItemModuleArray
import mffs.item.card.ItemCard
import mffs.util.TCache
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.ForgeDirection
import resonant.api.mffs.machine.{IFieldMatrix, IPermissionProvider}
import resonant.api.mffs.modules.{IModule, IProjectorMode}
import resonant.lib.content.prefab.TRotatable
import resonant.lib.network.ByteBufWrapper.ByteBufWrapper
import resonant.lib.network.discriminator.PacketType
import resonant.lib.utility.RotationUtility
import universalelectricity.core.transform.rotation.EulerAngle
import universalelectricity.core.transform.vector.Vector3

import scala.collection.convert.wrapAll._
import scala.collection.mutable
import scala.collection.mutable.Queue
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.util.{Failure, Success}

abstract class TileFieldMatrix extends TileModuleAcceptor with IFieldMatrix with IDelayedEventHandler with TRotatable with IPermissionProvider
{
  protected var calculatedField: mutable.Set[Vector3] = null
  protected final val delayedEvents = new mutable.SynchronizedQueue[DelayedEvent]()

  /**
   * Are the directions on the GUI absolute values?
   */
  var absoluteDirection = false
  protected var isCalculating = false

  protected val modeSlotID = 1

  override def update()
  {
    super.update()

    /**
     * Evaluated queued objects
     */
      delayedEvents foreach (_.update())
      delayedEvents.dequeueAll(_.ticks < 0)
  }

  def clearQueue() = delayedEvents.clear()

  override def write(buf: ByteBuf, id: Int)
  {
    super.write(buf, id)

    if (id == TilePacketType.description.id)
    {
      buf <<< absoluteDirection
    }
  }

  override def read(buf: ByteBuf, id: Int, player: EntityPlayer, packet: PacketType): Boolean =
  {
    super.read(buf, id, player, packet)

    if (world.isRemote)
    {
      if (id == TilePacketType.description.id)
      {
        absoluteDirection = buf.readBoolean()
      }
    }
    else
    {
      if (id == TilePacketType.toggleMode4.id)
      {
        absoluteDirection = !absoluteDirection
      }
    }

    return false
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

    return itemStack.getItem.isInstanceOf[IModule]
  }

  def getModeStack: ItemStack =
  {
    if (this.getStackInSlot(modeSlotID) != null)
    {
      if (this.getStackInSlot(modeSlotID).getItem.isInstanceOf[IProjectorMode])
      {
        return this.getStackInSlot(modeSlotID)
      }
    }
    return null
  }

  def getMode: IProjectorMode =
  {
    if (this.getModeStack != null)
    {
      return this.getModeStack.getItem.asInstanceOf[IProjectorMode]
    }
    return null
  }

  def getSidedModuleCount(module: IModule, directions: ForgeDirection*): Int =
  {
    var actualDirs = directions

    if (directions == null || directions.length > 0)
      actualDirs = ForgeDirection.VALID_DIRECTIONS

    return actualDirs.foldLeft(0)((b, a) => b + getModuleCount(module, getDirectionSlots(a): _*))
  }

  def getTranslation: Vector3 =
  {
    val cacheID = "getTranslation"

    if (hasCache(classOf[Vector3], cacheID)) return getCache(classOf[Vector3], cacheID)

    val direction = getDirection

    var zTranslationNeg = 0
    var zTranslationPos = 0
    var xTranslationNeg = 0
    var xTranslationPos = 0
    var yTranslationPos = 0
    var yTranslationNeg = 0

    if (absoluteDirection)
    {
      zTranslationNeg = getModuleCount(Content.moduleTranslate, getDirectionSlots(ForgeDirection.NORTH): _*)
      zTranslationPos = getModuleCount(Content.moduleTranslate, getDirectionSlots(ForgeDirection.SOUTH): _*)
      xTranslationNeg = getModuleCount(Content.moduleTranslate, getDirectionSlots(ForgeDirection.WEST): _*)
      xTranslationPos = getModuleCount(Content.moduleTranslate, getDirectionSlots(ForgeDirection.EAST): _*)
      yTranslationPos = getModuleCount(Content.moduleTranslate, getDirectionSlots(ForgeDirection.UP): _*)
      yTranslationNeg = getModuleCount(Content.moduleTranslate, getDirectionSlots(ForgeDirection.DOWN): _*)
    }
    else
    {
      zTranslationNeg = getModuleCount(Content.moduleTranslate, getDirectionSlots(RotationUtility.getRelativeSide(direction, ForgeDirection.NORTH)): _*)
      zTranslationPos = getModuleCount(Content.moduleTranslate, getDirectionSlots(RotationUtility.getRelativeSide(direction, ForgeDirection.SOUTH)): _*)
      xTranslationNeg = getModuleCount(Content.moduleTranslate, getDirectionSlots(RotationUtility.getRelativeSide(direction, ForgeDirection.WEST)): _*)
      xTranslationPos = getModuleCount(Content.moduleTranslate, getDirectionSlots(RotationUtility.getRelativeSide(direction, ForgeDirection.EAST)): _*)
      yTranslationPos = getModuleCount(Content.moduleTranslate, getDirectionSlots(ForgeDirection.UP): _*)
      yTranslationNeg = getModuleCount(Content.moduleTranslate, getDirectionSlots(ForgeDirection.DOWN): _*)
    }

    val translation = new Vector3(xTranslationPos - xTranslationNeg, yTranslationPos - yTranslationNeg, zTranslationPos - zTranslationNeg)

    cache(cacheID, translation)

    return translation
  }

  def getPositiveScale: Vector3 =
  {
    val cacheID = "getPositiveScale"

    if (hasCache(classOf[Vector3], cacheID)) return getCache(classOf[Vector3], cacheID)

    var zScalePos = 0
    var xScalePos = 0
    var yScalePos = 0

    if (absoluteDirection)
    {
      zScalePos = getModuleCount(Content.moduleScale, getDirectionSlots(ForgeDirection.SOUTH): _*)
      xScalePos = getModuleCount(Content.moduleScale, getDirectionSlots(ForgeDirection.EAST): _*)
      yScalePos = getModuleCount(Content.moduleScale, getDirectionSlots(ForgeDirection.UP): _*)
    }
    else
    {
      val direction = getDirection

      zScalePos = getModuleCount(Content.moduleScale, getDirectionSlots(RotationUtility.getRelativeSide(direction, ForgeDirection.SOUTH)): _*)
      xScalePos = getModuleCount(Content.moduleScale, getDirectionSlots(RotationUtility.getRelativeSide(direction, ForgeDirection.EAST)): _*)
      yScalePos = getModuleCount(Content.moduleScale, getDirectionSlots(ForgeDirection.UP): _*)
    }

    val omnidirectionalScale = getModuleCount(Content.moduleScale, getModuleSlots: _*)

    zScalePos += omnidirectionalScale
    xScalePos += omnidirectionalScale
    yScalePos += omnidirectionalScale

    val positiveScale = new Vector3(xScalePos, yScalePos, zScalePos)

    cache(cacheID, positiveScale)

    return positiveScale
  }

  def getNegativeScale: Vector3 =
  {
    val cacheID = "getNegativeScale"

    if (hasCache(classOf[Vector3], cacheID)) return getCache(classOf[Vector3], cacheID)

    var zScaleNeg = 0
    var xScaleNeg = 0
    var yScaleNeg = 0

    val direction = getDirection

    if (absoluteDirection)
    {
      zScaleNeg = getModuleCount(Content.moduleScale, getDirectionSlots(ForgeDirection.NORTH): _*)
      xScaleNeg = getModuleCount(Content.moduleScale, getDirectionSlots(ForgeDirection.WEST): _*)
      yScaleNeg = getModuleCount(Content.moduleScale, getDirectionSlots(ForgeDirection.DOWN): _*)
    }
    else
    {
      zScaleNeg = getModuleCount(Content.moduleScale, getDirectionSlots(RotationUtility.getRelativeSide(direction, ForgeDirection.NORTH)): _*)
      xScaleNeg = getModuleCount(Content.moduleScale, getDirectionSlots(RotationUtility.getRelativeSide(direction, ForgeDirection.WEST)): _*)
      yScaleNeg = getModuleCount(Content.moduleScale, getDirectionSlots(ForgeDirection.DOWN): _*)
    }

    val omnidirectionalScale = this.getModuleCount(Content.moduleScale, getModuleSlots: _*)
    zScaleNeg += omnidirectionalScale
    xScaleNeg += omnidirectionalScale
    yScaleNeg += omnidirectionalScale

    val negativeScale = new Vector3(xScaleNeg, yScaleNeg, zScaleNeg)

    cache(cacheID, negativeScale)

    return negativeScale
  }

  def getRotationYaw: Int =
  {
    val cacheID = "getRotationYaw"
    if (hasCache(classOf[Integer], cacheID)) return getCache(classOf[Integer], cacheID)

    var horizontalRotation = 0
    val direction = getDirection

    if (this.absoluteDirection)
    {
      horizontalRotation = getModuleCount(Content.moduleRotate, getDirectionSlots(ForgeDirection.EAST): _*) - getModuleCount(Content.moduleRotate, getDirectionSlots(ForgeDirection.WEST): _*) + getModuleCount(Content.moduleRotate, this.getDirectionSlots(ForgeDirection.SOUTH): _*) - this.getModuleCount(Content.moduleRotate, getDirectionSlots(ForgeDirection.NORTH): _*)
    }
    else
    {
      horizontalRotation = getModuleCount(Content.moduleRotate, getDirectionSlots(RotationUtility.getRelativeSide(direction, ForgeDirection.EAST)): _*) - getModuleCount(Content.moduleRotate, getDirectionSlots(RotationUtility.getRelativeSide(direction, ForgeDirection.WEST)): _*) + this.getModuleCount(Content.moduleRotate, getDirectionSlots(RotationUtility.getRelativeSide(direction, ForgeDirection.SOUTH)): _*) - getModuleCount(Content.moduleRotate, getDirectionSlots(RotationUtility.getRelativeSide(direction, ForgeDirection.NORTH)): _*)
    }

    horizontalRotation *= 2

    cache(cacheID, horizontalRotation)

    return horizontalRotation
  }

  def getRotationPitch: Int =
  {
    val cacheID = "getRotationPitch"

    if (hasCache(classOf[Integer], cacheID)) return getCache(classOf[Integer], cacheID)

    var verticalRotation = getModuleCount(Content.moduleRotate, getDirectionSlots(ForgeDirection.UP): _*) - getModuleCount(Content.moduleRotate, getDirectionSlots(ForgeDirection.DOWN): _*)
    verticalRotation *= 2

    cache(cacheID, verticalRotation)

    return verticalRotation
  }

  /**
   * Calculates the force field
   * @param callBack - Optional callback
   */
  protected def calculateField(callBack: () => Unit = null)
  {
    if (!worldObj.isRemote && !isCalculating)
    {
      if (getMode != null)
      {
        //Clear mode cache
        if (getModeStack.getItem.isInstanceOf[TCache])
          getModeStack.getItem.asInstanceOf[TCache].clearCache()

        isCalculating = true

        Future
        {
          generateCalculatedField
        }.onComplete
        {
          case Success(field) =>
          {
            calculatedField = field
            isCalculating = false

            if (callBack != null)
              callBack.apply()
          }
          case Failure(t) =>
          {
            //println(getClass.getName + ": An error has occurred upon field calculation: " + t.getMessage)
            isCalculating = false
          }
        }
      }
    }
  }

  protected def generateCalculatedField = getExteriorPoints

  /**
   * Gets the exterior points of the field based on the matrix.
   */
  protected def getExteriorPoints: mutable.Set[Vector3] =
  {
    var field = mutable.Set.empty[Vector3]

    if (getModuleCount(Content.moduleInvert) > 0)
      field = getMode.getInteriorPoints(this)
    else
      field = getMode.getExteriorPoints(this)

    getModules() foreach (_.onPreCalculate(this, field))

    val translation = getTranslation
    val rotationYaw = getRotationYaw
    val rotationPitch = getRotationPitch

    val rotation: EulerAngle = new EulerAngle(rotationYaw, rotationPitch)

    val maxHeight = world.getHeight

    field = mutable.Set((field.view.par map (pos => (pos.transform(rotation) + position + translation).round) filter (position => position.yi <= maxHeight && position.yi >= 0)).seq.toSeq: _ *)

    getModules() foreach (_.onPostCalculate(this, field))

    return field
  }

  def getInteriorPoints: JSet[Vector3] =
  {
    val cacheID = "getInteriorPoints"

    if (hasCache(classOf[Set[Vector3]], cacheID)) return getCache(classOf[Set[Vector3]], cacheID)

    if (getModeStack != null && getModeStack.getItem.isInstanceOf[TCache])
    {
      (getModeStack.getItem.asInstanceOf[TCache]).clearCache
    }

    val newField = getMode.getInteriorPoints(this)

    if (getModuleCount(Content.moduleArray) > 0)
    {
      Content.moduleArray.asInstanceOf[ItemModuleArray].onPreCalculateInterior(this, getMode.getExteriorPoints(this), newField)
    }

    val translation = getTranslation
    val rotationYaw = getRotationYaw
    val rotationPitch = getRotationPitch
    val rotation = new EulerAngle(rotationYaw, rotationPitch, 0)
    val maxHeight = world.getHeight

    val field = mutable.Set((newField.view.par map (pos => (pos.transform(rotation) + position + translation).round) filter (position => position.yi <= maxHeight && position.yi >= 0)).seq.toSeq: _ *)

    cache(cacheID, field)
    return field
  }

  val _getModuleSlots = (14 until 25).toArray

  def getModuleSlots: Array[Int] = _getModuleSlots

  override def getDirectionSlots(direction: ForgeDirection): Array[Int] =
  {
    direction match
    {
      case ForgeDirection.UP =>
        return Array(10, 11)
      case ForgeDirection.DOWN =>
        return Array(12, 13)
      case ForgeDirection.SOUTH =>
        return Array(2, 3)
      case ForgeDirection.NORTH =>
        return Array(4, 5)
      case ForgeDirection.WEST =>
        return Array(6, 7)
      case ForgeDirection.EAST =>
        return Array(8, 9)
      case _ =>
        return Array[Int]()
    }
  }

  def getCalculatedField: JSet[Vector3] =
  {
    return if (calculatedField != null) calculatedField else mutable.Set.empty[Vector3]
  }

  def queueEvent(evt: DelayedEvent)
  {
    delayedEvents += evt
  }

  /**
   * NBT Methods
   */
  override def readFromNBT(nbt: NBTTagCompound)
  {
    super.readFromNBT(nbt)
    absoluteDirection = nbt.getBoolean("isAbsolute")
  }

  override def writeToNBT(nbt: NBTTagCompound)
  {
    super.writeToNBT(nbt)
    nbt.setBoolean("isAbsolute", absoluteDirection)
  }

}