package com.dark.fluid;

import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

/** Tank that has a filter on the fluid ids it will accept
 * 
 * @author DarkGuardsman */
public class FilteredTank extends FluidTank
{
    private List<Integer> fluidIds = new ArrayList<Integer>();
    boolean gas = true;
    boolean liquid = true;

    public FilteredTank(int capacity, int... fluidIds)
    {
        this(capacity, true, true, fluidIds);
    }

    public FilteredTank(int capacity, boolean gas, boolean liquid, int... fluidIds)
    {
        super(capacity);
        this.gas = gas;
        this.liquid = liquid;
        for (int id : fluidIds)
        {
            this.fluidIds.add(id);
        }
    }

    public FilteredTank(FluidStack stack, int capacity)
    {
        super(stack, capacity);
    }

    public FilteredTank(Fluid fluid, int amount, int capacity)
    {
        super(fluid, amount, capacity);
    }

    @Override
    public int fill(FluidStack resource, boolean doFill)
    {
        if (resource != null && resource.getFluid() != null && (!gas || gas && resource.getFluid().isGaseous()) && (!liquid || liquid && !resource.getFluid().isGaseous()))
        {
            if (fluidIds.contains(resource.fluidID))
            {
                return super.fill(resource, doFill);
            }
        }
        return 0;
    }

}
