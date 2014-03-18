package resonantinduction.electrical.itemrailing;

import calclavia.lib.grid.Grid;
import calclavia.lib.grid.Node;

/**
 * @since 18/03/14
 * @author tgame14
 */
public class NodeRailing extends Node<PartRailing, GridRailing, NodeRailing>
{
    public NodeRailing (PartRailing parent)
    {
        super(parent);
    }

    @Override
    protected GridRailing newGrid ()
    {
        return new GridRailing(getClass());
    }

}
