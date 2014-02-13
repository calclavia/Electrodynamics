package resonantinduction.api.mechanical.fluid;

/**
 * Applied to tiles that are pipes and support pressure
 * 
 * @author DarkGuardsman
 */
public interface IFluidPipe extends IFluidConnector, IPressureInput
{
	/**
	 * Max pressure this pipe can support.
	 * 
	 * @return amount in pascals.
	 */
	int getMaxPressure();

	int getPressure();

	/**
	 * Max flow rate of fluid this pipe can support
	 * 
	 * @return amount in liters.
	 */
	int getMaxFlowRate();
}
