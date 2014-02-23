package resonantinduction.mechanical.fluid.transport;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import resonantinduction.api.mechanical.fluid.IFluidPipe;
import resonantinduction.api.mechanical.fluid.IPressure;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.prefab.tile.IRotatable;
import calclavia.lib.prefab.tile.TileAdvanced;

public class TilePump extends TileAdvanced implements IFluidHandler, IRotatable, IPressure
{
	private int pressure;

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
		return false;
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
		pressure = amount;
	}

	@Override
	public int getPressure(ForgeDirection dir)
	{
		if (dir == getDirection())
		{
			return 20;
		}
		else if (dir == getDirection().getOpposite())
		{
			return -25;
		}

		return 0;
	}

}
