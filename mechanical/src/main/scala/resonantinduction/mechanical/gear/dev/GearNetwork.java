package resonantinduction.mechanical.gear.dev;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;

import net.minecraftforge.common.ForgeDirection;
import universalelectricity.api.net.IUpdate;
import universalelectricity.api.vector.Vector3;
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

    public GearNetwork()
    {
        doRemap = true;
    }

    public GearNetwork(NodeGear nodeGear)
    {
        this();
        onAdded(nodeGear);
    }

    /** Called by a gear when its added to the world */
    public void onAdded(NodeGear gear)
    {
        onAdded(gear, true);
    }

    public void onAdded(NodeGear gear, boolean checkMerge)
    {
        if (gear instanceof NodeGenerator)
        {
            if (!generators.contains(gear))
            {
                //TODO set gear's network
                generators.add((NodeGenerator) gear);
                if (checkMerge)
                    checkForMerge(gear);
                doRemap = true;
            }
        }
        else
        {
            if (!nodes.contains(gear))
            {
                nodes.add(gear);
                if (checkMerge)
                    checkForMerge(gear);
                doRemap = true;
            }
        }
    }

    public void checkForMerge(NodeGear gear)
    {

    }

    /** Called by a gear when its removed from the world */
    public void onRemoved(NodeGear gear, boolean doSplit)
    {
        if (generators.remove(gear) || nodes.remove(gear))
        {
            if (doSplit)
                this.split(gear);
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
            //Rebuilds the network using a set of path finders
            LinkedHashSet<Vector3> nodes = new LinkedHashSet<Vector3>();
            
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

    /** Called to do a path finding check too see if the network needs to be split into two or more
     * networks. Does up to 6 path finding checks, and stops when it finds a connection back to one
     * of the sub networks. */
    protected void split(NodeGear splitPoint)
    {
        /** Loop through the connected blocks and attempt to see if there are connections between the
         * two points elsewhere. */
        WeakHashMap<NodeGear, ForgeDirection> connectedBlocks = splitPoint.connections;

        for (Entry<NodeGear, ForgeDirection> entry : connectedBlocks.entrySet())
        {
            final NodeGear connectedA = entry.getKey();

            if (connectedA != null)
            {
                for (Entry<NodeGear, ForgeDirection> entry2 : connectedBlocks.entrySet())
                {
                    final NodeGear connectedB = entry2.getKey();

                    if (connectedB != null && connectedA != connectedB)
                    {
                        ConnectionPathfinder<NodeGear> finder = new ConnectionPathfinder<NodeGear>(NodeGear.class, connectedB, splitPoint);
                        finder.findNodes(connectedA);

                        if (finder.results.size() <= 0)
                        {
                            try
                            {
                                /** The connections A and B are not connected anymore. Give them both
                                 * a new common network. */
                                GearNetwork newNetwork = new GearNetwork();

                                for (NodeGear node : finder.closedSet)
                                {
                                    if (node != splitPoint)
                                    {
                                        newNetwork.onAdded(node, false);
                                        onRemoved(node, false);
                                    }
                                }
                                newNetwork.doRemap = true;
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

    /** Called to rebuild the network */
    protected void reconstruct()
    {
        for(NodeGear gear : nodes)
        {
            gear.reconstruct();
        }
        for(NodeGenerator gen : generators)
        {
            gen.reconstruct();
        }
    }

    /** Called to destroy or rather clean up the network. Make sure to do your cleanup in this
     * method. */
    protected void deconstruct()
    {
        this.nodes.clear();
        this.generators.clear();
        this.isDead = true;
        for(NodeGear gear : nodes)
        {
            gear.deconstruct();
        }
        for(NodeGenerator gen : generators)
        {
            gen.deconstruct();
        }
    }

}
