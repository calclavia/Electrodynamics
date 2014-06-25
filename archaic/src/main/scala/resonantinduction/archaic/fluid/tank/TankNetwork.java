package resonantinduction.archaic.fluid.tank;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidStack;
import resonant.lib.utility.FluidUtility;
import resonantinduction.core.grid.fluid.FluidDistributionetwork;
import resonantinduction.core.grid.fluid.IFluidDistribution;

/** Network that handles connected tanks
 * 
 * @author DarkGuardsman */
public class TankNetwork extends FluidDistributionetwork
{
    public TankNetwork()
    {
        super();
        needsUpdate = true;
    }

    @Override
    public void update()
    {
		final FluidStack networkTankFluid = getTank().getFluid();
		int lowestY = 255, highestY = 0;

		if (getConnectors().size() > 0)
		{
			int totalFluid = networkTankFluid != null ? networkTankFluid.amount : 0;

			HashMap<Integer, Integer> heightCount = new HashMap();
			PriorityQueue<IFluidDistribution> heightPriorityQueue = new PriorityQueue(1024, new Comparator()
			{
				@Override
				public int compare(Object a, Object b)
				{
					if (networkTankFluid != null && networkTankFluid.getFluid().isGaseous())
						return 0;

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

			while (!heightPriorityQueue.isEmpty())
			{
				IFluidDistribution distributeNode = heightPriorityQueue.poll();
				int yCoord = ((TileEntity) distributeNode).yCoord;
				int connectorCount = heightCount.get(yCoord);

				if (totalFluid <= 0)
				{
					distributeNode.getInternalTank().setFluid(null);
					distributeNode.onFluidChanged();
					continue;
				}

				int fluidPer = totalFluid / connectorCount;
				int deltaFluidAmount = fluidPer - distributeNode.getInternalTank().getFluidAmount();

				int current = distributeNode.getInternalTank().getFluidAmount();

				if (deltaFluidAmount > 0)
				{
					int filled = distributeNode.getInternalTank().fill(FluidUtility.getStack(networkTankFluid, deltaFluidAmount), false);
					distributeNode.getInternalTank().fill(FluidUtility.getStack(networkTankFluid, deltaFluidAmount / 10), true);
					totalFluid -= current + filled;
				}
				else
				{
					FluidStack drain = distributeNode.getInternalTank().drain(Math.abs(deltaFluidAmount), false);
					distributeNode.getInternalTank().drain(Math.abs(deltaFluidAmount / 10), true);

					if (drain != null)
						totalFluid -= current - drain.amount;
				}

				if (deltaFluidAmount != 0)
					didChange = true;

				if (connectorCount > 1)
					connectorCount--;

				heightCount.put(yCoord, connectorCount);
				distributeNode.onFluidChanged();
			}

			if (!didChange)
				needsUpdate = false;
		}
    }

    @Override
    public TankNetwork newInstance()
    {
        return new TankNetwork();
    }
}
