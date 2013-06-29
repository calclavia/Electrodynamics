package mffs.tileentity;

import java.util.Set;

import mffs.ModularForceFieldSystem;
import mffs.api.IFieldInteraction;
import mffs.api.modules.IModule;
import net.minecraft.tileentity.TileEntity;
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

	private IFieldInteraction projector;
	private IThreadCallBack callBack;

	public ProjectorCalculationThread(IFieldInteraction projector)
	{
		this.projector = projector;
	}

	public ProjectorCalculationThread(IFieldInteraction projector, IThreadCallBack callBack)
	{
		this(projector);
		this.callBack = callBack;
	}

	@Override
	public void run()
	{
		this.projector.setCalculating(true);

		try
		{
			if (this.projector.getMode() != null)
			{
				Set<Vector3> newField;

				if (this.projector.getModuleCount(ModularForceFieldSystem.itemModuleInvert) > 0)
				{
					newField = this.projector.getMode().getInteriorPoints(this.projector);
				}
				else
				{
					newField = this.projector.getMode().getExteriorPoints(this.projector);
				}

				Vector3 translation = this.projector.getTranslation();
				int rotationYaw = this.projector.getRotationYaw();
				int rotationPitch = this.projector.getRotationPitch();

				for (Vector3 position : newField)
				{
					if (rotationYaw != 0 || rotationPitch != 0)
					{
						CalculationHelper.rotateByAngle(position, rotationYaw, rotationPitch);
					}

					position.add(new Vector3((TileEntity) this.projector));
					position.add(translation);

					if (position.intY() <= ((TileEntity) this.projector).worldObj.getHeight())
					{
						this.projector.getCalculatedField().add(position.round());
					}
				}

				for (IModule module : this.projector.getModules(this.projector.getModuleSlots()))
				{
					module.onCalculate(this.projector, this.projector.getCalculatedField());
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		this.projector.setCalculating(false);
		this.projector.setCalculated(true);

		if (this.callBack != null)
		{
			this.callBack.onThreadComplete();
		}
	}
}