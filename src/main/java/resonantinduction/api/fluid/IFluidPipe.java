package resonantinduction.api.fluid;

/** Applied to tiles that are pipes and support pressure
 * 
 * @author DarkGuardsman */
public interface IFluidPipe extends IFluidConnector, IPressureInput
{
    /** Max pressure this pipe can support */
    int getMaxPressure();

    /** Max flow rate of fluid this pipe can support */
    int getMaxFlowRate();
}
