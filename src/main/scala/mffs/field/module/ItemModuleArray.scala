package mffs.field.module

import java.util.{Set => JSet}

import com.resonant.core.structure.Structure
import mffs.api.machine.FieldMatrix
import mffs.base.ItemModule
import nova.core.util.Direction
import nova.core.util.transform.Vector3i

class ItemModuleArray extends ItemModule {

	override def getID: String = "moduleArray"

	override def onCalculateExterior(projector: FieldMatrix, structure: Structure) {
		generateArray(projector, structure)
	}

	override def onCalculateInterior(projector: FieldMatrix, structure: Structure) {
		generateArray(projector, structure)
	}

	def generateArray(projector: FieldMatrix, structure: Structure) {
		structure.postStructure = structure.postStructure.compose(
			(field: Set[Vector3i]) => {
				var newField = Set.empty[Vector3i]
				val longestDirectional = getDirectionWidthMap(field)

				//TODO: Execute concurrently. Test speed.
				Direction.DIRECTIONS.foreach(
					dir => {
						val copyAmount = projector.getSidedModuleCount(this, dir)
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

				return newField
			}
		)
	}

	def getDirectionWidthMap(field: Set[Vector3i]): Map[Direction, Int] = {
		var longestDirectional = Map.empty[Direction, Int]

		longestDirectional += (Direction.DOWN -> 0)
		longestDirectional += (Direction.UP -> 0)
		longestDirectional += (Direction.NORTH -> 0)
		longestDirectional += (Direction.SOUTH -> 0)
		longestDirectional += (Direction.WEST -> 0)
		longestDirectional += (Direction.EAST -> 0)

		for (fieldPosition <- field) {
			if (fieldPosition.x > 0 && fieldPosition.x > longestDirectional(Direction.EAST)) {
				longestDirectional += (Direction.EAST -> fieldPosition.x)
			}
			else if (fieldPosition.x < 0 && fieldPosition.x < longestDirectional(Direction.WEST)) {
				longestDirectional += (Direction.WEST -> fieldPosition.x)
			}
			if (fieldPosition.y > 0 && fieldPosition.y > longestDirectional(Direction.UP)) {
				longestDirectional += (Direction.UP -> fieldPosition.y)
			}
			else if (fieldPosition.y < 0 && fieldPosition.y < longestDirectional(Direction.DOWN)) {
				longestDirectional += (Direction.DOWN -> fieldPosition.y)
			}
			if (fieldPosition.z > 0 && fieldPosition.z > longestDirectional(Direction.SOUTH)) {
				longestDirectional += (Direction.SOUTH -> fieldPosition.z)
			}
			else if (fieldPosition.z < 0 && fieldPosition.z < longestDirectional(Direction.NORTH)) {
				longestDirectional += (Direction.NORTH -> fieldPosition.z)
			}
		}

		return longestDirectional
	}

	override def getFortronCost(amplifier: Float): Float = super.getFortronCost(amplifier) + (super.getFortronCost(amplifier) * amplifier) / 100f
}