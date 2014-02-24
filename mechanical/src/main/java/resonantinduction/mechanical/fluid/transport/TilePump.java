package resonantinduction.mechanical.fluid.transport;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import resonantinduction.api.mechanical.fluid.IPressure;
import resonantinduction.mechanical.network.TileMechanical;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.prefab.tile.IRotatable;

public class TilePump extends TileMechanical implements IFluidHandler, IRotatable, IPressure
{
	private final long maximumPower = 10000;

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		if (from == getDirection().getOpposite())
		{
			TileEntity tileOut = new Vector3(this).translate(from.getOpposite()).getTileEntity(this.worldObj);

			if (tileOut instanceof IFluidHandler)
				return ((IFluidHandler) tileOut).fill(from, resource, doFill);
		}

		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		return from == this.getDirection().getOpposite();
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return from == this.getDirection();
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		return null;
	}

	@Override
	public ForgeDirection getDirection()
	{
		return ForgeDirection.getOrientation(this.worldObj.getBlockMetadata(xCoord, yCoord, zCoord));
	}

	@Override
	public void setDirection(ForgeDirection direction)
	{
		this.worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, direction.ordinal(), 3);
	}

	@Override
	public void setPressure(int amount)
	{
	}

	@Override
	public int getPressure(ForgeDirection dir)
	{
		if (getPower() > 0)
		{
			if (dir == getDirection())
			{
				return (int) Math.max((((double) getPower() / (double) maximumPower) * 100), 2);
			}
			else if (dir == getDirection().getOpposite())
			{
				return (int) -Math.max((((double) getPower() / (double) maximumPower) * 100), 2);
			}
		}

		return 0;
	}

	@Override
	public boolean canConnect(ForgeDirection from, Object source)
	{
		return true;
	}
}
