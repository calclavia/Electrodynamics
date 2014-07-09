package mffs.field.thread

import mffs.ModularForceFieldSystem
import mffs.base.TileFieldMatrix
import net.minecraft.tileentity.TileEntity
import universalelectricity.core.transform.rotation.Rotation
import universalelectricity.core.transform.vector.Vector3

import scala.collection.convert.wrapAll._
import scala.collection.mutable

/**
 * A thread that allows multi-threading calculation of projector fields.
 *
 * @author Calclavia
 */
class ProjectorCalculationThread(projector: TileFieldMatrix, callBack: () => Unit = null) extends AbstractFieldCalculationThread(callBack)
{
  override def run
  {
    projector.setCalculating(true)

    try
    {
      if (projector.getMode != null)
      {
        var newField = mutable.Set.empty[Vector3]

        if (projector.getModuleCount(ModularForceFieldSystem.Items.moduleInvert) > 0)
          newField = projector.getMode.getInteriorPoints(projector)
        else
          newField = projector.getMode.getExteriorPoints(projector)

        val translation = projector.getTranslation
        val rotationYaw = projector.getRotationYaw
        val rotationPitch = projector.getRotationPitch

        //TODO: Check efficiency of parallel
        projector.getModules().par foreach (_.onPreCalculate(projector, newField))

        val maxHeight = projector.world.getHeight
        val center = new Vector3(projector.asInstanceOf[TileEntity])

        newField = (newField.par map (pos => (pos.apply(new Rotation(rotationYaw, rotationPitch, 0)) + center + translation).round) filter (position => position.yi <= maxHeight && position.yi >= 0)).seq

        projector.getModules().foreach(_.onCalculate(projector, newField))

        projector.getCalculatedField.clear()
        projector.getCalculatedField.addAll(newField)
      }
    }
    catch
      {
        case e: Exception =>
        {
          e.printStackTrace
        }
      }

    projector.setCalculating(false)
    projector.setCalculated(true)

    if (callBack != null)
    {
      callBack.apply()
    }
  }
}