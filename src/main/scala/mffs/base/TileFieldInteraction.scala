package mffs.base

import java.util.Set

import com.google.common.io.ByteArrayDataInput
import mffs.field.module.ItemModuleArray
import mffs.field.thread.ProjectorCalculationThread
import mffs.mobilize.event.{DelayedEvent, IDelayedEventHandler}
import mffs.{ModularForceFieldSystem, Settings}
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.ForgeDirection
import resonant.api.mffs.modules.{IModule, IProjectorMode}
import resonant.api.mffs.{ICache, IFieldInteraction}
import universalelectricity.core.transform.vector.Vector3

import scala.collection.mutable.{HashSet, Queue}

abstract class TileFieldInteraction extends TileModuleAcceptor with IFieldInteraction with IDelayedEventHandler
{
  protected final val calculatedField = new HashSet[Vector3]()
  protected final val delayedEvents = new Queue[DelayedEvent]()

  /**
   * Are the directions on the GUI absolute values?
   */
  var isAbsolute: Boolean = false
  protected var isCalculating: Boolean = false
  protected var isCalculated: Boolean = false

  protected val moduleSlotID: Int = 2

  override def update()
  {
    super.update()

    /**
     * Evaluated queued objects
     */
    delayedEvents.clone foreach (_.update())
    delayedEvents dequeueAll (_.ticks <= 0)
  }

  override def onReceivePacket(packetID: Int, dataStream: ByteArrayDataInput)
  {
    super.onReceivePacket(packetID, dataStream)

    if (packetID == TilePacketType.TOGGLE_MODE_4.id && !world.isRemote)
    {
      this.isAbsolute = !this.isAbsolute
    }
  }

