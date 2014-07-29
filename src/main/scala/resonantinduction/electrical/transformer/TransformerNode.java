package resonantinduction.electrical.transformer;

import universalelectricity.api.core.grid.INodeProvider;
import universalelectricity.core.grid.node.ElectricNode;

/**
 * Created by robert on 7/29/2014.
 */
public class TransformerNode extends ElectricNode {

    public TransformerNode(INodeProvider parent)
    {
        super(parent);
    }
}
