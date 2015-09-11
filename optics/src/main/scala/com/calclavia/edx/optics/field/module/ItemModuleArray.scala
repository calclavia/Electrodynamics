package com.calclavia.edx.optics.field.module

import java.util.{Set => JSet}

import com.calclavia.edx.optics.api.machine.FieldMatrix
import com.calclavia.edx.optics.component.ItemModule
import com.resonant.core.structure.Structure
import nova.core.util.Direction
import nova.scala.wrapper.VectorWrapper._
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D

class ItemModuleArray extends ItemModule {

	override def onCalculateExterior(projector: FieldMatrix, structure: Structure) {
		generateArray(projector, structure)
	}

	override def onCalculateInterior(projector: FieldMatrix, structure: Structure) {
		generateArray(projector, structure)
	}

	def generateArray(projector: FieldMatrix, structure: Structure) {
		structure.postStructure = structure.postStructure.compose(
			(field: Set[Vector3D]) => {
				var newField = Set.empty[Vector3D]
				val longestDirectional = getDirectionWidthMap(field)

				//TODO: Execute concurrently. Test speed.
				Direction.VALID_DIRECTIONS.foreach(
					dir => {
						val copyAmount = projector.getSidedModuleCount(getFactory(), dir)
						val directionalDisplacement = Math.abs(longestDirectional(dir)) + Math.abs(longestDirectional(dir.opposite)) + 1

						(0 until copyAmount).foreach(
							i => {
								val dirDisplacementScale = directionalDisplacement * (i + 1)

								field.foreach(
									originalFieldBlock => {
										val newFieldBlock = originalFieldBlock + (dir.toVector * dirDisplacementScale)
										newField += newFieldBlock
									}
								)
							}
						)
					}
				)

				newField
			}
		)
	}

	def getDirectionWidthMap(field: Set[Vector3D]): Map[Direction, Int] = {
		var longestDirectional = Map.empty[Direction, Int]

		longestDirectional += (Direction.DOWN -> 0)
		longestDirectional += (Direction.UP -> 0)
		longestDirectional += (Direction.NORTH -> 0)
		longestDirectional += (Direction.SOUTH -> 0)
		longestDirectional += (Direction.WEST -> 0)
		longestDirectional += (Direction.EAST -> 0)

		for (fieldPosition <- field) {
			if (fieldPosition.xi > 0 && fieldPosition.xi > longestDirectional(Direction.EAST)) {
				longestDirectional += (Direction.EAST -> fieldPosition.xi)
			}
			else if (fieldPosition.xi < 0 && fieldPosition.xi < longestDirectional(Direction.WEST)) {
				longestDirectional += (Direction.WEST -> fieldPosition.xi)
			}
			if (fieldPosition.yi > 0 && fieldPosition.yi > longestDirectional(Direction.UP)) {
				longestDirectional += (Direction.UP -> fieldPosition.yi)
			}
			else if (fieldPosition.yi < 0 && fieldPosition.yi < longestDirectional(Direction.DOWN)) {
				longestDirectional += (Direction.DOWN -> fieldPosition.yi)
			}
			if (fieldPosition.zi > 0 && fieldPosition.zi > longestDirectional(Direction.SOUTH)) {
				longestDirectional += (Direction.SOUTH -> fieldPosition.zi)
			}
			else if (fieldPosition.zi < 0 && fieldPosition.zi < longestDirectional(Direction.NORTH)) {
				longestDirectional += (Direction.NORTH -> fieldPosition.zi)
			}
		}

		return longestDirectional
	}

	override def getFortronCost(amplifier: Float): Float = super.getFortronCost(amplifier) + (super.getFortronCost(amplifier) * amplifier) / 100f
}