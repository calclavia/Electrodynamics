package resonantinduction.core.fluid;

import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidHandler;
import resonantinduction.api.mechanical.fluid.IPressure;
import universalelectricity.api.net.IConnector;

/**
 * Applied to tiles that are pipes and support pressure
 * 
 * @author DarkGuardsman
 */
public interface IPressurizedNode extends IConnector<PressureNetwork>, IFluidHandler, IPressure
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
