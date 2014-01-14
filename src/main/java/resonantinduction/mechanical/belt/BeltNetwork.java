package resonantinduction.mechanical.belt;

import net.minecraft.tileentity.TileEntity;
import resonantinduction.api.IBelt;
import resonantinduction.api.IBeltNetwork;
import resonantinduction.mechanical.network.IMechConnector;
import resonantinduction.mechanical.network.IMechNetwork;
import resonantinduction.mechanical.network.MechNetwork;
import universalelectricity.api.net.IConnector;
import universalelectricity.core.net.ConnectionPathfinder;
import universalelectricity.core.net.Network;

/** Network used to update belts in a uniform way
 * 
 * @author DarkGuardsman */
public class BeltNetwork extends Network<IBeltNetwork, IBelt, TileEntity> implements IBeltNetwork
{

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
    public IBeltNetwork merge(IBeltNetwork network)
    {
        if (network.getClass().isAssignableFrom(this.getClass()) && network != this)
        {
            BeltNetwork newNetwork = new BeltNetwork();
            newNetwork.getConnectors().addAll(this.getConnectors());
            newNetwork.getConnectors().addAll(network.getConnectors());

            network.getConnectors().clear();
            network.getNodes().clear();
            this.getConnectors().clear();
            this.getNodes().clear();

            newNetwork.reconstruct();
            return newNetwork;
        }

        return null;
    }

    @Override
    public void split(IBelt splitPoint)
    {
        this.removeConnector(splitPoint);
        this.reconstruct();

        /** Loop through the connected blocks and attempt to see if there are connections between the
         * two points elsewhere. */
        Object[] connectedBlocks = splitPoint.getConnections();

        for (int i = 0; i < connectedBlocks.length; i++)
        {
            Object connectedBlockA = connectedBlocks[i];

            if (connectedBlockA instanceof IBelt)
            {
                for (int ii = 0; ii < connectedBlocks.length; ii++)
                {
                    final Object connectedBlockB = connectedBlocks[ii];

                    if (connectedBlockA != connectedBlockB && connectedBlockB instanceof IBelt)
                    {
                        ConnectionPathfinder finder = new ConnectionPathfinder((IConnector) connectedBlockB, splitPoint);
                        finder.findNodes((IConnector) connectedBlockA);

                        if (finder.results.size() <= 0)
                        {
                            try
                            {
                                /** The connections A and B are not connected anymore. Give them both
                                 * a new common network. */
                                IBeltNetwork newNetwork = new BeltNetwork();
                                for (IConnector node : finder.closedSet)
                                {
                                    if (node != splitPoint && node instanceof IBelt)
                                    {
                                        newNetwork.addConnector((IBelt) node);
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
    public void split(IBelt connectorA, IBelt connectorB)
    {
        /** Check if connectorA connects with connectorB. */
        ConnectionPathfinder finder = new ConnectionPathfinder(connectorB);
        finder.findNodes(connectorA);

        if (finder.results.size() <= 0)
        {
            /** The connections A and B are not connected anymore. Give them both a new common
             * network. */
            IMechNetwork newNetwork = new MechNetwork();

            for (IConnector node : finder.closedSet)
            {
                if (node instanceof IMechConnector)
                {
                    newNetwork.addConnector((IMechConnector) node);
                }
            }

            newNetwork.reconstruct();
        }
    }

    @Override
    public int frame()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float speed()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void reconstruct()
    {
        // TODO Auto-generated method stub

    }

}
