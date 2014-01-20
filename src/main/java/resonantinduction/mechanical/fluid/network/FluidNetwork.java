package resonantinduction.mechanical.fluid.network;

import java.util.Set;

import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import resonantinduction.api.fluid.IFluidConnector;
import resonantinduction.api.fluid.IFluidNetwork;
import universalelectricity.api.net.IUpdate;
import universalelectricity.core.net.NetworkTickHandler;
import universalelectricity.core.net.NodeNetwork;
import calclavia.lib.utility.FluidUtility;

/**
 * The fluid network.
 * 
 * @author DarkCow, Calclavia
 * 
 */
public abstract class FluidNetwork extends NodeNetwork<IFluidNetwork, IFluidConnector, IFluidHandler> implements IFluidNetwork, IUpdate
{
	protected FluidTank tank = new FluidTank(0);
	protected final FluidTankInfo[] tankInfo = new FluidTankInfo[1];

	// TODO: Make animated distribution to create a smooth flow transition.
	public boolean animateDistribution = false;

	@Override
	public void addConnector(IFluidConnector connector)
	{
		NetworkTickHandler.addNetwork(this);
		super.addConnector(connector);
	}

	@Override
	public void update()
	{

	}

	@Override
	public boolean canUpdate()
	{
		return animateDistribution;
	}

	@Override
	public boolean continueUpdate()
	{
		return canUpdate();
	}

	@Override
	public void reconstruct()
	{
		this.tank = new FluidTank(0);

		for (IFluidConnector part : this.getConnectors())
		{
			if (part.getNetwork() instanceof IFluidNetwork)
			{
				part.setNetwork(this);
			}

			this.reconstructConnector(part);
		}

		this.reconstructTankInfo();
		this.distributeConnectors();
	}

	public void reconstructConnector(IFluidConnector connector)
	{
		FluidTank tank = connector.getInternalTank();

		if (tank != null)
		{
			this.tank.setCapacity(this.tank.getCapacity() + tank.getCapacity());
			if (this.tank.getFluid() == null)
			{
				this.tank.setFluid(tank.getFluid());
			}
			else if (this.tank.getFluid().isFluidEqual(tank.getFluid()))
			{
				this.tank.getFluid().amount += tank.getFluidAmount();
			}
			else if (this.tank.getFluid() != null)
			{
				// TODO cause a mixing event
			}
		}
	}

	public void reconstructTankInfo()
	{
		if (this.getTank() != null)
		{
			this.tankInfo[0] = this.getTank().getInfo();
		}
		else
		{
			this.tankInfo[0] = null;
		}

		this.distributeConnectors();
		NetworkTickHandler.addNetwork(this);
	}

	@Override
	public int fill(IFluidConnector source, ForgeDirection from, FluidStack resource, boolean doFill)
	{
		int prev = this.getTank().getFluidAmount();
		int fill = this.getTank().fill(resource, doFill);

		if (prev != this.getTank().getFluidAmount())
		{
			this.reconstructTankInfo();
		}

		return fill;
	}

	@Override
	public FluidStack drain(IFluidConnector source, ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		if (resource != null && resource.isFluidEqual(this.getTank().getFluid()))
		{
			FluidStack before = this.getTank().getFluid();
			FluidStack drain = this.getTank().drain(resource.amount, doDrain);

			if (before != this.getTank().getFluid() || this.getTank().getFluid() == null || this.getTank().getFluid().amount != before.amount)
			{
				this.reconstructTankInfo();
			}

			return drain;
		}
		return null;
	}

	@Override
	public FluidStack drain(IFluidConnector source, ForgeDirection from, int resource, boolean doDrain)
	{
		if (this.getTank().getFluid() != null)
		{
			return this.drain(source, from, FluidUtility.getStack(this.getTank().getFluid(), resource), doDrain);
		}
		return null;
	}

	public void distributeConnectors()
	{
		FluidStack stack = this.getTank().getFluid();
		this.fillTankSet(stack != null ? stack.copy() : null, this.getConnectors());
	}

	public void fillTankSet(FluidStack stack, Set<IFluidConnector> connectors)
	{
		int parts = connectors.size();
		for (IFluidConnector part : connectors)
		{
			part.getInternalTank().setFluid(null);
			if (stack != null)
			{
				int fillPer = (stack.amount / parts) + (stack.amount % parts);
				stack.amount -= part.getInternalTank().fill(FluidUtility.getStack(stack, fillPer), true);

				if (parts > 1)
					parts--;
			}

			part.onFluidChanged();
		}
	}

	@Override
	public FluidTank getTank()
	{
		if (this.tank == null)
		{
			this.tank = new FluidTank(0);
		}
		return this.tank;
	}

	@Override
	public FluidTankInfo[] getTankInfo()
	{
		return tankInfo;
	}

	@Override
	public String toString()
	{
		return super.toString() + "  Vol:" + this.tank.getFluidAmount();
	}

}
