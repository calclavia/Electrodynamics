package mffs.base

import java.util.{Optional, Set => JSet}

import com.resonant.core.prefab.block.Rotatable
import com.resonant.core.structure.Structure
import com.resonant.lib.util.RotationUtility
import mffs.api.machine.{FieldMatrix, IPermissionProvider}
import mffs.api.modules.StructureProvider
import mffs.content.Content
import mffs.util.CacheHandler
import nova.core.game.Game
import nova.core.item.Item
import nova.core.network.Sync
import nova.core.retention.Stored
import nova.core.util.Direction
import nova.core.util.transform._

import scala.concurrent.Future
import scala.util.{Failure, Success}

abstract class BlockFieldMatrix extends BlockModuleHandler with FieldMatrix with Rotatable with IPermissionProvider {
	val _getModuleSlots = (14 until 25).toArray
	protected val modeSlotID = 1

	/**
	 * Are the directions on the GUI absolute values?
	 */
	@Sync(ids = Array(PacketBlock.description.ordinal(), PacketBlock.toggleMode4.ordinal()))
	@Stored
	var absoluteDirection = false

	protected var calculatedField: Set[Vector3i] = null

	protected var isCalculating = false

	/*
	override def isItemValidForSlot(slotID: Int, Item: Item): Boolean = {
		if (slotID == 0) {
			return Item.getItem.isInstanceOf[ItemCard]
		}
		else if (slotID == modeSlotID) {
			return Item.getItem.isInstanceOf[IProjectorMode]
		}

		return Item.getItem.isInstanceOf[IModule]
	}*/

	def getModuleSlots: Array[Int] = _getModuleSlots

	def getSidedModuleCount(module: Item, directions: Direction*): Int = {
		var actualDirs = directions

		if (directions == null || directions.length > 0) {
			actualDirs = Direction.DIRECTIONS
		}

		return actualDirs.foldLeft(0)((b, a) => b + getModuleCount(module, getDirectionSlots(a): _*))
	}

	override def getDirectionSlots(direction: Direction): Array[Int] =
		direction match {
			case Direction.UP =>
				Array(10, 11)
			case Direction.DOWN =>
				Array(12, 13)
			case Direction.SOUTH =>
				Array(2, 3)
			case Direction.NORTH =>
				Array(4, 5)
			case Direction.WEST =>
				Array(6, 7)
			case Direction.EAST =>
				Array(8, 9)
			case _ =>
				Array[Int]()
		}

	/**
	 * @return Gets the item that provides a shape
	 */
	def getShapeItem: Item with StructureProvider = {
		val optional = inventory.get(modeSlotID)
		if (optional.isPresent) {
			if (optional.get().isInstanceOf[Item with StructureProvider]) {
				return optional.asInstanceOf[Item with StructureProvider]
			}
		}
		return null
	}

	def getScale = (getPositiveScale + getNegativeScale) / 2

	def getPositiveScale: Vector3d =
		getOrSetCache("getPositiveScale", () => {
			var zScalePos = 0
			var xScalePos = 0
			var yScalePos = 0

			if (absoluteDirection) {
				zScalePos = getModuleCount(Content.moduleScale, getDirectionSlots(Direction.SOUTH): _*)
				xScalePos = getModuleCount(Content.moduleScale, getDirectionSlots(Direction.EAST): _*)
				yScalePos = getModuleCount(Content.moduleScale, getDirectionSlots(Direction.UP): _*)
			}
			else {
				val direction = getDirection

				zScalePos = getModuleCount(Content.moduleScale, getDirectionSlots(RotationUtility.getRelativeSide(direction, Direction.SOUTH)): _*)
				xScalePos = getModuleCount(Content.moduleScale, getDirectionSlots(RotationUtility.getRelativeSide(direction, Direction.EAST)): _*)
				yScalePos = getModuleCount(Content.moduleScale, getDirectionSlots(Direction.UP): _*)
			}

			val omnidirectionalScale = getModuleCount(Content.moduleScale, getModuleSlots: _*)

			zScalePos += omnidirectionalScale
			xScalePos += omnidirectionalScale
			yScalePos += omnidirectionalScale

			return new Vector3d(xScalePos, yScalePos, zScalePos)
		})

