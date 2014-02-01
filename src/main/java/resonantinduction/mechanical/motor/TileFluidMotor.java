package resonantinduction.mechanical.motor;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import resonantinduction.api.fluid.IFluidPipe;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.prefab.tile.IRotatable;
import calclavia.lib.prefab.tile.TileAdvanced;

public class TileFluidMotor extends TileAdvanced implements IFluidHandler, IRotatable
{
    ForgeDirection facing = ForgeDirection.UNKNOWN;
    boolean input = true;
    final int maxFlow = 1000;
    int volFilled = 0;
    int averageVol = 0;

    @Override
    public void updateEntity()
    {
        super.updateEntity();
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
    {
        if (from == getDirection().getOpposite())
        {
            TileEntity tileOut = new Vector3(this).translate(from.getOpposite()).getTileEntity(this.worldObj);
            TileEntity tileIn = new Vector3(this).translate(from).getTileEntity(this.worldObj);
            if (tileIn instanceof IFluidPipe && tileOut instanceof IFluidPipe)
            {
                if (((IFluidPipe) tileIn).getPressure() <= ((IFluidPipe) tileOut).getPressure())
                {
                    return 0;
                }
            }
            if (tileOut instanceof IFluidHandler && !(tileOut instanceof TileFluidMotor))
            {
                //TODO pass fluid on to the other side of the motor and get average flow rate
            }
        }
        return 0;
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid)
    {
        return !input && from == this.getDirection().getOpposite();
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from)
    {
        if (from == getDirection().getOpposite())
        {
            TileEntity tile = new Vector3(this).translate(from.getOpposite()).getTileEntity(this.worldObj);
            if (tile instanceof IFluidHandler && !(tile instanceof TileFluidMotor))
            {
                return ((IFluidHandler) tile).getTankInfo(from);
            }
        }
        return new FluidTankInfo[1];
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

}
