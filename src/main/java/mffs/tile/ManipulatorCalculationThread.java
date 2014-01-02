package mffs.tile;

import java.util.HashSet;
import java.util.Set;

import universalelectricity.api.vector.Vector3;

/**
 * A thread that allows multi-threading calculation of projector fields.
 * 
 * @author Calclavia
 * 
 */
public class ManipulatorCalculationThread extends Thread
{
	public interface IThreadCallBack
	{
		/**
		 * Called when the thread finishes the calculation.
		 */
		public void onThreadComplete();
	}

	private TileForceManipulator manipulator;
	private IThreadCallBack callBack;

	public ManipulatorCalculationThread(TileForceManipulator projector)
	{
		this.manipulator = projector;
	}

	public ManipulatorCalculationThread(TileForceManipulator projector, IThreadCallBack callBack)
	{
		this(projector);
		this.callBack = callBack;
	}

	@Override
	public void run()
	{
		this.manipulator.isCalculatingManipulation = true;

		try
		{
			/**
			 * Move
			 */
			Set<Vector3> mobilizationPoints = this.manipulator.getInteriorPoints();

			if (this.manipulator.canMove())
			{
				this.manipulator.manipulationVectors = new HashSet<Vector3>();

				for (Vector3 position : mobilizationPoints)
				{
					this.manipulator.manipulationVectors.add(position.clone());
				}
			}
			else
			{
				this.manipulator.markFailMove = true;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		this.manipulator.isCalculatingManipulation = false;

		if (this.callBack != null)
		{
			this.callBack.onThreadComplete();
		}

	}
}