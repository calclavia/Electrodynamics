package mffs.tile

import calclavia.api.mffs.IFieldInteraction
import calclavia.api.mffs.fortron.IServerThread
import mffs.ModularForceFieldSystem
import net.minecraft.tileentity.TileEntity
import universalelectricity.api.vector.Vector3
import java.util.Set
import scala.collection.convert.wrapAll._

/**
 * A thread that allows multi-threading calculation of projector fields.
 *
 * @author Calclavia
 */
class ProjectorCalculationThread(projector: IFieldInteraction) extends Thread with IServerThread
{
	private var callBack: IThreadCallBack = null

	def this(projector: IFieldInteraction, callBack: IThreadCallBack)
	{
		this(projector)
		this.callBack = callBack
	}

	override def run
	{
		projector.setCalculating(true)

		try
		{
			if (projector.getMode != null)
			{
				var newField: Set[Vector3] = null

				if (projector.getModuleCount(ModularForceFieldSystem.itemModuleInvert) > 0)
					newField = projector.getMode.getInteriorPoints(projector)
				else
					newField = projector.getMode.getExteriorPoints(projector)

				val translation = projector.getTranslation()
				val rotationYaw = projector.getRotationYaw()
				val rotationPitch = projector.getRotationPitch()

				val t = System.currentTimeMillis()

				projector.getModules().foreach(_.onPreCalculate(projector, newField))

				val maxHeight = projector.asInstanceOf[TileEntity].worldObj.getHeight()
				val center = new Vector3(this.projector.asInstanceOf[TileEntity])

				newField.par.map(
					position =>
					{
						if (rotationYaw != 0 || rotationPitch != 0)
							position.rotate(rotationYaw, rotationPitch)

						position.translate(center)
						position.translate(translation)
						position.round()
					}
				).filter(position => position.intY <= maxHeight && position.intY >= 0)

				projector.getModules().foreach(_.onCalculate(projector, newField))

				projector.getCalculatedField().addAll(newField)
				println("T: " + (System.currentTimeMillis() - t) + " : " + newField.size())
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
			callBack.onThreadComplete
		}
	}
}