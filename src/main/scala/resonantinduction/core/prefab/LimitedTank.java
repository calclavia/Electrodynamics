package resonantinduction.core.prefab;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

/**
 * Version of the fluid tank that restricts input & output amounts
 * @author Darkguardsman
 */
public class LimitedTank extends FluidTank
{
    protected int maxInput;
    protected int maxOutput;

    public LimitedTank(int capacity)
    {
        this(capacity, capacity);
    }

    public LimitedTank(int capacity, int maxFluidMovement)
    {
        this(capacity, maxFluidMovement, maxFluidMovement);
    }

    public LimitedTank(int capacity, int maxinput, int maxOutput)
    {
        super(capacity);
        this.maxInput = maxinput;
        this.maxOutput = maxOutput;
    }

    @Override
    public int fill(FluidStack resource, boolean doFill)
    {
        if(resource != null)
        {
            FluidStack fluid = resource.copy();
            fluid.amount = Math.max(fluid.amount, maxInput);
            return super.fill(resource, doFill);
        }
        return 0;
    }

    @Override
    public FluidStack drain(int maxDrain, boolean doDrain)
    {
        return super.drain(Math.max(maxDrain, maxOutput),doDrain);
    }
}
