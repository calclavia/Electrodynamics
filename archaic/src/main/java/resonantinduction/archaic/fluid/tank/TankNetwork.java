package resonantinduction.archaic.fluid.tank;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidStack;
import resonantinduction.core.fluid.FluidDistributionetwork;
import resonantinduction.core.fluid.IFluidDistribution;
import calclavia.lib.utility.FluidUtility;

/**
 * Network that handles connected tanks
 * 
 * @author DarkGuardsman
 */
public class TankNetwork extends FluidDistributionetwork
{
	public TankNetwork()
	{
		super();
		animateDistribution = true;
	}

	@Override
	public void update()
	{
		// if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
		// distributeConnectors();
	}

	@Override
	public void distributeConnectors()
	{
		int animateRate = 0;

		FluidStack totalFluid = getTank().getFluid();
		int lowestY = 255, highestY = 0;

		if (totalFluid == null || totalFluid.getFluid().isGaseous())
		{
			super.distributeConnectors();
		}
		else if (getConnectors().size() > 0)
		{
			FluidStack distributeFluid = totalFluid.copy();
			HashMap<Integer, Integer> heightCount = new HashMap<Integer, Integer>();
			PriorityQueue<IFluidDistribution> heightPriorityQueue = new PriorityQueue<IFluidDistribution>(1024, new Comparator()
			{
				@Override
				public int compare(Object a, Object b)
				{
					TileEntity wa = (TileEntity) a;
					TileEntity wb = (TileEntity) b;
					return wa.yCoord - wb.yCoord;
				}
			});

			for (IFluidDistribution connector : this.getConnectors())
			{
				if (connector instanceof TileEntity)
				{
					int yCoord = ((TileEntity) connector).yCoord;

					if (yCoord < lowestY)
					{
						lowestY = yCoord;
					}

					if (yCoord > highestY)
					{
						highestY = yCoord;
					}

					heightPriorityQueue.add(connector);
					heightCount.put(yCoord, heightCount.containsKey(yCoord) ? heightCount.get(yCoord) + 1 : 1);
				}
			}

			boolean didChange = false;
			//System.out.println("TANK UPDATE " + distributeFluid.amount);
			while (!heightPriorityQueue.isEmpty())
			{
				IFluidDistribution distributeNode = heightPriorityQueue.poll();
				int yCoord = ((TileEntity) distributeNode).yCoord;
				int connectorCount = heightCount.get(yCoord);

				if (distributeFluid == null || distributeFluid.amount <= 0)
				{
					break;
				}

				int fluidPer = (distributeFluid.amount / connectorCount) + (distributeFluid.amount % connectorCount);
				int deltaFluidAmount = (fluidPer - distributeNode.getInternalTank().getFluidAmount()) / 10;
				
				distributeNode.getInternalTank().setFluid(FluidUtility.getStack(distributeFluid,  fluidPer));
				
				/*
				 * 				System.out.println(connectorCount + " : " + fluidPer);

				if (deltaFluidAmount > 0)
				{
					distributeNode.getInternalTank().fill(FluidUtility.getStack(distributeFluid, deltaFluidAmount), true);
				}
				else
				{
					//TODO: This causes quite a lot of issues.
					FluidStack drained = distributeNode.getInternalTank().drain(Math.abs(deltaFluidAmount), true);
				}*/

				distributeFluid.amount -= distributeNode.getInternalTank().getFluidAmount();

				if (deltaFluidAmount != 0)
					didChange = true;

				if (connectorCount > 1)
					connectorCount--;

				heightCount.put(yCoord, connectorCount);
				distributeNode.onFluidChanged();
			}
		}
	}

	@Override
	public TankNetwork newInstance()
	{
		return new TankNetwork();
	}
}
