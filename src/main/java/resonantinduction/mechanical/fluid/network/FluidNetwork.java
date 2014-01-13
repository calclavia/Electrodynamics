package resonantinduction.mechanical.fluid.network;

import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import resonantinduction.api.fluid.IFluidNetwork;
import resonantinduction.api.fluid.IFluidPart;
import universalelectricity.api.net.IConnector;
import universalelectricity.core.net.ConnectionPathfinder;
import universalelectricity.core.net.Network;

public class FluidNetwork extends Network<IFluidNetwork, IFluidPart, IFluidHandler> implements IFluidNetwork
{
    protected FluidTank tank;
    protected final FluidTankInfo[] tankInfo = new FluidTankInfo[1];

    public FluidNetwork()
    {

    }

    public FluidNetwork(IFluidPart... parts)
    {
        for (IFluidPart part : parts)
        {
            this.addConnector(part);
        }
    }

    @Override
    public void reconstruct()
    {
        // TODO Auto-generated method stub

    }

    public void rebuildTank()
    {
        if (this.getTank() != null)
        {
            this.tankInfo[0] = this.getTank().getInfo();
        }
        else
        {
            this.tankInfo[0] = null;
        }
    }

    @Override
    public int fill(IFluidPart source, ForgeDirection from, FluidStack resource, boolean doFill)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public FluidStack drain(IFluidPart source, ForgeDirection from, FluidStack resource, boolean doDrain)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FluidStack drain(IFluidPart source, ForgeDirection from, int resource, boolean doDrain)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean canUpdate()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean continueUpdate()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void update()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public IFluidNetwork merge(IFluidNetwork network)
    {
        FluidNetwork newNetwork = null;
        if (network != null && network.getClass().equals(this.getClass()) && network != this)
        {

            try
            {
                newNetwork = this.getClass().newInstance();

                newNetwork.getConnectors().addAll(this.getConnectors());
                newNetwork.getConnectors().addAll(network.getConnectors());

                network.getConnectors().clear();
                network.getNodes().clear();
                this.getConnectors().clear();
                this.getNodes().clear();

                newNetwork.reconstruct();

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        }
        return newNetwork;
    }

    @Override
    public void split(IFluidPart splitPoint)
    {
        this.removeConnector(splitPoint);
        this.reconstruct();

        /** Loop through the connected blocks and attempt to see if there are connections between the
         * two points elsewhere. */
        Object[] connectedBlocks = splitPoint.getConnections();

        for (int i = 0; i < connectedBlocks.length; i++)
        {
            Object connectedBlockA = connectedBlocks[i];

            if (connectedBlockA instanceof IFluidPart)
            {
                for (int ii = 0; ii < connectedBlocks.length; ii++)
                {
                    final Object connectedBlockB = connectedBlocks[ii];

                    if (connectedBlockA != connectedBlockB && connectedBlockB instanceof IFluidPart)
                    {
                        ConnectionPathfinder finder = new ConnectionPathfinder((IFluidPart) connectedBlockB, splitPoint);
                        finder.findNodes((IFluidPart) connectedBlockA);

                        if (finder.results.size() <= 0)
                        {
                            try
                            {
                                /** The connections A and B are not connected anymore. Give them both
                                 * a new common network. */
                                IFluidNetwork newNetwork = this.getClass().newInstance();

                                for (IConnector node : finder.closedSet)
                                {
                                    if (node != splitPoint && node instanceof IFluidPart)
                                    {
                                        newNetwork.addConnector((IFluidPart) node);
                                    }
                                }
                                newNetwork.reconstruct();
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }

                        }
                    }
                }
            }
        }
    }

    @Override
    public void split(IFluidPart connectorA, IFluidPart connectorB)
    {
        this.reconstruct();

        /** Check if connectorA connects with connectorB. */
        ConnectionPathfinder finder = new ConnectionPathfinder(connectorB);
        finder.findNodes(connectorA);

        if (finder.results.size() <= 0)
        {
            /** The connections A and B are not connected anymore. Give them both a new common
             * network. */
            IFluidNetwork newNetwork;
            try
            {
                newNetwork = this.getClass().newInstance();

                for (IConnector node : finder.closedSet)
                {
                    if (node instanceof IFluidPart)
                    {
                        newNetwork.addConnector((IFluidPart) node);
                    }
                }

                newNetwork.reconstruct();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public FluidTank getTank()
    {
        return this.tank;
    }

    @Override
    public FluidTankInfo[] getTankInfo()
    {
        return tankInfo;
    }

}
