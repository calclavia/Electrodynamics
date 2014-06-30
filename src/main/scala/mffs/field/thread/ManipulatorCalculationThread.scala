package mffs.field.thread

import java.util.HashSet

import mffs.mobilize.TileForceMobilizer
import universalelectricity.core.transform.vector.Vector3

import scala.collection.JavaConversions._

/**
 * A thread that allows multi-threading calculation of projector fields.
 *
 * @author Calclavia
 *
 */
class ManipulatorCalculationThread(manipulator: TileForceMobilizer, callBack: () => Unit = null) extends AbstractFieldCalculationThread(callBack)
{
  override def run
  {
    manipulator.isCalculatingManipulation = true

    try
    {
      val mobilizationPoints = this.manipulator.getInteriorPoints

      if (manipulator.canMove())
      {
        manipulator.manipulationVectors = new HashSet[Vector3]
        mobilizationPoints.foreach(pos => manipulator.manipulationVectors.add(pos.clone()))
      }
      else
      {
        manipulator.markFailMove = true
      }
    }
    catch
      {
        case e: Exception =>
        {
          e.printStackTrace
        }
      }

    manipulator.isCalculatingManipulation = false

    if (callBack != null)
    {
      callBack.apply()
    }
  }

}