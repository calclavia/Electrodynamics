package resonantinduction.api.mechanical.fluid;

import net.minecraftforge.fluids.FluidTank;

/**
 * Applied to tiles that are pipes and support pressure
 * 
 * @author DarkGuardsman
 */
public interface IFluidPipe extends IFluidConnector, IPressure
{
	public FluidTank getInternalTank();

	public void onFluidChanged();

	public boolean canFlow();

	/**
	 * Max flow rate of fluid this pipe can support
	 * 
	 * @return amount in liters.
	 */
	public int getMaxFlowRate();
}
