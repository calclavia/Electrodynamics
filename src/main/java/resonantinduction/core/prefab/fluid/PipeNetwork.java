package resonantinduction.core.prefab.fluid;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import resonantinduction.api.mechanical.fluid.IFluidConnector;
import resonantinduction.api.mechanical.fluid.IFluidNetwork;
import resonantinduction.api.mechanical.fluid.IFluidPipe;
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
	public int maxPressure = 0;
	public int currentPressure = 0;
	public int currentFlowRate = 0;

	@Override
	public void update()
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

	@Override
	public boolean canUpdate()
	{
		return getTank().getFluidAmount() > 0 && sideMap.size() > 0 && getConnectors().size() > 0;
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
		this.maxPressure = Integer.MAX_VALUE;
		super.reconstruct();
	}

	@Override
	public void reconstructConnector(IFluidConnector connector)
	{
		super.reconstructConnector(connector);

		if (connector instanceof IFluidPipe)
		{
			if (((IFluidPipe) connector).getMaxFlowRate() < this.maxFlowRate)
				this.maxFlowRate = ((IFluidPipe) connector).getMaxFlowRate();

			if (((IFluidPipe) connector).getMaxPressure() < this.maxPressure)
				this.maxPressure = ((IFluidPipe) connector).getMaxPressure();
		}
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

	@Override
	public int getPressure()
	{
		return this.currentPressure;
	}
}
