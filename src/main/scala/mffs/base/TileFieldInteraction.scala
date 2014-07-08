package mffs.base

import java.util.Set

import io.netty.buffer.ByteBuf
import mffs.ModularForceFieldSystem
import mffs.field.module.ItemModuleArray
import mffs.field.thread.ProjectorCalculationThread
import mffs.mobilize.event.{DelayedEvent, IDelayedEventHandler}
import mffs.util.TCache
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.ForgeDirection
import resonant.api.mffs.IFieldInteraction
import resonant.api.mffs.modules.{IModule, IProjectorMode}
import resonant.lib.utility.RotationUtility
import universalelectricity.core.transform.rotation.Rotation
import universalelectricity.core.transform.vector.Vector3

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.collection.mutable.Queue

abstract class TileFieldInteraction extends TileModuleAcceptor with IFieldInteraction with IDelayedEventHandler
{
  protected val calculatedField = mutable.Set.empty[Vector3]
  protected final val delayedEvents = new Queue[DelayedEvent]()

  /**
   * Are the directions on the GUI absolute values?
   */
  var absoluteDirection = false
  protected var isCalculating = false
  protected var isCalculated = false

  protected val moduleSlotID = 2

  override def update()
  {
    super.update()

    /**
     * Evaluated queued objects
     */
    delayedEvents.clone foreach (_.update())
    delayedEvents dequeueAll (_.ticks <= 0)
  }

  override def onReceivePacket(packetID: Int, dataStream: ByteBuf)
  {
    super.onReceivePacket(packetID, dataStream)

    if (packetID == TilePacketType.TOGGLE_MODE_4.id && !world.isRemote)
    {
      this.absoluteDirection = !this.absoluteDirection
    }
  }

  protected def calculateForceField(callBack: () => Unit = null)
  {
    if (!worldObj.isRemote && !isCalculating)
    {
      if (getMode != null)
      {
        if (getModeStack.getItem.isInstanceOf[TCache])
          getModeStack.getItem.asInstanceOf[TCache].clearCache()

        calculatedField.clear()

        new ProjectorCalculationThread(this, callBack).start()

        isCalculating = true
      }
    }
  }

  protected def calculateForceField()
  {
    this.calculateForceField(null)
  }

