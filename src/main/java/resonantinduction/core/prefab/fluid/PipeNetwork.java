package resonantinduction.core.prefab.fluid;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidHandler;
import resonantinduction.api.mechanical.fluid.IFluidConnector;
import resonantinduction.api.mechanical.fluid.IFluidNetwork;
import resonantinduction.api.mechanical.fluid.IFluidPipe;
import resonantinduction.api.mechanical.fluid.IPressure;
import calclavia.lib.utility.FluidUtility;

/**
 * The network for pipe fluid transfer. getNodes() is NOT used.
 * 
 * @author DarkGuardsman
 */
public class PipeNetwork extends FluidNetwork
{
	@Override
	public void update()
	{
		for (IFluidConnector connector : getConnectors())
		{
			if (connector instanceof IFluidPipe)
			{
				calculatePressure((IFluidPipe) connector);
				distribute((IFluidPipe) connector);
			}
		}
	}

	/**
	 * Calculate pressure in this pipe.
	 */
	public void calculatePressure(IFluidPipe sourcePipe)
	{
		int totalPressure = 0;
		int findCount = 0;
		int minPressure = 0;
		int maxPressure = 0;

		for (int i = 0; i < 6; i++)
		{
			Object obj = sourcePipe.getConnections()[i];

			if (obj instanceof IPressure)
			{
				int pressure = ((IPressure) obj).getPressure(ForgeDirection.getOrientation(i).getOpposite());

				minPressure = Math.min(pressure, minPressure);
				maxPressure = Math.max(pressure, maxPressure);
				totalPressure += pressure;
				findCount++;
			}
		}

		if (findCount == 0)
		{
			sourcePipe.setPressure(0);
		}
		else
		{
			/**
			 * Create pressure loss.
			 */
			if (minPressure < 0)
				minPressure += 1;
			if (maxPressure > 0)
				maxPressure -= 1;

			sourcePipe.setPressure(Math.max(minPressure, Math.min(maxPressure, totalPressure / findCount + Integer.signum(totalPressure))));
		}
	}

	/**
	 * Distribute fluid in this pipe based on pressure.
	 */
	public static void distribute(IFluidPipe sourcePipe)
	{
		for (int i = 0; i < 6; i++)
		{
			Object obj = sourcePipe.getConnections()[i];

			if (obj instanceof IFluidPipe)
			{
				IFluidPipe otherPipe = (IFluidPipe) obj;

				/**
				 * Move fluid from higher pressure to lower. In this case, move from tankA to tankB.
				 */
				ForgeDirection dir = ForgeDirection.getOrientation(i);
				int pressureA = sourcePipe.getPressure(dir);
				int pressureB = otherPipe.getPressure(dir.getOpposite());

				if (pressureA >= pressureB)
				{
					FluidTank tankA = sourcePipe.getInternalTank();
					FluidStack fluidA = tankA.getFluid();

					if (tankA != null && fluidA != null)
					{
						int amountA = fluidA.amount;

						if (amountA > 0)
						{
							FluidTank tankB = otherPipe.getInternalTank();

							if (tankB != null)
							{
								int amountB = tankB.getFluidAmount();

								int quantity = Math.max(pressureA > pressureB ? 25 : 0, (amountA - amountB) / 2);
								quantity = Math.min(Math.min(quantity, tankB.getCapacity() - amountB), amountA);

								if (quantity > 0)
								{
									FluidStack drainStack = sourcePipe.drain(dir.getOpposite(), quantity, false);

									if (drainStack != null)
										sourcePipe.drain(dir.getOpposite(), otherPipe.fill(dir, drainStack, true), true);
								}
							}
						}
					}
				}
			}
			else if (obj instanceof IFluidHandler)
			{
				IFluidHandler fluidHandler = (IFluidHandler) obj;
				ForgeDirection dir = ForgeDirection.getOrientation(i);
				int pressure = sourcePipe.getPressure(dir);
				int tankPressure = fluidHandler instanceof IPressure ? ((IPressure) fluidHandler).getPressure(dir.getOpposite()) : 0;
				FluidTank sourceTank = sourcePipe.getInternalTank();

				int transferAmount = (Math.max(pressure, tankPressure) - Math.min(pressure, tankPressure)) * sourcePipe.getMaxFlowRate();

				if (pressure > tankPressure)
				{
					if (sourceTank.getFluidAmount() > 0 && transferAmount > 0)
					{
						FluidStack drainStack = sourcePipe.drain(dir.getOpposite(), transferAmount, false);
						sourcePipe.drain(dir.getOpposite(), fluidHandler.fill(dir.getOpposite(), drainStack, true), true);
					}
				}
				else if (pressure < tankPressure)
				{
					if (transferAmount > 0)
					{
						FluidStack drainStack = fluidHandler.drain(dir.getOpposite(), transferAmount, false);
						fluidHandler.drain(dir.getOpposite(), sourcePipe.fill(dir.getOpposite(), drainStack, true), true);
					}
				}
			}
		}
	}

	@Override
	public boolean canUpdate()
	{
		return getConnectors().size() > 0;
	}

	@Override
	public boolean continueUpdate()
	{
		return canUpdate();
	}

	@Override
	public FluidStack drain(IFluidConnector source, ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		return null;
	}

	@Override
	public FluidStack drain(IFluidConnector source, ForgeDirection from, int resource, boolean doDrain)
	{
		return null;
	}

	@Override
	public Class getConnectorClass()
	{
		return IFluidPipe.class;
	}

	@Override
	public IFluidNetwork newInstance()
	{
		return new PipeNetwork();
	}
}