	def getNegativeScale: Vector3d =
		getOrSetCache("getNegativeScale", () => {
			var zScaleNeg = 0
			var xScaleNeg = 0
			var yScaleNeg = 0

			val direction = getDirection

			if (absoluteDirection) {
				zScaleNeg = getModuleCount(Content.moduleScale, getDirectionSlots(Direction.NORTH): _*)
				xScaleNeg = getModuleCount(Content.moduleScale, getDirectionSlots(Direction.WEST): _*)
				yScaleNeg = getModuleCount(Content.moduleScale, getDirectionSlots(Direction.DOWN): _*)
			}
			else {
				zScaleNeg = getModuleCount(Content.moduleScale, getDirectionSlots(RotationUtility.getRelativeSide(direction, Direction.NORTH)): _*)
				xScaleNeg = getModuleCount(Content.moduleScale, getDirectionSlots(RotationUtility.getRelativeSide(direction, Direction.WEST)): _*)
				yScaleNeg = getModuleCount(Content.moduleScale, getDirectionSlots(Direction.DOWN): _*)
			}

			val omnidirectionalScale = this.getModuleCount(Content.moduleScale, getModuleSlots: _*)
			zScaleNeg += omnidirectionalScale
			xScaleNeg += omnidirectionalScale
			yScaleNeg += omnidirectionalScale

			return new Vector3d(xScaleNeg, yScaleNeg, zScaleNeg)
		})



	def getTranslation: Vector3d =
		getOrSetCache("getTranslation", () => {

			val direction = getDirection

			var zTranslationNeg = 0
			var zTranslationPos = 0
			var xTranslationNeg = 0
			var xTranslationPos = 0
			var yTranslationPos = 0
			var yTranslationNeg = 0

			if (absoluteDirection) {
				zTranslationNeg = getModuleCount(Content.moduleTranslate, getDirectionSlots(Direction.NORTH): _*)
				zTranslationPos = getModuleCount(Content.moduleTranslate, getDirectionSlots(Direction.SOUTH): _*)
				xTranslationNeg = getModuleCount(Content.moduleTranslate, getDirectionSlots(Direction.WEST): _*)
				xTranslationPos = getModuleCount(Content.moduleTranslate, getDirectionSlots(Direction.EAST): _*)
				yTranslationPos = getModuleCount(Content.moduleTranslate, getDirectionSlots(Direction.UP): _*)
				yTranslationNeg = getModuleCount(Content.moduleTranslate, getDirectionSlots(Direction.DOWN): _*)
			}
			else {
				zTranslationNeg = getModuleCount(Content.moduleTranslate, getDirectionSlots(RotationUtility.getRelativeSide(direction, Direction.NORTH)): _*)
				zTranslationPos = getModuleCount(Content.moduleTranslate, getDirectionSlots(RotationUtility.getRelativeSide(direction, Direction.SOUTH)): _*)
				xTranslationNeg = getModuleCount(Content.moduleTranslate, getDirectionSlots(RotationUtility.getRelativeSide(direction, Direction.WEST)): _*)
				xTranslationPos = getModuleCount(Content.moduleTranslate, getDirectionSlots(RotationUtility.getRelativeSide(direction, Direction.EAST)): _*)
				yTranslationPos = getModuleCount(Content.moduleTranslate, getDirectionSlots(Direction.UP): _*)
				yTranslationNeg = getModuleCount(Content.moduleTranslate, getDirectionSlots(Direction.DOWN): _*)
			}

			return new Vector3d(xTranslationPos - xTranslationNeg, yTranslationPos - yTranslationNeg, zTranslationPos - zTranslationNeg)
		})

