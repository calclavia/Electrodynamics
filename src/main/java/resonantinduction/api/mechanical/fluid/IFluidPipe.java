package resonantinduction.api.mechanical.fluid;

import net.minecraftforge.common.ForgeDirection;

/**
 * Applied to tiles that are pipes and support pressure
 * 
 * @author DarkGuardsman
 */
public interface IFluidPipe extends IFluidConnector, IPressureInput
{

	/**
	 * Max flow rate of fluid this pipe can support
	 * 
	 * @return amount in liters.
	 */
	int getMaxFlowRate();

}
