package resonantinduction.core.prefab.node;

import net.minecraft.block.material.Material;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Created by robert on 8/15/2014.
 */
public class TilePressureNode extends TileTankNode
{
	public TilePressureNode(Material material)
	{
		super(material);
		tankNode_$eq(new NodePressure(this));
	}

	public NodePressure getPressureNode()
	{
		return (NodePressure) tankNode();
	}

	public int getPressure(ForgeDirection direction)
	{
		return getPressureNode().getPressure(direction);
	}
}
