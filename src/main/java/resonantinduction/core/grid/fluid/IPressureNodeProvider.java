package resonantinduction.core.grid.fluid;

import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidHandler;
import resonantinduction.core.grid.INodeProvider;

public interface IPressureNodeProvider extends INodeProvider<PressureNode>, IFluidHandler
{
	FluidTank getPressureTank();
}
