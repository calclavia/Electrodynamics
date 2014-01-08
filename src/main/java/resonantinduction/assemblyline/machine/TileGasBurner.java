package resonantinduction.assemblyline.machine;

import resonantinduction.assemblyline.fluid.GasTank;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import com.builtbroken.minecraft.prefab.TileEntityMachine;

/** @author Archadia */
public class TileGasBurner extends TileEntityMachine implements IFluidHandler
{

    GasTank tank = new GasTank(5000);

    public TileGasBurner()
    {

    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
    {
        if (resource != null)
        {
            if (resource.getFluid().isGaseous())
            {
                return tank.fill(resource, doFill);
            }
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
        return true;
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

}
