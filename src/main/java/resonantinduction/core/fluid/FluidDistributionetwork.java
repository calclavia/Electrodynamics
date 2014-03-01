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
	public boolean needsUpdate = false;

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
		return needsUpdate && getConnectors().size() > 0;
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
		needsUpdate = true;
		NetworkTickHandler.addNetwork(this);
	}

	@Override
	public void reconstructConnector(IFluidDistribution connector)
	{
		if (connector.getNetwork() instanceof FluidDistributionetwork)
			connector.setNetwork(this);

		FluidTank connectorTank = connector.getInternalTank();

		if (connectorTank != null)
		{
			tank.setCapacity(tank.getCapacity() + connectorTank.getCapacity());

			if (connectorTank.getFluid() != null)
			{
				if (tank.getFluid() == null)
				{
					tank.setFluid(connectorTank.getFluid().copy());
				}
				else if (tank.getFluid().isFluidEqual(connectorTank.getFluid()))
				{
					tank.getFluid().amount += connectorTank.getFluidAmount();
				}
				else if (tank.getFluid() != null)
				{
					// TODO: Cause a mixing event
				}
			}
		}
	}

	public int fill(IFluidDistribution source, ForgeDirection from, FluidStack resource, boolean doFill)
	{
		int prev = this.getTank().getFluidAmount();
		int fill = this.getTank().fill(resource.copy(), doFill);
		needsUpdate = true;
		NetworkTickHandler.addNetwork(this);
		return fill;
	}

	public FluidStack drain(IFluidDistribution source, ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		if (resource != null && resource.isFluidEqual(getTank().getFluid()))
		{
			FluidStack drain = getTank().drain(resource.amount, doDrain);
			needsUpdate = true;
			NetworkTickHandler.addNetwork(this);
			return drain;
		}

		return null;
	}

	public FluidStack drain(IFluidDistribution source, ForgeDirection from, int resource, boolean doDrain)
	{
		FluidStack drain = getTank().drain(resource, doDrain);
		needsUpdate = true;
		NetworkTickHandler.addNetwork(this);
		return drain;
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
		return new FluidTankInfo[] { getTank().getInfo() };
	}

	@Override
	public String toString()
	{
		return super.toString() + " Volume: " + this.tank.getFluidAmount();
	}
}
