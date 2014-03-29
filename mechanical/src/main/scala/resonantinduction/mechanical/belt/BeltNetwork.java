package resonantinduction.mechanical.belt;

import calclavia.lib.grid.TickingGrid;

/** @author Darkguardsman */
public class BeltNetwork extends TickingGrid<BeltNode>
{

    public BeltNetwork(BeltNode node)
    {
        super(node, BeltNode.class);
    }

}
