package mffs.api.modules;

import net.minecraft.entity.EntityLivingBase;
import calclavia.api.mffs.modules.IModule;
import calclavia.api.mffs.security.IInterdictionMatrix;

public interface IInterdictionMatrixModule extends IModule
{
	/**
	 * Called when the Interdiction Matrix attempts to defend a region.
	 * 
	 * @return True if to stop processing other modules in this list.
	 */
	public boolean onDefend(IInterdictionMatrix defenseStation, EntityLivingBase entityLiving);
}
