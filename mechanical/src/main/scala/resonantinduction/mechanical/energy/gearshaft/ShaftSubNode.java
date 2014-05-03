package resonantinduction.mechanical.energy.gearshaft;

import resonantinduction.mechanical.energy.gear.PartGearShaft;
import calclavia.lib.grid.INodeProvider;
import calclavia.lib.grid.Node;

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
