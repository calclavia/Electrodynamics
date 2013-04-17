package mffs.tileentity;

import java.util.HashSet;
import java.util.Set;

import mffs.api.modules.IModule;
import net.minecraft.util.MathHelper;
import universalelectricity.core.vector.Vector3;
import calclavia.lib.CalculationHelper;

/**
 * A thread that allows multi-threading calculation of projector fields.
 * 
 * @author Calclavia
 * 
 */
public class ProjectorCalculationThread extends Thread
{
	public interface IThreadCallBack
	{
		/**
		 * Called when the thread finishes the calculation.
		 */
		public void onThreadComplete();
	}

	private TileEntityForceFieldProjector projector;
	private IThreadCallBack callBack;

	public ProjectorCalculationThread(TileEntityForceFieldProjector projector)
	{
		this.projector = projector;
	}

	public ProjectorCalculationThread(TileEntityForceFieldProjector projector, IThreadCallBack callBack)
	{
		this(projector);
		this.callBack = callBack;
	}

	public void run()
	{
		this.projector.isCalculating = true;

		Set<Vector3> newField = new HashSet<Vector3>();

		this.projector.getMode().calculateField(this.projector, newField);

		Vector3 translation = this.projector.getTranslation();
		int rotationYaw = (int) MathHelper.wrapAngleTo180_float(this.projector.getRotationYaw());
		int rotationPitch = (int) MathHelper.wrapAngleTo180_float(this.projector.getRotationPitch());

		for (Vector3 position : newField)
		{
			if (rotationYaw != 0)
			{
				CalculationHelper.rotateXZByAngle(position, rotationYaw);
			}

			if (rotationPitch != 0)
			{
				CalculationHelper.rotateYByAngle(position, rotationPitch);
			}

			position.add(new Vector3(this.projector));
			position.add(translation);

			if (position.intY() <= this.projector.worldObj.getHeight())
			{
				this.projector.getCalculatedField().add(position);
			}
		}

		for (IModule module : this.projector.getModules(this.projector.getModuleSlots()))
		{
			module.onCalculate(this.projector, this.projector.getCalculatedField());
		}

		this.projector.isCalculating = false;
		this.projector.isCalculated = true;

		if (this.callBack != null)
		{
			this.callBack.onThreadComplete();
		}
	}
}