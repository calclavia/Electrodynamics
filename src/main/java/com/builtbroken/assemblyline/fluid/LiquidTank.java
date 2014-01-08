package com.builtbroken.assemblyline.fluid;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

/** Version of the fluid tank that only supports liquids
 * 
 * @author DarkGuardsman */
public class LiquidTank extends FluidTank
{

    public LiquidTank(int capacity)
    {
        super(capacity);
    }

    public LiquidTank(FluidStack stack, int capacity)
    {
        super(stack, capacity);
    }

    public LiquidTank(Fluid fluid, int amount, int capacity)
    {
        super(fluid, amount, capacity);
    }

    @Override
    public int fill(FluidStack resource, boolean doFill)
    {
        if (resource != null && resource.getFluid() != null && !resource.getFluid().isGaseous())
        {
            return super.fill(resource, doFill);
        }
        return 0;
    }

}
