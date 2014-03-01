package resonantinduction.archaic.fluid.tank;

import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidStack;
import resonantinduction.core.fluid.FluidDistributionetwork;
import resonantinduction.core.fluid.IFluidDistribution;
import calclavia.lib.utility.FluidUtility;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

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
		needsUpdate = true;
	}

	@Override
	public void update()
	{
		final FluidStack totalFluid = getTank().getFluid();
		int lowestY = 255, highestY = 0;

		if (totalFluid != null && getConnectors().size() > 0)
		{
			FluidStack distributeFluid = totalFluid.copy();
			HashMap<Integer, Integer> heightCount = new HashMap<Integer, Integer>();
			PriorityQueue<IFluidDistribution> heightPriorityQueue = new PriorityQueue<IFluidDistribution>(1024, new Comparator()
			{
				@Override
				public int compare(Object a, Object b)
				{
					if (totalFluid.getFluid().isGaseous())
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

				if (distributeFluid == null || distributeFluid.amount <= 0)
				{
					distributeNode.getInternalTank().setFluid(null);
					distributeNode.onFluidChanged();
					continue;
				}

				int fluidPer = distributeFluid.amount / connectorCount;
				int deltaFluidAmount = fluidPer - distributeNode.getInternalTank().getFluidAmount();

				int current = distributeNode.getInternalTank().getFluidAmount();

				if (deltaFluidAmount > 0)
				{
					int filled = distributeNode.getInternalTank().fill(FluidUtility.getStack(distributeFluid, deltaFluidAmount), false);
					distributeNode.getInternalTank().fill(FluidUtility.getStack(distributeFluid, deltaFluidAmount / 10), true);
					distributeFluid.amount -= current + filled;
				}
				else
				{
					FluidStack drain = distributeNode.getInternalTank().drain(Math.abs(deltaFluidAmount), false);
					distributeNode.getInternalTank().drain(Math.abs(deltaFluidAmount / 10), true);

					if (drain != null)
						distributeFluid.amount -= current - drain.amount;
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
