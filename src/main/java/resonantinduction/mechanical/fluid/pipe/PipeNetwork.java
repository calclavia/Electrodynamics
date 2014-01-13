package resonantinduction.mechanical.fluid.pipe;

import resonantinduction.api.fluid.IFluidPipe;
import resonantinduction.mechanical.fluid.network.FluidNetwork;

/** @author DarkGuardsman */
public class PipeNetwork extends FluidNetwork
{
    //TODO implements pressure for future hydraulic machines
    
    public PipeNetwork(IFluidPipe... pipes)
    {
        super(pipes);
    }
}
