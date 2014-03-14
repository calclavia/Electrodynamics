package resonantinduction.core.grid.fluid;

import calclavia.lib.grid.INodeProvider;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidHandler;

public interface IPressureNodeProvider extends INodeProvider, IFluidHandler
{
	public FluidTank getPressureTank();

	public void onFluidChanged();
}