  protected def calculateForceField(callBack: () => Unit = null)
  {
    if (!this.worldObj.isRemote && !this.isCalculating)
    {
      if (this.getMode != null)
      {
        if (getModeStack.getItem.isInstanceOf[ICache])
        {
          (getModeStack.getItem.asInstanceOf[ICache]).clearCache()
        }
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

    return actualDirs.foldLeft(0)((b, a) => b + getModuleCount(module, getSlotsBasedOnDirection(a): _*))
  }

  def getModuleSlots: Array[Int] = Array(15, 16, 17, 18, 19, 20)

  def getTranslation: Vector3 =
  {
    val cacheID: String = "getTranslation"

    if (Settings.useCache)
    {
      if (this.cache.containsKey(cacheID))
      {
        if (this.cache.get(cacheID).isInstanceOf[Vector3])
        {
          return this.cache.get(cacheID).asInstanceOf[Vector3]
        }
      }
    }
    var direction = getDirection

    if (direction == ForgeDirection.UP || direction == ForgeDirection.DOWN)
    {
      direction = ForgeDirection.NORTH
    }

    var zTranslationNeg = 0
    var zTranslationPos = 0
    var xTranslationNeg = 0
    var xTranslationPos = 0
    var yTranslationPos = 0
    var yTranslationNeg = 0

    if (this.isAbsolute)
    {
      zTranslationNeg = getModuleCount(ModularForceFieldSystem.itemModuleTranslate, getSlotsBasedOnDirection(ForgeDirection.NORTH): _*)
      zTranslationPos = getModuleCount(ModularForceFieldSystem.itemModuleTranslate, getSlotsBasedOnDirection(ForgeDirection.SOUTH): _*)
      xTranslationNeg = getModuleCount(ModularForceFieldSystem.itemModuleTranslate, getSlotsBasedOnDirection(ForgeDirection.WEST): _*)
      xTranslationPos = getModuleCount(ModularForceFieldSystem.itemModuleTranslate, getSlotsBasedOnDirection(ForgeDirection.EAST): _*)
      yTranslationPos = getModuleCount(ModularForceFieldSystem.itemModuleTranslate, getSlotsBasedOnDirection(ForgeDirection.UP): _*)
      yTranslationNeg = getModuleCount(ModularForceFieldSystem.itemModuleTranslate, getSlotsBasedOnDirection(ForgeDirection.DOWN): _*)
    }
    else
    {
      zTranslationNeg = getModuleCount(ModularForceFieldSystem.itemModuleTranslate, getSlotsBasedOnDirection(VectorHelper.getOrientationFromSide(direction, ForgeDirection.NORTH)): _*)
      zTranslationPos = getModuleCount(ModularForceFieldSystem.itemModuleTranslate, getSlotsBasedOnDirection(VectorHelper.getOrientationFromSide(direction, ForgeDirection.SOUTH)): _*)
      xTranslationNeg = getModuleCount(ModularForceFieldSystem.itemModuleTranslate, getSlotsBasedOnDirection(VectorHelper.getOrientationFromSide(direction, ForgeDirection.WEST)): _*)
      xTranslationPos = getModuleCount(ModularForceFieldSystem.itemModuleTranslate, getSlotsBasedOnDirection(VectorHelper.getOrientationFromSide(direction, ForgeDirection.EAST)): _*)
      yTranslationPos = getModuleCount(ModularForceFieldSystem.itemModuleTranslate, getSlotsBasedOnDirection(ForgeDirection.UP): _*)
      yTranslationNeg = getModuleCount(ModularForceFieldSystem.itemModuleTranslate, getSlotsBasedOnDirection(ForgeDirection.DOWN): _*)
    }

    val translation = new Vector3(xTranslationPos - xTranslationNeg, yTranslationPos - yTranslationNeg, zTranslationPos - zTranslationNeg)

    if (Settings.useCache)
    {
      this.cache.put(cacheID, translation)
    }

    return translation
  }

  def getPositiveScale: Vector3 =
  {
    val cacheID: String = "getPositiveScale"

    if (Settings.useCache)
    {
      if (this.cache.containsKey(cacheID))
      {
        if (this.cache.get(cacheID).isInstanceOf[Vector3])
        {
          return this.cache.get(cacheID).asInstanceOf[Vector3]
        }
      }
    }

    var zScalePos = 0
    var xScalePos = 0
    var yScalePos = 0

    if (this.isAbsolute)
    {
      zScalePos = this.getModuleCount(ModularForceFieldSystem.itemModuleScale, this.getSlotsBasedOnDirection(ForgeDirection.SOUTH))
      xScalePos = this.getModuleCount(ModularForceFieldSystem.itemModuleScale, this.getSlotsBasedOnDirection(ForgeDirection.EAST))
      yScalePos = this.getModuleCount(ModularForceFieldSystem.itemModuleScale, this.getSlotsBasedOnDirection(ForgeDirection.UP))
    }
    else
    {
      var direction: Nothing = this.getDirection
      if (direction == ForgeDirection.UP || direction == ForgeDirection.DOWN)
      {
        direction = ForgeDirection.NORTH
      }
      zScalePos = this.getModuleCount(ModularForceFieldSystem.itemModuleScale, this.getSlotsBasedOnDirection(VectorHelper.getOrientationFromSide(direction, ForgeDirection.SOUTH)))
      xScalePos = this.getModuleCount(ModularForceFieldSystem.itemModuleScale, this.getSlotsBasedOnDirection(VectorHelper.getOrientationFromSide(direction, ForgeDirection.EAST)))
      yScalePos = this.getModuleCount(ModularForceFieldSystem.itemModuleScale, this.getSlotsBasedOnDirection(ForgeDirection.UP))
    }

    val omnidirectionalScale: Int = this.getModuleCount(ModularForceFieldSystem.itemModuleScale, this.getModuleSlots)
    zScalePos += omnidirectionalScale
    xScalePos += omnidirectionalScale
    yScalePos += omnidirectionalScale
    val positiveScale: Vector3 = new Vector3(xScalePos, yScalePos, zScalePos)
    if (Settings.USE_CACHE)
    {
      this.cache.put(cacheID, positiveScale)
    }
    return positiveScale
  }

  def getNegativeScale(): Vector3 =
  {
    val cacheID: String = "getNegativeScale"
    if (Settings.useCache)
    {
      if (this.cache.containsKey(cacheID))
      {
        if (this.cache.get(cacheID).isInstanceOf[Vector3])
        {
          return this.cache.get(cacheID).asInstanceOf[Vector3]
        }
      }
    }
    var zScaleNeg: Int = 0
    var xScaleNeg: Int = 0
    var yScaleNeg: Int = 0
    if (this.isAbsolute)
    {
      zScaleNeg = this.getModuleCount(ModularForceFieldSystem.itemModuleScale, this.getSlotsBasedOnDirection(ForgeDirection.NORTH))
      xScaleNeg = this.getModuleCount(ModularForceFieldSystem.itemModuleScale, this.getSlotsBasedOnDirection(ForgeDirection.WEST))
      yScaleNeg = this.getModuleCount(ModularForceFieldSystem.itemModuleScale, this.getSlotsBasedOnDirection(ForgeDirection.DOWN))
    }
    else
    {
      var direction: Nothing = this.getDirection
      if (direction == ForgeDirection.UP || direction == ForgeDirection.DOWN)
      {
        direction = ForgeDirection.NORTH
      }
      zScaleNeg = this.getModuleCount(ModularForceFieldSystem.itemModuleScale, this.getSlotsBasedOnDirection(VectorHelper.getOrientationFromSide(direction, ForgeDirection.NORTH)))
      xScaleNeg = this.getModuleCount(ModularForceFieldSystem.itemModuleScale, this.getSlotsBasedOnDirection(VectorHelper.getOrientationFromSide(direction, ForgeDirection.WEST)))
      yScaleNeg = this.getModuleCount(ModularForceFieldSystem.itemModuleScale, this.getSlotsBasedOnDirection(ForgeDirection.DOWN))
    }
    val omnidirectionalScale: Int = this.getModuleCount(ModularForceFieldSystem.itemModuleScale, this.getModuleSlots)
    zScaleNeg += omnidirectionalScale
    xScaleNeg += omnidirectionalScale
    yScaleNeg += omnidirectionalScale
    val negativeScale: Vector3 = new Vector3(xScaleNeg, yScaleNeg, zScaleNeg)
    if (Settings.USE_CACHE)
    {
      this.cache.put(cacheID, negativeScale)
    }
    return negativeScale
  }

  def getRotationYaw(): Int =
  {
    val cacheID: String = "getRotationYaw"
    if (Settings.USE_CACHE)
    {
      if (this.cache.containsKey(cacheID))
      {
        if (this.cache.get(cacheID).isInstanceOf[Integer])
        {
          return this.cache.get(cacheID).asInstanceOf[Integer]
        }
      }
    }
    var horizontalRotation: Int = 0
    if (this.isAbsolute)
    {
      horizontalRotation = this.getModuleCount(ModularForceFieldSystem.itemModuleRotate, this.getSlotsBasedOnDirection(ForgeDirection.EAST)) - this.getModuleCount(ModularForceFieldSystem.itemModuleRotate, this.getSlotsBasedOnDirection(ForgeDirection.WEST)) + this.getModuleCount(ModularForceFieldSystem.itemModuleRotate, this.getSlotsBasedOnDirection(ForgeDirection.SOUTH)) - this.getModuleCount(ModularForceFieldSystem.itemModuleRotate, this.getSlotsBasedOnDirection(ForgeDirection.NORTH))
    }
    else
    {
      val direction: Nothing = this.getDirection
      horizontalRotation = this.getModuleCount(ModularForceFieldSystem.itemModuleRotate, this.getSlotsBasedOnDirection(VectorHelper.getOrientationFromSide(direction, ForgeDirection.EAST))) - this.getModuleCount(ModularForceFieldSystem.itemModuleRotate, this.getSlotsBasedOnDirection(VectorHelper.getOrientationFromSide(direction, ForgeDirection.WEST))) + this.getModuleCount(ModularForceFieldSystem.itemModuleRotate, this.getSlotsBasedOnDirection(VectorHelper.getOrientationFromSide(direction, ForgeDirection.SOUTH))) - this.getModuleCount(ModularForceFieldSystem.itemModuleRotate, this.getSlotsBasedOnDirection(VectorHelper.getOrientationFromSide(direction, ForgeDirection.NORTH)))
    }
    horizontalRotation *= 2
    if (Settings.USE_CACHE)
    {
      this.cache.put(cacheID, horizontalRotation)
    }
    return horizontalRotation
  }

  def getRotationPitch: Int =
  {
    val cacheID: String = "getRotationPitch"
    if (Settings.USE_CACHE)
    {
      if (this.cache.containsKey(cacheID))
      {
        if (this.cache.get(cacheID).isInstanceOf[Integer])
        {
          return this.cache.get(cacheID).asInstanceOf[Integer]
        }
      }
    }
    var verticleRotation: Int = this.getModuleCount(ModularForceFieldSystem.itemModuleRotate, this.getSlotsBasedOnDirection(ForgeDirection.UP)) - this.getModuleCount(ModularForceFieldSystem.itemModuleRotate, this.getSlotsBasedOnDirection(ForgeDirection.DOWN))
    verticleRotation *= 2
    if (Settings.USE_CACHE)
    {
      this.cache.put(cacheID, verticleRotation)
    }
    return verticleRotation
  }

  @SuppressWarnings(Array("unchecked")) def getInteriorPoints: Set[Vector3] =
  {
    val cacheID: String = "getInteriorPoints"
    if (Settings.USE_CACHE)
    {
      if (this.cache.containsKey(cacheID))
      {
        if (this.cache.get(cacheID).isInstanceOf[Set[_]])
        {
          return this.cache.get(cacheID).asInstanceOf[Set[Vector3]]
        }
      }
    }
    if (getModeStack != null && getModeStack.getItem.isInstanceOf[ICache])
    {
      (this.getModeStack.getItem.asInstanceOf[ICache]).clearCache
    }
    val newField: Set[Vector3] = this.getMode.getInteriorPoints(this)
    if (getModuleCount(ModularForceFieldSystem.itemModuleArray) > 0)
    {
      (ModularForceFieldSystem.itemModuleArray.asInstanceOf[ItemModuleArray]).onPreCalculateInterior(this, getMode.getExteriorPoints(this), newField)
    }
    val returnField: Set[Vector3] = new HashSet[_]
    val translation: Vector3 = this.getTranslation
    val rotationYaw: Int = this.getRotationYaw
    val rotationPitch: Int = this.getRotationPitch
    for (position <- newField)
    {
      val newPosition: Vector3 = position.clone
      if (rotationYaw != 0 || rotationPitch != 0)
      {
        newPosition.rotate(rotationYaw, rotationPitch)
      }
      newPosition.translate(new Vector3(this))
      newPosition.translate(translation)
      returnField.add(newPosition)
    }
    if (Settings.USE_CACHE)
    {
      this.cache.put(cacheID, returnField)
    }
    return returnField
  }

  def getSlotsBasedOnDirection(direction: ForgeDirection): Array[Int] =
  {
    direction match
    {
      case _ =>
        return Array[Int]
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
    isAbsolute = nbt.getBoolean("isAbsolute")
  }

  override def writeToNBT(nbt: NBTTagCompound)
  {
    super.writeToNBT(nbt)
    nbt.setBoolean("isAbsolute", isAbsolute)
  }

}