package resonantinduction.archaic.fluid.gutter;

import net.minecraftforge.common.util.ForgeDirection;
import resonantinduction.core.grid.fluid.TileTankNode;
import resonantinduction.core.grid.fluid.pressure.FluidPressureNode;

public class FluidGravityNode extends FluidPressureNode
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

	@Override
	public int getMaxFlowRate()
	{
		return 20;
	}
}
