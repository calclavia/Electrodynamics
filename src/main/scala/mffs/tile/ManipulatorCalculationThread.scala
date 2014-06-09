package mffs.tile

import java.util.HashSet
import universalelectricity.api.vector.Vector3
import calclavia.api.mffs.fortron.IServerThread
import scala.collection.JavaConversions._

/**
 * A thread that allows multi-threading calculation of projector fields.
 *
 * @author Calclavia
 *
 */
class ManipulatorCalculationThread(manipulator: TileForceManipulator) extends Thread with IServerThread
{
	private var callBack: IThreadCallBack = null

	def this(manipulator: TileForceManipulator, callBack: IThreadCallBack)
	{
		this(manipulator)
		this.callBack = callBack
	}

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
			callBack.onThreadComplete
		}
	}

}