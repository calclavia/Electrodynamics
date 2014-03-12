package resonantinduction.archaic.fluid.gutter;

import net.minecraftforge.common.ForgeDirection;
import resonantinduction.core.grid.fluid.FluidPressureNode;
import resonantinduction.core.grid.fluid.IPressureNodeProvider;

public class FluidGravityNode extends FluidPressureNode
{
	public FluidGravityNode(IPressureNodeProvider parent)
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

	@Override
	public int getMaxFlowRate()
	{
		return 20;
	}
}
