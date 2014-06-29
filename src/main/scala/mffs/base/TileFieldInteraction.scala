package mffs.base

import java.util.{HashSet, LinkedList, Queue, Set}

import com.google.common.io.ByteArrayDataInput
import mffs.field.module.ItemModuleArray
import mffs.{ModularForceFieldSystem, Settings}
import mffs.mobilize.event.{DelayedEvent, IDelayedEventHandler}
import mffs.field.thread.ProjectorCalculationThread
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import resonant.api.mffs.{ICache, IFieldInteraction}
import resonant.api.mffs.modules.{IModule, IProjectorMode}
import universalelectricity.core.transform.vector.Vector3

abstract class TileFieldInteraction extends TileModuleAcceptor with IFieldInteraction with IDelayedEventHandler
{
  protected val moduleSlotID: Int = 2

  override def updateEntity
  {
    super.updateEntity
    val continueEvents: Queue[DelayedEvent] = new LinkedList[_]
    while (!delayedEvents.isEmpty)
    {
      val evt: DelayedEvent = delayedEvents.poll
      evt.update
      if (evt.ticks > 0)
      {
        continueEvents.add(evt)
      }
    }
    delayedEvents.addAll(continueEvents)
  }

  override def onReceivePacket(packetID: Int, dataStream: ByteArrayDataInput)
  {
    super.onReceivePacket(packetID, dataStream)
    if (packetID == TilePacketType.TOGGLE_MODE_4.ordinal && !this.worldObj.isRemote)
    {
      this.isAbsolute = !this.isAbsolute
    }
  }

  protected def calculateForceField(callBack: Nothing)
  {
    if (!this.worldObj.isRemote && !this.isCalculating)
    {
      if (this.getMode != null)
      {
        if (this.getModeStack.getItem.isInstanceOf[ICache])
        {
          (this.getModeStack.getItem.asInstanceOf[ICache]).clearCache
        }
        calculatedField.clear
        (new ProjectorCalculationThread(this, callBack)).start
        isCalculating = true
      }
    }
  }

  protected def calculateForceField
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

  def getSidedModuleCount(module: IModule, direction: Nothing*): Int =
  {
    var count: Int = 0
    if (direction != null && direction.length > 0)
    {
      for (checkDir <- direction)
      {
        count += this.getModuleCount(module, this.getSlotsBasedOnDirection(checkDir))
      }
    }
    else
    {
      {
        var i: Int = 0
        while (i < 6)
        {
          {
            val checkDir: Nothing = ForgeDirection.getOrientation(i)
            count += this.getModuleCount(module, this.getSlotsBasedOnDirection(checkDir))
          }
          ({
            i += 1; i - 1
          })
        }
      }
    }
    return count
  }

  def getModuleSlots: Array[Int] =
  {
    return Array[Int](15, 16, 17, 18, 19, 20)
  }

