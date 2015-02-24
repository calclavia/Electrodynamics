package mffs.base

import java.util.{Set => JSet}

import com.resonant.core.prefab.block.Rotatable
import mffs.Content
import mffs.api.machine.{FieldMatrix, IPermissionProvider}
import mffs.field.mobilize.event.{DelayedEvent, IDelayedEventHandler}
import mffs.field.module.ItemModuleArray
import mffs.item.card.ItemCard
import mffs.util.CacheHandler
import nova.core.network.Packet

abstract class TileFieldMatrix extends BlockModuleAcceptor with FieldMatrix with IDelayedEventHandler with Rotatable with IPermissionProvider
{
  protected final val delayedEvents = new mutable.SynchronizedQueue[DelayedEvent]()
  val _getModuleSlots = (14 until 25).toArray
  protected val modeSlotID = 1
  /**
   * Are the directions on the GUI absolute values?
   */
  var absoluteDirection = false
	protected var calculatedField: mutable.Set[Vector3d] = null
  protected var isCalculating = false

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

	override def write(buf: Packet, id: Int)
  {
    super.write(buf, id)

	  if (id == PacketBlock.description.id)
    {
      buf <<< absoluteDirection
    }
  }

	override def read(buf: Packet, id: Int, packetType: PacketType)
  {
    super.read(buf, id, packetType)

    if (world.isRemote)
    {
		if (id == PacketBlock.description.id)
      {
        absoluteDirection = buf.readBoolean()
      }
    }
    else
    {
		if (id == PacketBlock.toggleMode4.id)
      {
        absoluteDirection = !absoluteDirection
      }
    }
  }

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

	  return Item.getItem.isInstanceOf[IModule]
  }

  def getSidedModuleCount(module: IModule, directions: ForgeDirection*): Int =
  {
    var actualDirs = directions

    if (directions == null || directions.length > 0)
      actualDirs = ForgeDirection.VALID_DIRECTIONS

    return actualDirs.foldLeft(0)((b, a) => b + getModuleCount(module, getDirectionSlots(a): _*))
  }

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

	def getPositiveScale: Vector3d =
  {
    val cacheID = "getPositiveScale"

	  if (hasCache(classOf[Vector3d], cacheID)) return getCache(classOf[Vector3d], cacheID)

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

	  val positiveScale = new Vector3d(xScalePos, yScalePos, zScalePos)

    cache(cacheID, positiveScale)

    return positiveScale
  }

	def getModuleSlots: Array[Int] = _getModuleSlots

	def getNegativeScale: Vector3d =
  {
    val cacheID = "getNegativeScale"

	  if (hasCache(classOf[Vector3d], cacheID)) return getCache(classOf[Vector3d], cacheID)

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

	  val negativeScale = new Vector3d(xScaleNeg, yScaleNeg, zScaleNeg)

    cache(cacheID, negativeScale)

    return negativeScale
  }

	def getInteriorPoints: JSet[Vector3d] =
  {
    val cacheID = "getInteriorPoints"

	  if (hasCache(classOf[Set[Vector3d]], cacheID)) return getCache(classOf[Set[Vector3d]], cacheID)

	  if (getModeStack != null && getModeStack.getItem.isInstanceOf[CacheHandler])
    {
		(getModeStack.getItem.asInstanceOf[CacheHandler]).clearCache
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

  def getMode: IProjectorMode =
  {
    if (this.getModeStack != null)
    {
      return this.getModeStack.getItem.asInstanceOf[IProjectorMode]
    }
    return null
  }

	def getModeStack: Item = {
		if (this.getStackInSlot(modeSlotID) != null) {
			if (this.getStackInSlot(modeSlotID).getItem.isInstanceOf[IProjectorMode]) {
				return this.getStackInSlot(modeSlotID)
			}
		}
		return null
	}

	def getTranslation: Vector3d =
  {
    val cacheID = "getTranslation"

	  if (hasCache(classOf[Vector3d], cacheID)) return getCache(classOf[Vector3d], cacheID)

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

	  val translation = new Vector3d(xTranslationPos - xTranslationNeg, yTranslationPos - yTranslationNeg, zTranslationPos - zTranslationNeg)

    cache(cacheID, translation)

    return translation
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

	def getCalculatedField: JSet[Vector3d] = {
		return if (calculatedField != null) calculatedField else mutable.Set.empty[Vector3d]
	}

	def queueEvent(evt: DelayedEvent) {
		delayedEvents += evt
	}

	/**
	 * NBT Methods
	 */
	override def readFromNBT(nbt: NBTTagCompound) {
		super.readFromNBT(nbt)
		absoluteDirection = nbt.getBoolean("isAbsolute")
	}

	override def writeToNBT(nbt: NBTTagCompound) {
		super.writeToNBT(nbt)
		nbt.setBoolean("isAbsolute", absoluteDirection)
	}

	/**
	 * Calculates the force field
	 * @param callBack - Optional callback
	 */
	protected def calculateField(callBack: () => Unit = null) {
		if (Game.instance.networkManager.isServer && !isCalculating) {
			if (getMode != null) {
				//Clear mode cache
				if (getModeStack.getItem.isInstanceOf[CacheHandler]) {
					getModeStack.getItem.asInstanceOf[CacheHandler].clearCache()
				}

				isCalculating = true

				Future {
					generateCalculatedField
				}.onComplete {
					case Success(field) => {
						calculatedField = field
						isCalculating = false

						if (callBack != null) {
							callBack.apply()
						}
					}
					case Failure(t) => {
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
	protected def getExteriorPoints: mutable.Set[Vector3d] = {
		var field = mutable.Set.empty[Vector3d]

		if (getModuleCount(Content.moduleInvert) > 0) {
			field = getMode.getInteriorPoints(this)
		}
		else {
			field = getMode.getExteriorPoints(this)
		}

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

}