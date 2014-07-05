package mffs.field.module

import java.util.{HashMap, HashSet, Set}

import mffs.base.ItemModule
import net.minecraftforge.common.util.ForgeDirection
import resonant.api.mffs.IFieldInteraction
import universalelectricity.core.transform.vector.Vector3

import scala.collection.JavaConversions._

class ItemModuleArray(i: Int) extends ItemModule(i, "moduleArray")
{
	override def onPreCalculate(projector: IFieldInteraction, fieldBlocks: Set[Vector3])
	{
		onPreCalculateInterior(projector, fieldBlocks, fieldBlocks)
	}

	def onPreCalculateInterior(projector: IFieldInteraction, exterior: Set[Vector3], interior: Set[Vector3])
	{
		val originalField: Set[Vector3] = new HashSet(interior)
		val longestDirectional = getDirectionWidthMap(exterior)

		ForgeDirection.VALID_DIRECTIONS.foreach(
			direction =>
			{
				val copyAmount = projector.getSidedModuleCount(this, direction)

				val directionalDisplacement = Math.abs(longestDirectional.get(direction)) + Math.abs(longestDirectional.get(direction.getOpposite)) + 1

				(0 until copyAmount).foreach(
					i =>
					{
						val directionalDisplacementScale = directionalDisplacement * (i + 1)

						originalField.foreach(originalFieldBlock =>
						{
							val newFieldBlock: Vector3 = originalFieldBlock.clone + (new Vector3(direction) * directionalDisplacementScale)
							interior.add(newFieldBlock)
						})
					}
				)
			}
		)
	}

	def getDirectionWidthMap(field: Set[Vector3]): HashMap[ForgeDirection, Integer] =
	{
		val longestDirectional: HashMap[ForgeDirection, Integer] = new HashMap[ForgeDirection, Integer]
		longestDirectional.put(ForgeDirection.DOWN, 0)
		longestDirectional.put(ForgeDirection.UP, 0)
		longestDirectional.put(ForgeDirection.NORTH, 0)
		longestDirectional.put(ForgeDirection.SOUTH, 0)
		longestDirectional.put(ForgeDirection.WEST, 0)
		longestDirectional.put(ForgeDirection.EAST, 0)
		for (fieldPosition <- field)
		{
			if (fieldPosition.xi > 0 && fieldPosition.xi > longestDirectional.get(ForgeDirection.EAST))
			{
				longestDirectional.put(ForgeDirection.EAST, fieldPosition.xi)
			}
			else if (fieldPosition.xi < 0 && fieldPosition.xi < longestDirectional.get(ForgeDirection.WEST))
			{
				longestDirectional.put(ForgeDirection.WEST, fieldPosition.xi)
			}
			if (fieldPosition.yi > 0 && fieldPosition.yi > longestDirectional.get(ForgeDirection.UP))
			{
				longestDirectional.put(ForgeDirection.UP, fieldPosition.yi)
			}
			else if (fieldPosition.yi < 0 && fieldPosition.yi < longestDirectional.get(ForgeDirection.DOWN))
			{
				longestDirectional.put(ForgeDirection.DOWN, fieldPosition.yi)
			}
			if (fieldPosition.zi > 0 && fieldPosition.zi > longestDirectional.get(ForgeDirection.SOUTH))
			{
				longestDirectional.put(ForgeDirection.SOUTH, fieldPosition.zi)
			}
			else if (fieldPosition.zi < 0 && fieldPosition.zi < longestDirectional.get(ForgeDirection.NORTH))
			{
				longestDirectional.put(ForgeDirection.NORTH, fieldPosition.zi)
			}
		}
		return longestDirectional
	}

	override def getFortronCost(amplifier: Float): Float =
	{
		return super.getFortronCost(amplifier) + (super.getFortronCost(amplifier) * amplifier) / 100f
	}
}