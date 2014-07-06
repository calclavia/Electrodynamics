package mffs.field.thread

import mffs.mobilize.TileForceMobilizer

import scala.collection.convert.wrapAll._

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

    if (manipulator.canMove)
    {
      manipulator.manipulationVectors = manipulator.getInteriorPoints.toSet
    }
    else
    {
      manipulator.markFailMove = true
    }

    manipulator.isCalculatingManipulation = false

    if (callBack != null)
    {
      callBack.apply()
    }
  }

}