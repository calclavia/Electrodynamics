package resonantinduction.mechanical.gear.dev;

import java.util.HashMap;

import resonant.api.grid.INode;
import resonant.lib.type.Pair;

/** Applied to any device that acts as part of a gear network using or supplying rotational force
 * 
 * @author Darkguardsman */
public class NodeGear implements INode
{
    /** Used by the gear network to track the rotation effect each generator has on this gear */
    public HashMap<NodeGenerator, Pair<Boolean, Float>> rotationEffectMap = new HashMap<NodeGenerator, Pair<Boolean, Float>>();
    
    /** Speed by which this gear rotates */
    protected float rotationSpeed = 0;
    /** Force carried by this gear */
    protected float force = 0;
    /** Flag if the gear is rotating clockwise */
    protected boolean clockwise = false;

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

    }

    @Override
    public void recache()
    {
        // TODO Auto-generated method stub

    }

}
