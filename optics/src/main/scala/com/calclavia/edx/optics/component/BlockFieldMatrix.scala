package com.calclavia.edx.optics.component

import java.util.{Optional, Set => JSet}

import com.calclavia.edx.core.EDX
import com.calclavia.edx.optics.api.machine.{FieldMatrix, IPermissionProvider}
import com.calclavia.edx.optics.api.modules.StructureProvider
import com.calclavia.edx.optics.content.OpticsContent
import com.calclavia.edx.optics.util.CacheHandler
import com.resonant.core.structure.Structure
import nova.core.component.transform.Orientation
import nova.core.item.{Item, ItemFactory}
import nova.core.network.Sync
import nova.core.retention.Store
import nova.core.util.Direction
import nova.core.util.math.RotationUtil
import nova.scala.wrapper.VectorWrapper._
import org.apache.commons.math3.geometry.euclidean.threed.{Rotation, Vector3D}

import scala.collection.convert.wrapAll._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

abstract class BlockFieldMatrix extends BlockFrequency with FieldMatrix with IPermissionProvider with CacheHandler {
	val _getModuleSlots = (14 until 25).toArray
	protected val modeSlotID = 0

	/**
	 * Are the directions on the GUI absolute values?
	 */
	@Sync(ids = Array(BlockPacketID.description, BlockPacketID.toggleMode4))
	@Store
	var absoluteDirection = false

	protected var calculatedField: Set[Vector3D] = null

	protected var isCalculating = false

	val crystalHandler = add(new CrystalHandler(this))

	add(new Orientation(this))

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

	override def getShape: StructureProvider = getShapeItem

	/**
	 * @return Gets the item that provides a shape
	 */
	def getShapeItem: Item with StructureProvider = {
		val optional = inventory.get(modeSlotID)
		if (optional.isPresent) {
			if (optional.get.isInstanceOf[Item with StructureProvider]) {
				return optional.get.asInstanceOf[Item with StructureProvider]
			}
		}
		return null
	}

