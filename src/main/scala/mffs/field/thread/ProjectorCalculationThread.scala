package mffs.field.thread

import java.util.Set

import mffs.ModularForceFieldSystem
import net.minecraft.tileentity.TileEntity
import resonant.api.mffs.IFieldInteraction
import universalelectricity.core.transform.rotation.Rotation
import universalelectricity.core.transform.vector.Vector3

import scala.collection.convert.wrapAll._

/**
 * A thread that allows multi-threading calculation of projector fields.
 *
 * @author Calclavia
 */
class ProjectorCalculationThread(projector: IFieldInteraction, callBack: () => Unit = null) extends AbstractFieldCalculationThread(callBack)
{
  override def run
  {
    projector.setCalculating(true)

    try
    {
      if (projector.getMode != null)
      {
        var newField: Set[Vector3] = null

        if (projector.getModuleCount(ModularForceFieldSystem.Items.moduleInvert) > 0)
          newField = projector.getMode.getInteriorPoints(projector)
        else
          newField = projector.getMode.getExteriorPoints(projector)

        val translation = projector.getTranslation
        val rotationYaw = projector.getRotationYaw
        val rotationPitch = projector.getRotationPitch

        //TODO: Check efficiency of parallel
        projector.getModules().par foreach (_.onPreCalculate(projector, newField))

        val maxHeight = projector.asInstanceOf[TileEntity].getWorldObj.getHeight
        val center = new Vector3(projector.asInstanceOf[TileEntity])

        newField = newField.par map (pos => (pos.apply(new Rotation(rotationYaw, rotationPitch, 0)) + center + translation).round) filter (position => position.yi <= maxHeight && position.yi >= 0)

        projector.getModules().foreach(_.onCalculate(projector, newField))

        projector.getCalculatedField().clear()
        projector.getCalculatedField().addAll(newField)
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