package resonantinduction.archaic.fluid.gutter;

import net.minecraftforge.common.util.ForgeDirection;
import resonantinduction.core.prefab.node.NodePressure;
import resonantinduction.core.prefab.node.TileTankNode;

public class FluidGravityNode extends NodePressure
{
	public FluidGravityNode(TileTankNode parent)
	{
		super(parent);
	}

	@Override
	public int getPressure(ForgeDirection dir)
	{
		if (dir == ForgeDirection.UP)
			return -2;

		if (dir == ForgeDirection.DOWN)
			return 2;

		return 0;
	}
}
