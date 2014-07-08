package mffs.field.module

import java.util.{HashMap, HashSet, Set}

import mffs.base.ItemModule
import net.minecraftforge.common.util.ForgeDirection
import resonant.api.mffs.IFieldInteraction
import universalelectricity.core.transform.vector.Vector3

import scala.collection.JavaConversions._
import scala.collection.mutable

class ItemModuleArray extends ItemModule
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

        val directionalDisplacement = Math.abs(longestDirectional(direction)) + Math.abs(longestDirectional(direction.getOpposite)) + 1

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

  def getDirectionWidthMap(field: Set[Vector3]): mutable.Map[ForgeDirection, Integer] =
  {
    val longestDirectional= mutable.Map.empty[ForgeDirection, Integer]

    longestDirectional.put(ForgeDirection.DOWN, 0)
    longestDirectional.put(ForgeDirection.UP, 0)
    longestDirectional.put(ForgeDirection.NORTH, 0)
    longestDirectional.put(ForgeDirection.SOUTH, 0)
    longestDirectional.put(ForgeDirection.WEST, 0)
    longestDirectional.put(ForgeDirection.EAST, 0)

    for (fieldPosition <- field)
    {
      if (fieldPosition.xi > 0 && fieldPosition.xi > longestDirectional(ForgeDirection.EAST))
      {
        longestDirectional.put(ForgeDirection.EAST, fieldPosition.xi)
      }
      else if (fieldPosition.xi < 0 && fieldPosition.xi < longestDirectional(ForgeDirection.WEST))
      {
        longestDirectional.put(ForgeDirection.WEST, fieldPosition.xi)
      }
      if (fieldPosition.yi > 0 && fieldPosition.yi > longestDirectional(ForgeDirection.UP))
      {
        longestDirectional.put(ForgeDirection.UP, fieldPosition.yi)
      }
      else if (fieldPosition.yi < 0 && fieldPosition.yi < longestDirectional(ForgeDirection.DOWN))
      {
        longestDirectional.put(ForgeDirection.DOWN, fieldPosition.yi)
      }
      if (fieldPosition.zi > 0 && fieldPosition.zi > longestDirectional(ForgeDirection.SOUTH))
      {
        longestDirectional.put(ForgeDirection.SOUTH, fieldPosition.zi)
      }
      else if (fieldPosition.zi < 0 && fieldPosition.zi < longestDirectional(ForgeDirection.NORTH))
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