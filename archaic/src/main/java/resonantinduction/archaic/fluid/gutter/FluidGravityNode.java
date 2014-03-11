package resonantinduction.archaic.fluid.gutter;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.IFluidHandler;
import resonantinduction.archaic.fluid.grate.TileGrate;
import resonantinduction.core.grid.fluid.IPressureNodeProvider;
import resonantinduction.core.grid.fluid.FluidPressureNode;
import calclavia.lib.utility.WorldUtility;

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
			return -1;

		if (dir == ForgeDirection.DOWN)
			return 1;

		return 0;
	}

	@Override
	public int getMaxFlowRate()
	{
		return 20;
	}
}