	override def getSidedModuleCount(module: ItemFactory, directions: Direction*): Int = {
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
	 * Gets the number of modules in this block that are in specific slots
	 * @param slots The slot IDs. Providing null will search all slots
	 * @return The number of all item modules in the slots.
	 */
	override def getModuleCount(compareModule: ItemFactory, slots: Int*): Int = crystalHandler.getModuleCount(compareModule, slots: _*)

	def getInteriorPoints: JSet[Vector3D] =
		getOrSetCache("getInteriorPoints", () => {
			if (getShapeItem.isInstanceOf[CacheHandler]) {
				getShapeItem.asInstanceOf[CacheHandler].clearCache
			}

			val structure = getStructure
			crystalHandler.getModules().foreach(_.onCalculateInterior(this, structure))
			return structure.getExteriorStructure
		})

	def getStructure: Structure = {
		val structure = getShapeItem.getStructure
		structure.setBlockFactory(Optional.of(OpticsContent.forceField))
		structure.setTranslate((getTranslation + transform.position))
		structure.setScale(getScale)
		structure.setRotation(getRotation)
		return structure
	}

	def getScale = (getPositiveScale + getNegativeScale) / 2

	def getPositiveScale: Vector3D =
		getOrSetCache("getPositiveScale", () => {
			var zScalePos = 0
			var xScalePos = 0
			var yScalePos = 0

			if (absoluteDirection) {
				zScalePos = getModuleCount(OpticsContent.moduleScale, getDirectionSlots(Direction.SOUTH): _*)
				xScalePos = getModuleCount(OpticsContent.moduleScale, getDirectionSlots(Direction.EAST): _*)
				yScalePos = getModuleCount(OpticsContent.moduleScale, getDirectionSlots(Direction.UP): _*)
			}
			else {
				val direction = get(classOf[Orientation]).orientation

				zScalePos = getModuleCount(OpticsContent.moduleScale, getDirectionSlots(RotationUtil.getRelativeSide(direction, Direction.SOUTH)): _*)
				xScalePos = getModuleCount(OpticsContent.moduleScale, getDirectionSlots(RotationUtil.getRelativeSide(direction, Direction.EAST)): _*)
				yScalePos = getModuleCount(OpticsContent.moduleScale, getDirectionSlots(Direction.UP): _*)
			}

			val omnidirectionalScale = getModuleCount(OpticsContent.moduleScale, getModuleSlots: _*)

			zScalePos += omnidirectionalScale
			xScalePos += omnidirectionalScale
			yScalePos += omnidirectionalScale

			return new Vector3D(xScalePos, yScalePos, zScalePos)
		})

	def getNegativeScale: Vector3D =
		getOrSetCache("getNegativeScale", () => {
			var zScaleNeg = 0
			var xScaleNeg = 0
			var yScaleNeg = 0

			val direction = get(classOf[Orientation]).orientation

			if (absoluteDirection) {
				zScaleNeg = getModuleCount(OpticsContent.moduleScale, getDirectionSlots(Direction.NORTH): _*)
				xScaleNeg = getModuleCount(OpticsContent.moduleScale, getDirectionSlots(Direction.WEST): _*)
				yScaleNeg = getModuleCount(OpticsContent.moduleScale, getDirectionSlots(Direction.DOWN): _*)
			}
			else {
				zScaleNeg = getModuleCount(OpticsContent.moduleScale, getDirectionSlots(RotationUtil.getRelativeSide(direction, Direction.NORTH)): _*)
				xScaleNeg = getModuleCount(OpticsContent.moduleScale, getDirectionSlots(RotationUtil.getRelativeSide(direction, Direction.WEST)): _*)
				yScaleNeg = getModuleCount(OpticsContent.moduleScale, getDirectionSlots(Direction.DOWN): _*)
			}

			val omnidirectionalScale = this.getModuleCount(OpticsContent.moduleScale, getModuleSlots: _*)
			zScaleNeg += omnidirectionalScale
			xScaleNeg += omnidirectionalScale
			yScaleNeg += omnidirectionalScale

			return new Vector3D(xScaleNeg, yScaleNeg, zScaleNeg)
		})

	def getModuleSlots: Array[Int] = _getModuleSlots

	def getTranslation: Vector3D =
		getOrSetCache("getTranslation", () => {

			val direction = get(classOf[Orientation]).orientation

			var zTranslationNeg = 0
			var zTranslationPos = 0
			var xTranslationNeg = 0
			var xTranslationPos = 0
			var yTranslationPos = 0
			var yTranslationNeg = 0

			if (absoluteDirection) {
				zTranslationNeg = getModuleCount(OpticsContent.moduleTranslate, getDirectionSlots(Direction.NORTH): _*)
				zTranslationPos = getModuleCount(OpticsContent.moduleTranslate, getDirectionSlots(Direction.SOUTH): _*)
				xTranslationNeg = getModuleCount(OpticsContent.moduleTranslate, getDirectionSlots(Direction.WEST): _*)
				xTranslationPos = getModuleCount(OpticsContent.moduleTranslate, getDirectionSlots(Direction.EAST): _*)
				yTranslationPos = getModuleCount(OpticsContent.moduleTranslate, getDirectionSlots(Direction.UP): _*)
				yTranslationNeg = getModuleCount(OpticsContent.moduleTranslate, getDirectionSlots(Direction.DOWN): _*)
			}
			else {
				zTranslationNeg = getModuleCount(OpticsContent.moduleTranslate, getDirectionSlots(RotationUtil.getRelativeSide(direction, Direction.NORTH)): _*)
				zTranslationPos = getModuleCount(OpticsContent.moduleTranslate, getDirectionSlots(RotationUtil.getRelativeSide(direction, Direction.SOUTH)): _*)
				xTranslationNeg = getModuleCount(OpticsContent.moduleTranslate, getDirectionSlots(RotationUtil.getRelativeSide(direction, Direction.WEST)): _*)
				xTranslationPos = getModuleCount(OpticsContent.moduleTranslate, getDirectionSlots(RotationUtil.getRelativeSide(direction, Direction.EAST)): _*)
				yTranslationPos = getModuleCount(OpticsContent.moduleTranslate, getDirectionSlots(Direction.UP): _*)
				yTranslationNeg = getModuleCount(OpticsContent.moduleTranslate, getDirectionSlots(Direction.DOWN): _*)
			}

			return new Vector3D(xTranslationPos - xTranslationNeg, yTranslationPos - yTranslationNeg, zTranslationPos - zTranslationNeg)
		})

	def getRotation = new Rotation(RotationUtil.DEFAULT_ORDER, Math.toRadians(getRotationYaw), Math.toRadians(getRotationPitch), 0)

	/**
	 * @return Gets the rotation yaw in degrees
	 */
	def getRotationYaw: Int =
		getOrSetCache("getRotationYaw", () => {

			var horizontalRotation = 0
			val direction = get(classOf[Orientation]).orientation

			if (this.absoluteDirection) {
				horizontalRotation = getModuleCount(OpticsContent.moduleRotate, getDirectionSlots(Direction.EAST): _*) - getModuleCount(OpticsContent.moduleRotate, getDirectionSlots(Direction.WEST): _*) + getModuleCount(OpticsContent.moduleRotate, this.getDirectionSlots(Direction.SOUTH): _*) - this.getModuleCount(OpticsContent.moduleRotate, getDirectionSlots(Direction.NORTH): _*)
			}
			else {
				horizontalRotation = getModuleCount(OpticsContent.moduleRotate, getDirectionSlots(RotationUtil.getRelativeSide(direction, Direction.EAST)): _*) - getModuleCount(OpticsContent.moduleRotate, getDirectionSlots(RotationUtil.getRelativeSide(direction, Direction.WEST)): _*) + this.getModuleCount(OpticsContent.moduleRotate, getDirectionSlots(RotationUtil.getRelativeSide(direction, Direction.SOUTH)): _*) - getModuleCount(OpticsContent.moduleRotate, getDirectionSlots(RotationUtil.getRelativeSide(direction, Direction.NORTH)): _*)
			}

			return horizontalRotation * 2
		})

	def getRotationPitch: Int =
		getOrSetCache("getRotationPitch", () => {
			val verticalRotation = getModuleCount(OpticsContent.moduleRotate, getDirectionSlots(Direction.UP): _*) - getModuleCount(OpticsContent.moduleRotate, getDirectionSlots(Direction.DOWN): _*)
			return verticalRotation * 2
		})

	def getCalculatedField: JSet[Vector3D] = if (calculatedField != null) calculatedField else Set.empty[Vector3D]

	/**
	 * Calculates the force field
	 * @param callBack - Optional callback
	 */
	protected def calculateField(callBack: () => Unit = null) {
		if (EDX.network.isServer && !isCalculating) {
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
						calculatedField = field.toSet
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

	/**
	 * Gets the exterior points of the field based on the matrix.
	 */
	protected def getExteriorPoints: JSet[Vector3D] = {
		val structure = getStructure

		crystalHandler.getModules().foreach(_.onCalculateExterior(this, structure))

		val field = {
			if (getModuleCount(OpticsContent.moduleInvert) > 0) {
				structure.getInteriorStructure
			}
			else {
				structure.getExteriorStructure
			}
		}

		return field
	}
}