  def getModeStack: ItemStack =
  {
    if (this.getStackInSlot(moduleSlotID) != null)
    {
      if (this.getStackInSlot(moduleSlotID).getItem.isInstanceOf[IProjectorMode])
      {
        return this.getStackInSlot(moduleSlotID)
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

  def getModuleSlots: Array[Int] = Array(15, 16, 17, 18, 19, 20)

  def getTranslation: Vector3 =
  {
    val cacheID = "getTranslation"

    if (hasCache(classOf[Vector3], cacheID)) return getCache(classOf[Vector3], cacheID)

    val direction = getDirection
    /*
    //TODO: Check why this exists
    if (direction == ForgeDirection.UP || direction == ForgeDirection.DOWN)
    {
      direction = ForgeDirection.NORTH
    }
    */

    var zTranslationNeg = 0
    var zTranslationPos = 0
    var xTranslationNeg = 0
    var xTranslationPos = 0
    var yTranslationPos = 0
    var yTranslationNeg = 0

    if (this.absoluteDirection)
    {
      zTranslationNeg = getModuleCount(ModularForceFieldSystem.Items.moduleTranslate, getDirectionSlots(ForgeDirection.NORTH): _*)
      zTranslationPos = getModuleCount(ModularForceFieldSystem.Items.moduleTranslate, getDirectionSlots(ForgeDirection.SOUTH): _*)
      xTranslationNeg = getModuleCount(ModularForceFieldSystem.Items.moduleTranslate, getDirectionSlots(ForgeDirection.WEST): _*)
      xTranslationPos = getModuleCount(ModularForceFieldSystem.Items.moduleTranslate, getDirectionSlots(ForgeDirection.EAST): _*)
      yTranslationPos = getModuleCount(ModularForceFieldSystem.Items.moduleTranslate, getDirectionSlots(ForgeDirection.UP): _*)
      yTranslationNeg = getModuleCount(ModularForceFieldSystem.Items.moduleTranslate, getDirectionSlots(ForgeDirection.DOWN): _*)
    }
    else
    {
      zTranslationNeg = getModuleCount(ModularForceFieldSystem.Items.moduleTranslate, getDirectionSlots(RotationUtility.rotateSide(direction, ForgeDirection.NORTH)): _*)
      zTranslationPos = getModuleCount(ModularForceFieldSystem.Items.moduleTranslate, getDirectionSlots(RotationUtility.rotateSide(direction, ForgeDirection.SOUTH)): _*)
      xTranslationNeg = getModuleCount(ModularForceFieldSystem.Items.moduleTranslate, getDirectionSlots(RotationUtility.rotateSide(direction, ForgeDirection.WEST)): _*)
      xTranslationPos = getModuleCount(ModularForceFieldSystem.Items.moduleTranslate, getDirectionSlots(RotationUtility.rotateSide(direction, ForgeDirection.EAST)): _*)
      yTranslationPos = getModuleCount(ModularForceFieldSystem.Items.moduleTranslate, getDirectionSlots(ForgeDirection.UP): _*)
      yTranslationNeg = getModuleCount(ModularForceFieldSystem.Items.moduleTranslate, getDirectionSlots(ForgeDirection.DOWN): _*)
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
      zScalePos = getModuleCount(ModularForceFieldSystem.Items.moduleScale, getDirectionSlots(ForgeDirection.SOUTH): _*)
      xScalePos = getModuleCount(ModularForceFieldSystem.Items.moduleScale, getDirectionSlots(ForgeDirection.EAST): _*)
      yScalePos = getModuleCount(ModularForceFieldSystem.Items.moduleScale, getDirectionSlots(ForgeDirection.UP): _*)
    }
    else
    {
      val direction = getDirection
      /*
      //TODO: Check why this exists
      if (direction == ForgeDirection.UP || direction == ForgeDirection.DOWN)
      {
        direction = ForgeDirection.NORTH
      }
      */
      zScalePos = getModuleCount(ModularForceFieldSystem.Items.moduleScale, getDirectionSlots(RotationUtility.rotateSide(direction, ForgeDirection.SOUTH)): _*)
      xScalePos = getModuleCount(ModularForceFieldSystem.Items.moduleScale, getDirectionSlots(RotationUtility.rotateSide(direction, ForgeDirection.EAST)): _*)
      yScalePos = getModuleCount(ModularForceFieldSystem.Items.moduleScale, getDirectionSlots(ForgeDirection.UP): _*)
    }

    val omnidirectionalScale = getModuleCount(ModularForceFieldSystem.Items.moduleScale, getModuleSlots: _*)

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
    /*
    if (direction == ForgeDirection.UP || direction == ForgeDirection.DOWN)
    {
      direction = ForgeDirection.NORTH
    }*/

    if (absoluteDirection)
    {
      zScaleNeg = getModuleCount(ModularForceFieldSystem.Items.moduleScale, getDirectionSlots(ForgeDirection.NORTH): _*)
      xScaleNeg = getModuleCount(ModularForceFieldSystem.Items.moduleScale, getDirectionSlots(ForgeDirection.WEST): _*)
      yScaleNeg = getModuleCount(ModularForceFieldSystem.Items.moduleScale, getDirectionSlots(ForgeDirection.DOWN): _*)
    }
    else
    {
      zScaleNeg = getModuleCount(ModularForceFieldSystem.Items.moduleScale, getDirectionSlots(RotationUtility.rotateSide(direction, ForgeDirection.NORTH)): _*)
      xScaleNeg = getModuleCount(ModularForceFieldSystem.Items.moduleScale, getDirectionSlots(RotationUtility.rotateSide(direction, ForgeDirection.WEST)): _*)
      yScaleNeg = getModuleCount(ModularForceFieldSystem.Items.moduleScale, getDirectionSlots(ForgeDirection.DOWN): _*)
    }

    val omnidirectionalScale = this.getModuleCount(ModularForceFieldSystem.Items.moduleScale, getModuleSlots: _*)
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
      horizontalRotation = getModuleCount(ModularForceFieldSystem.Items.moduleRotate, getDirectionSlots(ForgeDirection.EAST): _*) - getModuleCount(ModularForceFieldSystem.Items.moduleRotate, getDirectionSlots(ForgeDirection.WEST): _*) + getModuleCount(ModularForceFieldSystem.Items.moduleRotate, this.getDirectionSlots(ForgeDirection.SOUTH): _*) - this.getModuleCount(ModularForceFieldSystem.Items.moduleRotate, getDirectionSlots(ForgeDirection.NORTH): _*)
    }
    else
    {
      horizontalRotation = getModuleCount(ModularForceFieldSystem.Items.moduleRotate, getDirectionSlots(RotationUtility.rotateSide(direction, ForgeDirection.EAST)): _*) - getModuleCount(ModularForceFieldSystem.Items.moduleRotate, getDirectionSlots(RotationUtility.rotateSide(direction, ForgeDirection.WEST)): _*) + this.getModuleCount(ModularForceFieldSystem.Items.moduleRotate, getDirectionSlots(RotationUtility.rotateSide(direction, ForgeDirection.SOUTH)): _*) - getModuleCount(ModularForceFieldSystem.Items.moduleRotate, getDirectionSlots(RotationUtility.rotateSide(direction, ForgeDirection.NORTH)): _*)
    }

    horizontalRotation *= 2

    cache(cacheID, horizontalRotation)

    return horizontalRotation
  }

  def getRotationPitch: Int =
  {
    val cacheID = "getRotationPitch"

    if (hasCache(classOf[Integer], cacheID)) return getCache(classOf[Integer], cacheID)

    var verticalRotation = getModuleCount(ModularForceFieldSystem.Items.moduleRotate, getDirectionSlots(ForgeDirection.UP): _*) - getModuleCount(ModularForceFieldSystem.Items.moduleRotate, getDirectionSlots(ForgeDirection.DOWN): _*)
    verticalRotation *= 2

    cache(cacheID, verticalRotation)

    return verticalRotation
  }

  @SuppressWarnings(Array("unchecked"))
  def getInteriorPoints: Set[Vector3] =
  {
    val cacheID = "getInteriorPoints"

    if (hasCache(classOf[Set[Vector3]], cacheID)) return getCache(classOf[Set[Vector3]], cacheID)

    if (getModeStack != null && getModeStack.getItem.isInstanceOf[TCache])
    {
      (getModeStack.getItem.asInstanceOf[TCache]).clearCache
    }

    val newField = getMode.getInteriorPoints(this)

    if (getModuleCount(ModularForceFieldSystem.Items.moduleArray) > 0)
    {
      (ModularForceFieldSystem.Items.moduleArray.asInstanceOf[ItemModuleArray]).onPreCalculateInterior(this, getMode.getExteriorPoints(this), newField)
    }

    val translation = getTranslation
    val rotationYaw = getRotationYaw
    val rotationPitch = getRotationPitch

    val returnField = newField map (_.clone.apply(new Rotation(rotationYaw, rotationPitch, 0)) + new Vector3(this) + translation)

    cache(cacheID, returnField)

    return returnField
  }

  override def getDirectionSlots(direction: ForgeDirection): Array[Int] =
  {
    direction match
    {
      case ForgeDirection.UP =>
        return Array[Int](3, 11)
      case ForgeDirection.DOWN =>
        return Array[Int](6, 14)
      case ForgeDirection.NORTH =>
        return Array[Int](7, 9)
      case ForgeDirection.SOUTH =>
        return Array[Int](8, 10)
      case ForgeDirection.WEST =>
        return Array[Int](4, 5)
      case ForgeDirection.EAST =>
        return Array[Int](12, 13)
      case _ =>
        return Array[Int]()
    }
  }

  def setCalculating(bool: Boolean)
  {
    isCalculating = bool
  }

  def setCalculated(bool: Boolean)
  {
    isCalculated = bool
  }

  def getCalculatedField: Set[Vector3] =
  {
    return calculatedField
  }

  def queueEvent(evt: DelayedEvent)
  {
    delayedEvents.add(evt)
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