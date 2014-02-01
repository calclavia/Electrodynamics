package resonantinduction.api.fluid;

import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import universalelectricity.api.net.INodeNetwork;

/** Interface version of the fluid network.
 * 
 * @author DarkGuardsman */
public interface IFluidNetwork extends INodeNetwork<IFluidNetwork, IFluidConnector, IFluidHandler>
{
    /** Called to build the network when something changes such as addition of a pipe */
    @Override
    void reconstruct();

    /** Called to add fluid into the network
     * 
     * @param source - part that is receiving the fluid for the network
     * @param from - direction of this connection
     * @param resource - fluid stack that is being filled into the network
     * @param doFill - true causes the action to be taken, false simulates the action
     * @return amount of fluid filled into the network */
    int fill(IFluidConnector source, ForgeDirection from, FluidStack resource, boolean doFill);

    /** Called to remove fluid from a network, not supported by all networks
     * 
     * @param source - part that is receiving the fluid for the network
     * @param from - direction of this connection
     * @param resource - fluid stack that is being filled into the network
     * @param doDrain - true causes the action to be taken, false simulates the action
     * @return FluidStack that contains the fluid drained from the network */
    FluidStack drain(IFluidConnector source, ForgeDirection from, FluidStack resource, boolean doDrain);

    /** Called to remove fluid from a network, not supported by all networks
     * 
     * @param source - part that is receiving the fluid for the network
     * @param from - direction of this connection
     * @param resource - fluid stack that is being filled into the network
     * @param doDrain - true causes the action to be taken, false simulates the action
     * @return FluidStack that contains the fluid drained from the network */
    FluidStack drain(IFluidConnector source, ForgeDirection from, int resource, boolean doDrain);

    /** Fluid tank that represents the entire network */
    FluidTank getTank();

    /** Information about the network's tank */
    FluidTankInfo[] getTankInfo();

    int getPressure();

}
