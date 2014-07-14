package resonantinduction.electrical.itemrailing;

import resonant.lib.grid.TickingGrid;

/**
 * @since 25/05/14
 * @author tgame14
 */
public class GridRailing extends TickingGrid<NodeRailing>
{
    public GridRailing (NodeRailing node, Class type)
    {
        super(node, type);
    }
}
