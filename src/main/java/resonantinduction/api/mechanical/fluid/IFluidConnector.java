package resonantinduction.api.mechanical.fluid;

import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidHandler;
import universalelectricity.api.net.IConnector;

/**
 * Generic interface for any tile that acts as part of a fluid network. Generally network assume
 * that each part can only support one fluid tank internally
 * 
 * @author DarkGuardsman
 */
public interface IFluidConnector extends IConnector<IFluidNetwork>, IFluidHandler
{
	/** FluidTank that the network will have access to fill or drain */
	public FluidTank getInternalTank();

	public void onFluidChanged();

	public boolean canFlow();
}
