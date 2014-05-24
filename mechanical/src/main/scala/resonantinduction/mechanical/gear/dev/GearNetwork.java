package resonantinduction.mechanical.gear.dev;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import universalelectricity.api.net.IUpdate;
import universalelectricity.core.net.ConnectionPathfinder;

/** Network type only used by gear related mechanical machines. Any node that uses this network needs
 * to call onAdded() and onRemoved(). This way the network knows when to handle remapping of all
 * connections.
 * 
 * @author Darkguardsman */
public class GearNetwork implements IUpdate
{
    /** Map of gears and how they related to each generator */
    private final Set<NodeGear> nodes = Collections.newSetFromMap(new WeakHashMap<NodeGear, Boolean>());
    private final Set<NodeGenerator> generators = Collections.newSetFromMap(new WeakHashMap<NodeGenerator, Boolean>());
    private boolean doRemap = false;
    private boolean isDead = false;

    /** Called by a gear when its added to the world */
    public void onAdded(NodeGear gear)
    {
        if (gear instanceof NodeGenerator)
        {
            if (!generators.contains(gear))
            {
                generators.add((NodeGenerator) gear);
                doRemap = true;
            }
        }
        else
        {
            if (!nodes.contains(gear))
            {
                nodes.add(gear);
                doRemap = true;
            }
        }
    }

    /** Called by a gear when its removed from the world */
    public void onRemoved(NodeGear gear)
    {
        if (generators.remove(gear) || nodes.remove(gear))
        {
            doRemap = true;
        }
    }

    @Override
    public void update()
    {
        //If remap flag was set make sure to remap before doing anything else
        if (doRemap)
        {
            doRemap = false;
        }

    }

    @Override
    public boolean canUpdate()
    {
        return !isDead;
    }

    @Override
    public boolean continueUpdate()
    {
        return canUpdate();
    }

    /** Called to merge this network with another network. Creates a new network in the process and
     * empties the old networks */
    public GearNetwork merge(GearNetwork network)
    {
        if (network != null && network.getClass().isAssignableFrom(getClass()) && network != this)
        {
            synchronized (this)
            {
                GearNetwork newNetwork = new GearNetwork();
                //Add all nodes to new network from old ones
                newNetwork.nodes.addAll(nodes);
                newNetwork.nodes.addAll(network.nodes);
                //Add all generator nodes
                newNetwork.generators.addAll(generators);
                newNetwork.generators.addAll(network.generators);

                //Destroy the old networks
                network.deconstruct();
                deconstruct();

                newNetwork.doRemap = true;
                return newNetwork;
            }
        }

        return null;
    }

    public void split(NodeGear splitPoint)
    {
        /** Loop through the connected blocks and attempt to see if there are connections between the
         * two points elsewhere. */
        Object[] connectedBlocks = getConnectionsFor(splitPoint);
        removeConnector(splitPoint);

        for (int i = 0; i < connectedBlocks.length; i++)
        {
            Object connectedA = connectedBlocks[i];

            if (connectedA != null && isValidConnector(connectedA))
            {
                for (int ii = 0; ii < connectedBlocks.length; ii++)
                {
                    final Object connectedB = connectedBlocks[ii];

                    if (connectedB != null && connectedA != connectedB && isValidConnector(connectedB))
                    {
                        ConnectionPathfinder<C> finder = new ConnectionPathfinder<C>(getConnectorClass(), (C) connectedB, splitPoint);
                        finder.findNodes((C) connectedA);

                        if (finder.results.size() <= 0)
                        {
                            try
                            {
                                /** The connections A and B are not connected anymore. Give them both
                                 * a new common network. */
                                N newNetwork = newInstance();

                                for (C node : finder.closedSet)
                                {
                                    if (node != splitPoint)
                                    {
                                        newNetwork.addConnector(node);
                                        removeConnector(node);
                                        onSplit(newNetwork);
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

        reconstruct();
    }

    /** Called to rebuild the network */
    protected void reconstruct()
    {

    }

    /** Called to destroy or rather clean up the network. Make sure to do your cleanup in this
     * method. */
    protected void deconstruct()
    {
        this.nodes.clear();
        this.generators.clear();
        this.isDead = true;
    }

}
