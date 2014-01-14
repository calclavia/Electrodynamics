package resonantinduction.mechanical.network;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.minecraftforge.common.ForgeDirection;
import universalelectricity.api.CompatibilityModule;
import universalelectricity.api.energy.IConductor;
import universalelectricity.api.net.IConnector;
import universalelectricity.core.net.ConnectionPathfinder;
import universalelectricity.core.net.Network;
import universalelectricity.core.net.NetworkTickHandler;

/** Simple network to translate speed and force using mechanical rotation
 * 
 * @author DarkGuardsman */
public class MechNetwork extends Network<IMechNetwork, IMechConnector, IMechMachine> implements IMechNetwork
{
    private int force = 0;
    private int speed = 0;
    private int resistance = 0;

    private HashMap<IMechMachine, ForceWrapper[]> forceMap = new HashMap<IMechMachine, ForceWrapper[]>();

    @Override
    public void update()
    {

    }

    @Override
    public void reconstruct()
    {
        if (this.getConnectors().size() > 0)
        {
            // Reset all values related to wires
            this.getNodes().clear();
            this.forceMap.clear();
            this.resistance = 0;
            this.force = 0;
            this.speed = 0;

            // Iterate threw list of wires
            Iterator<IMechConnector> it = this.getConnectors().iterator();

            while (it.hasNext())
            {
                IMechConnector conductor = it.next();

                if (conductor != null)
                {
                    this.reconstructConductor(conductor);
                }
                else
                {
                    it.remove();
                }
            }

            if (this.getNodes().size() > 0)
            {
                NetworkTickHandler.addNetwork(this);
            }
        }
    }

    /** Segmented out call so overriding can be done when conductors are reconstructed. */
    protected void reconstructConductor(IMechConnector conductor)
    {
        conductor.setNetwork(this);

        for (int i = 0; i < conductor.getConnections().length; i++)
        {
            reconstructHandler(conductor.getConnections()[i], ForgeDirection.getOrientation(i).getOpposite());
        }

        this.resistance += conductor.getResistance();
    }

    /** Segmented out call so overriding can be done when machines are reconstructed. */
    protected void reconstructHandler(Object obj, ForgeDirection side)
    {
        if (obj != null && !(obj instanceof IMechConnector))
        {
            if (obj instanceof IMechMachine)
            {
                ForceWrapper[] set = this.forceMap.get((IMechMachine)obj);
                if (set == null)
                {
                    set = new ForceWrapper[6];
                }
                this.getNodes().add((IMechMachine) obj);
                set[side.ordinal()] = new ForceWrapper(((IMechMachine) obj).getForceSide(side.getOpposite()),((IMechMachine) obj).getForceSide(side.getOpposite()));
                this.forceMap.put((IMechMachine) obj, set);
            }
        }
    }

    @Override
    public boolean canUpdate()
    {
        return true;
    }

    @Override
    public boolean continueUpdate()
    {
        return true;
    }

    @Override
    public IMechNetwork merge(IMechNetwork network)
    {
        if (network.getClass().isAssignableFrom(this.getClass()) && network != this)
        {
            MechNetwork newNetwork = new MechNetwork();
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
    public void split(IMechConnector splitPoint)
    {
        this.removeConnector(splitPoint);
        this.reconstruct();

        /** Loop through the connected blocks and attempt to see if there are connections between the
         * two points elsewhere. */
        Object[] connectedBlocks = splitPoint.getConnections();

        for (int i = 0; i < connectedBlocks.length; i++)
        {
            Object connectedBlockA = connectedBlocks[i];

            if (connectedBlockA instanceof IMechConnector)
            {
                for (int ii = 0; ii < connectedBlocks.length; ii++)
                {
                    final Object connectedBlockB = connectedBlocks[ii];

                    if (connectedBlockA != connectedBlockB && connectedBlockB instanceof IMechConnector)
                    {
                        ConnectionPathfinder finder = new ConnectionPathfinder((IConnector) connectedBlockB, splitPoint);
                        finder.findNodes((IConnector) connectedBlockA);

                        if (finder.results.size() <= 0)
                        {
                            try
                            {
                                /** The connections A and B are not connected anymore. Give them both
                                 * a new common network. */
                                IMechNetwork newNetwork = new MechNetwork();
                                for (IConnector node : finder.closedSet)
                                {
                                    if (node != splitPoint && node instanceof IMechConnector)
                                    {
                                        newNetwork.addConnector((IMechConnector) node);
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
    public void split(IMechConnector connectorA, IMechConnector connectorB)
    {
        this.reconstruct();

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
    public int getTorque()
    {
        return this.force;
    }

    @Override
    public int getRotationPerTick()
    {
        return this.speed;
    }

    public static class ForceWrapper
    {
        public int force = 0;
        public int speed = 0;

        public ForceWrapper(int force, int speed)
        {
            this.force = force;
            this.speed = speed;
        }
    }

}
