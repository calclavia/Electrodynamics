package resonantinduction.mechanical.energy.gearshaft;

import resonant.api.grid.INodeProvider;
import resonant.lib.grid.Node;
import resonantinduction.mechanical.energy.gear.PartGearShaft;

public class ShaftSubNode extends Node<INodeProvider, ShaftGrid, ShaftSubNode>
{
    public ShaftSubNode(PartGearShaft parent)
    {
        super(parent);
    }

    @Override
    protected ShaftGrid newGrid()
    {
        return new ShaftGrid(this);
    }
}
