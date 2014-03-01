package resonantinduction.archaic.fluid.tank;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import resonantinduction.api.mechanical.fluid.IPressure;
import resonantinduction.core.fluid.FluidDistributionetwork;
import resonantinduction.core.fluid.IFluidDistribution;
import resonantinduction.core.fluid.TileFluidDistribution;
import calclavia.lib.utility.WorldUtility;

public class TileTank extends TileFluidDistribution implements IPressure
{
	public static final int VOLUME = 16;

	public TileTank()
	{
		this.getInternalTank().setCapacity(VOLUME * FluidContainerRegistry.BUCKET_VOLUME);
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (!worldObj.isRemote)
		{
			getNetwork().distributeConnectors();
			sendTankUpdate();
		}
	}

	@Override
	public FluidDistributionetwork getNetwork()
	{
		if (this.network == null)
		{
			this.network = new TankNetwork();
			this.network.addConnector(this);
		}
		return this.network;
	}

	@Override
	public void setNetwork(FluidDistributionetwork network)
	{
		if (network instanceof TankNetwork)
		{
			this.network = network;
		}
	}

	@Override
	public void validateConnectionSide(TileEntity tileEntity, ForgeDirection side)
	{
		if (!this.worldObj.isRemote)
		{
			if (tileEntity instanceof TileTank)
			{
				getNetwork().merge(((IFluidDistribution) tileEntity).getNetwork());
				renderSides = WorldUtility.setEnableSide(renderSides, side, true);
				connectedBlocks[side.ordinal()] = tileEntity;
			}
		}
	}

	@Override
	public void setPressure(int amount)
	{
		pressure = amount;
	}

	@Override
	public int getPressure(ForgeDirection dir)
	{
		return (getInternalTank().getCapacity() - getInternalTank().getFluidAmount()) / 10;
	}

}
