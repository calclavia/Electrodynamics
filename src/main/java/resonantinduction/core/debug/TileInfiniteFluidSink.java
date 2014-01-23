package resonantinduction.core.debug;

import java.util.HashMap;

import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import calclavia.lib.prefab.tile.TileAdvanced;

/**
 * Designed to debug fluid devices by draining everything that comes in at one time
 * 
 * @author DarkGuardsman
 */
public class TileInfiniteFluidSink extends TileAdvanced implements IFluidHandler
{
	// TODO later add to this to make it actually have an ingame use other than debug
	public static HashMap<FluidStack, Long> storage = new HashMap<FluidStack, Long>();

	FluidTank tank = new FluidTank(1000000);

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		// TODO add wrench toggle options to change amount actually drained
		return resource != null && this.canFill(from, resource.getFluid()) ? resource.amount : 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		// TODO add wrench settings to close off sides for testing
		return true;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		return new FluidTankInfo[] { this.tank.getInfo() };
	}

}
