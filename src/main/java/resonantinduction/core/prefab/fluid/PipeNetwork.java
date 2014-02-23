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
	public HashMap<IFluidHandler, EnumSet<ForgeDirection>> sideMap = new HashMap<IFluidHandler, EnumSet<ForgeDirection>>();
	public HashMap<IFluidHandler, IFluidConnector> connectionMap = new HashMap<IFluidHandler, IFluidConnector>();
	public int maxFlowRate = 0;
	public int currentPressure = 0;
	public int currentFlowRate = 0;

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
	 * Old pipe distribution code.
	 */
	@Deprecated
	public void oldDistribution()
	{
		/*
		 * Slight delay to allow visual effect to take place before draining the pipe's internal
		 * tank
		 */
		FluidStack stack = this.getTank().getFluid().copy();
		int count = this.sideMap.size();

		Iterator<Entry<IFluidHandler, EnumSet<ForgeDirection>>> it = new HashMap<IFluidHandler, EnumSet<ForgeDirection>>(sideMap).entrySet().iterator();

		while (it.hasNext())
		{
			Entry<IFluidHandler, EnumSet<ForgeDirection>> entry = it.next();
			int sideCount = entry.getValue().size();

			for (ForgeDirection dir : entry.getValue())
			{
				int volPer = (stack.amount / count);
				int volPerSide = (volPer / sideCount);
				IFluidHandler handler = entry.getKey();

				/*
				 * Don't input to tanks from the sides where the pipe is extraction mode. This
				 * prevents feed-back loops.
				 */
				if (connectionMap.get(handler).canFlow())
				{
					stack.amount -= handler.fill(dir, FluidUtility.getStack(stack, Math.min(volPerSide, this.maxFlowRate)), true);
				}

				if (sideCount > 1)
					--sideCount;
				if (volPer <= 0)
					break;
			}

			if (count > 1)
				count--;

			if (stack == null || stack.amount <= 0)
			{
				stack = null;
				break;
			}
		}

		getTank().setFluid(stack);
		// TODO check for change before rebuilding
		reconstructTankInfo();
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

				/**
				 * Apply "gravity pressure"
				 */
				if (i == 0)
					pressure -= 25;
				else if (i == 1)
					pressure += 25;

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
				int pressureA = sourcePipe.getPressure(ForgeDirection.getOrientation(i));
				int pressureB = otherPipe.getPressure(ForgeDirection.getOrientation(i).getOpposite());

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
									tankA.drain(quantity, true);
									tankB.fill(new FluidStack(fluidA.getFluid(), quantity), true);
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
						FluidStack drainStack = sourceTank.drain(transferAmount, false);
						sourceTank.drain(fluidHandler.fill(dir.getOpposite(), drainStack, true), true);
					}
				}
				else if (pressure < tankPressure)
				{
					if (transferAmount > 0)
					{
						FluidStack drainStack = fluidHandler.drain(dir.getOpposite(), transferAmount, false);
						fluidHandler.drain(dir.getOpposite(), sourceTank.fill(drainStack, true), true);
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
	public void reconstruct()
	{
		this.sideMap.clear();
		this.maxFlowRate = Integer.MAX_VALUE;
		super.reconstruct();
	}

	@Override
	public void reconstructConnector(IFluidConnector connector)
	{
		super.reconstructConnector(connector);

		for (int i = 0; i < 6; i++)
		{
			if (connector.getConnections()[i] instanceof IFluidHandler && !(connector.getConnections()[i] instanceof IFluidPipe))
			{
				EnumSet<ForgeDirection> set = this.sideMap.get(connector.getConnections()[i]);
				if (set == null)
				{
					set = EnumSet.noneOf(ForgeDirection.class);
				}

				set.add(ForgeDirection.getOrientation(i).getOpposite());
				sideMap.put((IFluidHandler) connector.getConnections()[i], set);
				connectionMap.put((IFluidHandler) connector.getConnections()[i], connector);
			}
		}
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
