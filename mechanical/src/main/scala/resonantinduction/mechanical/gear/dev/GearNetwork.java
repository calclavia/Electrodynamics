package resonantinduction.mechanical.gear.dev;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import universalelectricity.api.net.IUpdate;

/** Network type only used by gear related mechanical machines
 * 
 * @author Darkguardsman */
public class GearNetwork implements IUpdate
{
    /** Map of gears and how they related to each generator */
    private final Set<NodeGear> nodes = Collections.newSetFromMap(new WeakHashMap<NodeGear, Boolean>());
    private final Set<NodeGenerator> generators = Collections.newSetFromMap(new WeakHashMap<NodeGenerator, Boolean>());
    private boolean doRemap = false;

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
        return true;
    }

    @Override
    public boolean continueUpdate()
    {
        return true;
    }

}