  def getTranslation: Vector3 =
  {
    val cacheID: String = "getTranslation"
    if (Settings.USE_CACHE)
    {
      if (this.cache.containsKey(cacheID))
      {
        if (this.cache.get(cacheID).isInstanceOf[Vector3])
        {
          return this.cache.get(cacheID).asInstanceOf[Vector3]
        }
      }
    }
    var direction: Nothing = this.getDirection
    if (direction eq ForgeDirection.UP || direction eq ForgeDirection.DOWN)
    {
      direction = ForgeDirection.NORTH
    }
    var zTranslationNeg: Int = 0
    var zTranslationPos: Int = 0
    var xTranslationNeg: Int = 0
    var xTranslationPos: Int = 0
    var yTranslationPos: Int = 0
    var yTranslationNeg: Int = 0
    if (this.isAbsolute)
    {
      zTranslationNeg = this.getModuleCount(ModularForceFieldSystem.itemModuleTranslate, this.getSlotsBasedOnDirection(ForgeDirection.NORTH))
      zTranslationPos = this.getModuleCount(ModularForceFieldSystem.itemModuleTranslate, this.getSlotsBasedOnDirection(ForgeDirection.SOUTH))
      xTranslationNeg = this.getModuleCount(ModularForceFieldSystem.itemModuleTranslate, this.getSlotsBasedOnDirection(ForgeDirection.WEST))
      xTranslationPos = this.getModuleCount(ModularForceFieldSystem.itemModuleTranslate, this.getSlotsBasedOnDirection(ForgeDirection.EAST))
      yTranslationPos = this.getModuleCount(ModularForceFieldSystem.itemModuleTranslate, this.getSlotsBasedOnDirection(ForgeDirection.UP))
      yTranslationNeg = this.getModuleCount(ModularForceFieldSystem.itemModuleTranslate, this.getSlotsBasedOnDirection(ForgeDirection.DOWN))
    }
    else
    {
      zTranslationNeg = this.getModuleCount(ModularForceFieldSystem.itemModuleTranslate, this.getSlotsBasedOnDirection(VectorHelper.getOrientationFromSide(direction, ForgeDirection.NORTH)))
      zTranslationPos = this.getModuleCount(ModularForceFieldSystem.itemModuleTranslate, this.getSlotsBasedOnDirection(VectorHelper.getOrientationFromSide(direction, ForgeDirection.SOUTH)))
      xTranslationNeg = this.getModuleCount(ModularForceFieldSystem.itemModuleTranslate, this.getSlotsBasedOnDirection(VectorHelper.getOrientationFromSide(direction, ForgeDirection.WEST)))
      xTranslationPos = this.getModuleCount(ModularForceFieldSystem.itemModuleTranslate, this.getSlotsBasedOnDirection(VectorHelper.getOrientationFromSide(direction, ForgeDirection.EAST)))
      yTranslationPos = this.getModuleCount(ModularForceFieldSystem.itemModuleTranslate, this.getSlotsBasedOnDirection(ForgeDirection.UP))
      yTranslationNeg = this.getModuleCount(ModularForceFieldSystem.itemModuleTranslate, this.getSlotsBasedOnDirection(ForgeDirection.DOWN))
    }
    val translation: Vector3 = new Vector3(xTranslationPos - xTranslationNeg, yTranslationPos - yTranslationNeg, zTranslationPos - zTranslationNeg)
    if (Settings.USE_CACHE)
    {
      this.cache.put(cacheID, translation)
    }
    return translation
  }

  def getPositiveScale: Vector3 =
  {
    val cacheID: String = "getPositiveScale"
    if (Settings.USE_CACHE)
    {
      if (this.cache.containsKey(cacheID))
      {
        if (this.cache.get(cacheID).isInstanceOf[Vector3])
        {
          return this.cache.get(cacheID).asInstanceOf[Vector3]
        }
      }
    }
    var zScalePos: Int = 0
    var xScalePos: Int = 0
    var yScalePos: Int = 0
    if (this.isAbsolute)
    {
      zScalePos = this.getModuleCount(ModularForceFieldSystem.itemModuleScale, this.getSlotsBasedOnDirection(ForgeDirection.SOUTH))
      xScalePos = this.getModuleCount(ModularForceFieldSystem.itemModuleScale, this.getSlotsBasedOnDirection(ForgeDirection.EAST))
      yScalePos = this.getModuleCount(ModularForceFieldSystem.itemModuleScale, this.getSlotsBasedOnDirection(ForgeDirection.UP))
    }
    else
    {
      var direction: Nothing = this.getDirection
      if (direction eq ForgeDirection.UP || direction eq ForgeDirection.DOWN)
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

  def getNegativeScale: Vector3 =
  {
    val cacheID: String = "getNegativeScale"
    if (Settings.USE_CACHE)
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
      if (direction eq ForgeDirection.UP || direction eq ForgeDirection.DOWN)
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

  def getRotationYaw: Int =
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
    import scala.collection.JavaConversions._
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

  def getSlotsBasedOnDirection(direction: Nothing): Array[Int] =
  {
    direction match
    {
      case _ =>
        return Array[Int]
      case UP =>
        return Array[Int](3, 11)
      case DOWN =>
        return Array[Int](6, 14)
      case NORTH =>
        return Array[Int](7, 9)
      case SOUTH =>
        return Array[Int](8, 10)
      case WEST =>
        return Array[Int](4, 5)
      case EAST =>
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

  protected final val calculatedField: Set[Vector3] = new HashSet[_]
  protected final val delayedEvents: Queue[DelayedEvent] = new LinkedList[_]
  /**
   * Are the directions on the GUI absolute values?
   */
  var isAbsolute: Boolean = false
  protected var isCalculating: Boolean = false
  protected var isCalculated: Boolean = false
}