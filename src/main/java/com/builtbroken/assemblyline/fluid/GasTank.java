package com.builtbroken.assemblyline.fluid;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

/** Version of the fluid tank that is restricted to gases only
 * 
 * @author DarkGuardsman */
public class GasTank extends FluidTank
{

    public GasTank(int capacity)
    {
        super(capacity);
    }

    public GasTank(FluidStack stack, int capacity)
    {
        super(stack, capacity);
    }

    public GasTank(Fluid fluid, int amount, int capacity)
    {
        super(fluid, amount, capacity);
    }

    @Override
    public int fill(FluidStack resource, boolean doFill)
    {
        if (resource != null && resource.getFluid() != null && resource.getFluid().isGaseous())
        {
            return super.fill(resource, doFill);
        }
        return 0;
    }

}
