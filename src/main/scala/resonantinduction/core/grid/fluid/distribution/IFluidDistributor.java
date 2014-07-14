package resonantinduction.core.grid.fluid.distribution;

import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidHandler;
import universalelectricity.api.core.grid.INodeProvider;

/**
 * Generic interface for any tile that acts as part of a fluid network. Generally network assume
 * that each part can only support one fluid tank internally
 *
 * @author DarkGuardsman
 */
public interface IFluidDistributor extends INodeProvider, IFluidHandler
{
	/**
	 * FluidTank that the network will have access to fill or drain
	 */
	public FluidTank getInternalTank();

	public void onFluidChanged();
}
