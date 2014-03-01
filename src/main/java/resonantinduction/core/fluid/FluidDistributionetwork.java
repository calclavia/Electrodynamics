package resonantinduction.core.fluid;

import java.util.Set;

import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import universalelectricity.api.net.IUpdate;
import universalelectricity.core.net.NetworkTickHandler;
import universalelectricity.core.net.NodeNetwork;
import calclavia.lib.utility.FluidUtility;

/**
 * The fluid network for instantaneous equal distribution between all nodes. Used for tanks.
 * 
 * @author DarkCow, Calclavia
 * 
 */
public abstract class FluidDistributionetwork extends NodeNetwork<FluidDistributionetwork, IFluidDistribution, IFluidHandler> implements IUpdate
{
	protected FluidTank tank = new FluidTank(0);
	protected final FluidTankInfo[] tankInfo = new FluidTankInfo[1];

	public boolean animateDistribution = false;

	public FluidDistributionetwork()
	{
		super(IFluidDistribution.class);
	}

	@Override
	public void addConnector(IFluidDistribution connector)
	{
		super.addConnector(connector);
		NetworkTickHandler.addNetwork(this);
	}

	@Override
	public boolean canUpdate()
	{
		return animateDistribution && getConnectors().size() > 0;
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

		super.reconstruct();

		this.reconstructTankInfo();
		this.distributeConnectors();
	}

	@Override
	public void reconstructConnector(IFluidDistribution connector)
	{
		if (connector.getNetwork() instanceof FluidDistributionetwork)
			connector.setNetwork(this);

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

		distributeConnectors();
		NetworkTickHandler.addNetwork(this);
	}

	public int fill(IFluidDistribution source, ForgeDirection from, FluidStack resource, boolean doFill)
	{
		int prev = this.getTank().getFluidAmount();
		int fill = this.getTank().fill(resource, doFill);

		if (prev != this.getTank().getFluidAmount())
		{
			reconstructTankInfo();
		}

		return fill;
	}

	public FluidStack drain(IFluidDistribution source, ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		if (resource != null && resource.isFluidEqual(getTank().getFluid()))
		{
			FluidStack before = this.getTank().getFluid();
			FluidStack drain = this.getTank().drain(resource.amount, doDrain);

			if (!FluidUtility.matchExact(before, drain))
			{
				reconstructTankInfo();
			}

			return drain;
		}
		return null;
	}

	public FluidStack drain(IFluidDistribution source, ForgeDirection from, int resource, boolean doDrain)
	{
		if (getTank().getFluid() != null)
		{
			return this.drain(source, from, FluidUtility.getStack(getTank().getFluid(), resource), doDrain);
		}
		return null;
	}

	public void distributeConnectors()
	{
		FluidStack stack = this.getTank().getFluid();
		this.fillTankSet(stack != null ? stack.copy() : null, this.getConnectors());
	}

	public void fillTankSet(FluidStack stack, Set<IFluidDistribution> connectors)
	{
		int connectorCount = connectors.size();

		for (IFluidDistribution part : connectors)
		{
			part.getInternalTank().setFluid(null);
			if (stack != null)
			{
				int fillPer = (stack.amount / connectorCount) + (stack.amount % connectorCount);
				stack.amount -= part.getInternalTank().fill(FluidUtility.getStack(stack, fillPer), true);

				if (connectorCount > 1)
					connectorCount--;
			}

			part.onFluidChanged();
		}
	}

	public FluidTank getTank()
	{
		if (this.tank == null)
		{
			this.tank = new FluidTank(0);
		}
		return this.tank;
	}

	public Class getConnectorClass()
	{
		return IFluidDistribution.class;
	}

	public FluidTankInfo[] getTankInfo()
	{
		return tankInfo;
	}

	@Override
	public String toString()
	{
		return super.toString() + " Volume: " + this.tank.getFluidAmount();
	}
}
