package resonantinduction.archaic.fluid.gutter;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
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

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid)
    {
        return from != ForgeDirection.UP && !fluid.isGaseous();
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid)
    {
        return from != ForgeDirection.UP && !fluid.isGaseous();
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
    {
        if (!resource.getFluid().isGaseous())
        {
            return super.fill(from, resource, doFill);
        }

        return 0;
    }
}