	/**
	 * @return Gets the rotation yaw in degrees
	 */
	def getRotationYaw: Int =
		getOrSetCache("getRotationYaw", () => {

			var horizontalRotation = 0
			val direction = getDirection

			if (this.absoluteDirection) {
				horizontalRotation = getModuleCount(Content.moduleRotate, getDirectionSlots(Direction.EAST): _*) - getModuleCount(Content.moduleRotate, getDirectionSlots(Direction.WEST): _*) + getModuleCount(Content.moduleRotate, this.getDirectionSlots(Direction.SOUTH): _*) - this.getModuleCount(Content.moduleRotate, getDirectionSlots(Direction.NORTH): _*)
			}
			else {
				horizontalRotation = getModuleCount(Content.moduleRotate, getDirectionSlots(RotationUtility.getRelativeSide(direction, Direction.EAST)): _*) - getModuleCount(Content.moduleRotate, getDirectionSlots(RotationUtility.getRelativeSide(direction, Direction.WEST)): _*) + this.getModuleCount(Content.moduleRotate, getDirectionSlots(RotationUtility.getRelativeSide(direction, Direction.SOUTH)): _*) - getModuleCount(Content.moduleRotate, getDirectionSlots(RotationUtility.getRelativeSide(direction, Direction.NORTH)): _*)
			}

			return horizontalRotation * 2
		})

	def getRotationPitch: Int =
		getOrSetCache("getRotationPitch", () => {
			val verticalRotation = getModuleCount(Content.moduleRotate, getDirectionSlots(Direction.UP): _*) - getModuleCount(Content.moduleRotate, getDirectionSlots(Direction.DOWN): _*)
			return verticalRotation * 2
		})

	def getRotation = Quaternion.fromEuler(Math.toRadians(getRotationYaw), Math.toRadians(getRotationPitch), 0)

	def getInteriorPoints: Set[Vector3i] =
		getOrSetCache("getInteriorPoints", () => {
			if (getShapeItem.isInstanceOf[CacheHandler]) {
				getShapeItem.asInstanceOf[CacheHandler].clearCache
			}

			val structure = getStructure
			getModules().foreach(_.onCalculateInterior(this, structure))
			return structure.getExteriorStructure
		})

	/**
	 * Gets the exterior points of the field based on the matrix.
	 */
	protected def getExteriorPoints: Set[Vector3i] = {
		val structure = getStructure

		getModules().foreach(_.onCalculateExterior(this, structure))

		val field = {
			if (getModuleCount(Content.moduleInvert) > 0) {
				structure.getInteriorStructure
			}
			else {
				structure.getExteriorStructure
			}
		}

		return field
	}

	def getStructure: Structure = {
		val structure = getShapeItem.getStructure
		structure.setBlock(Optional.of(Content.forceField))
		structure.setTranslate(getTranslation + position.toDouble)
		structure.setScale(getScale)
		structure.setRotation(getRotation)
		return structure
	}

	def getCalculatedField: Set[Vector3i] = if (calculatedField != null) calculatedField else Set.empty

	/**
	 * Calculates the force field
	 * @param callBack - Optional callback
	 */
	protected def calculateField(callBack: () => Unit = null) {
		if (Game.instance.networkManager.isServer && !isCalculating) {
			if (getShapeItem != null) {
				//Clear mode cache
				if (getShapeItem.isInstanceOf[CacheHandler]) {
					getShapeItem.asInstanceOf[CacheHandler].clearCache()
				}

				isCalculating = true

				Future {
					generateField
				}.onComplete {
					case Success(field) =>
						calculatedField = field
						isCalculating = false

						if (callBack != null) {
							callBack.apply()
						}
					case Failure(t) =>
						//println(getClass.getName + ": An error has occurred upon field calculation: " + t.getMessage)
						isCalculating = false
				}
			}
		}
	}

	protected def generateField = getExteriorPoints
}
