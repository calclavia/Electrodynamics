package resonantinduction.mechanical.gear.dev;

import java.util.HashMap;
import java.util.WeakHashMap;

import net.minecraftforge.common.ForgeDirection;
import resonant.api.grid.INode;
import resonant.api.grid.INodeProvider;
import resonant.lib.type.Pair;
import universalelectricity.api.net.IConnector;

/** Applied to any device that acts as part of a gear network using or supplying rotational force
 * 
 * @author Darkguardsman */
public class NodeGear implements INode, IConnector<GearNetwork>
{
    /** Used by the gear network to track the rotation effect each generator has on this gear */
    public HashMap<NodeGenerator, Pair<Boolean, Float>> rotationEffectMap = new HashMap<NodeGenerator, Pair<Boolean, Float>>();
    protected WeakHashMap<NodeGear, ForgeDirection> connections = new WeakHashMap<NodeGear, ForgeDirection>();

    /** Speed by which this gear rotates */
    protected float rotationSpeed = 0;
    /** Force carried by this gear */
    protected float force = 0;
    /** Flag if the gear is rotating clockwise */
    protected boolean clockwise = false;
    /** Network this gear is part of */
    protected GearNetwork network;

    @Override
    public void update(float deltaTime)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void reconstruct()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void deconstruct()
    {
        this.rotationEffectMap.clear();
        this.connections.clear();

    }

    @Override
    public void recache()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public GearNetwork getNetwork()
    {
        if (network == null)
        {
            network = new GearNetwork(this);
        }
        return network;
    }

    @Override
    public void setNetwork(GearNetwork network)
    {
        this.network = network;
    }

    @Override
    public boolean canConnect(ForgeDirection from, Object source)
    {
        return source instanceof NodeGear || source instanceof INodeProvider && ((INodeProvider) source).getNode(NodeGear.class, from) != null;
    }

    @Override
    public Object[] getConnections()
    {
        return null;
    }

    @Override
    public IConnector<GearNetwork> getInstance(ForgeDirection dir)
    {
        return this;
    }

}
