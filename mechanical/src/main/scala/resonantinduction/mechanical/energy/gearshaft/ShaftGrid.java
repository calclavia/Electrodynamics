package resonantinduction.mechanical.energy.gearshaft;

import resonant.lib.grid.NodeGrid;

public class ShaftGrid extends NodeGrid<ShaftSubNode>
{    
    public ShaftGrid(ShaftSubNode node)
    {
        super(ShaftSubNode.class);
        add(node);
    }
}
