package resonantinduction.mechanical.fluid.pipe;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map.Entry;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import resonantinduction.api.fluid.IFluidConnector;
import resonantinduction.api.fluid.IFluidPipe;
import resonantinduction.mechanical.fluid.network.FluidNetwork;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.utility.FluidUtility;

/** @author DarkGuardsman */
public class PipeNetwork extends FluidNetwork
{
	public HashMap<IFluidHandler, EnumSet<ForgeDirection>> connectionMap = new HashMap<IFluidHandler, EnumSet<ForgeDirection>>();

	@Override
	public void update()
	{
		super.update();

		/*
		 * Slight delay to allow visual effect to take place before draining the pipe's internal
		 * tank
		 */
		if (this.getTank().getFluidAmount() > 0)
		{
			FluidStack stack = this.getTank().getFluid().copy();
			int count = this.connectionMap.size();

			for (Entry<IFluidHandler, EnumSet<ForgeDirection>> entry : this.connectionMap.entrySet())
			{
				int sideCount = entry.getValue().size();
				for (ForgeDirection dir : entry.getValue())
				{
					int volPer = (stack.amount / count) + (stack.amount % count);
					int volPerSide = (volPer / sideCount) + (volPer % count);
					int maxFill = 1000;
					TileEntity entity = new Vector3((TileEntity) entry.getKey()).modifyPositionFromSide(dir).getTileEntity(((TileEntity) entry.getKey()).worldObj);
					if (entity instanceof IFluidPipe)
					{
						maxFill = ((IFluidPipe) entity).getMaxFlowRate();
					}

					stack.amount -= entry.getKey().fill(dir, FluidUtility.getStack(stack, Math.min(volPerSide, maxFill)), true);

					if (sideCount > 1)
						--sideCount;
					if (volPer <= 0)
						break;
				}
				if (count > 1)
					count--;
				if (stack == null || stack.amount <= 0)
					break;
			}

			this.getTank().setFluid(stack);
			// TODO check for change before rebuilding
			this.rebuildHandler();
		}
	}

	@Override
	public boolean canUpdate()
	{
		return true;
	}

	@Override
	public boolean continueUpdate()
	{
		return this.getConnectors().size() > 0;
	}

	@Override
	public void reconstruct()
	{
		this.connectionMap.clear();
		super.reconstruct();
	}

	@Override
	public void buildPart(IFluidConnector part)
	{
		super.buildPart(part);
		for (int i = 0; i < 6; i++)
		{
			if (part.getConnections()[i] instanceof IFluidHandler && !(part.getConnections()[i] instanceof IFluidPipe))
			{
				EnumSet<ForgeDirection> set = this.connectionMap.get(part.getConnections()[i]);
				if (set == null)
				{
					set = EnumSet.noneOf(ForgeDirection.class);
				}
				set.add(ForgeDirection.getOrientation(i).getOpposite());
				this.connectionMap.put((IFluidHandler) part.getConnections()[i], set);
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
}
