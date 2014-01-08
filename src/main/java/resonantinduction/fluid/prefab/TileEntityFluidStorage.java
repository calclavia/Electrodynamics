package resonantinduction.fluid.prefab;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import com.builtbroken.minecraft.FluidHelper;
import com.builtbroken.minecraft.helpers.ColorCode.IColorCoded;

public abstract class TileEntityFluidStorage extends TileEntityFluidDevice implements IFluidHandler, IColorCoded
{

    public FluidTank fluidTank;

    @Override
    public String getMeterReading(EntityPlayer user, ForgeDirection side, EnumTools tool)
    {
        if (tool != EnumTools.PIPE_GUAGE)
        {
            return null;
        }
        if (this.getTank().getFluid() == null)
        {
            return "Empty";
        }
        return String.format("%d/%d %S Stored", getTank().getFluid().amount / FluidContainerRegistry.BUCKET_VOLUME, this.getTank().getCapacity() / FluidContainerRegistry.BUCKET_VOLUME, getTank().getFluid().getFluid().getLocalizedName());
    }

    @Override
    public boolean canTileConnect(Connection type, ForgeDirection dir)
    {
        return type == Connection.FLUIDS;
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
    {
        if (resource == null || resource.getFluid() == null)
        {
            return 0;
        }
        else if (this.getTank().getFluid() != null && !resource.isFluidEqual(this.getTank().getFluid()))
        {
            return 0;
        }
        return this.getTank().fill(resource, doFill);
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
    {
        if (this.getTank().getFluid() == null)
        {
            return null;
        }
        FluidStack stack = this.getTank().getFluid();
        if (maxDrain < stack.amount)
        {
            stack = FluidHelper.getStack(stack, maxDrain);
        }
        return this.getTank().drain(maxDrain, doDrain);
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from)
    {
        if (this.getTank() != null)
        {
            return new FluidTankInfo[] { new FluidTankInfo(this.getTank()) };
        }
        return new FluidTankInfo[1];
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        if (nbt.hasKey("stored"))
        {
            NBTTagCompound tag = nbt.getCompoundTag("stored");
            String name = tag.getString("LiquidName");
            int amount = nbt.getInteger("Amount");
            Fluid fluid = FluidRegistry.getFluid(name);
            if (fluid != null)
            {
                FluidStack liquid = new FluidStack(fluid, amount);
                getTank().setFluid(liquid);
            }
        }
        else
        {
            //System.out.println("Loading fluid tank");
            getTank().readFromNBT(nbt.getCompoundTag("FluidTank"));
            //System.out.println("Tank: "+ (getTank().getFluid() != null ? getTank().getFluid().fluidID +"@"+getTank().getFluid().amount+"mb" : "Empty"));

        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        if (this.getTank() != null)
        {
            //System.out.println("Saving fluid tank");
            //System.out.println("Tank: "+ (getTank().getFluid() != null ? getTank().getFluid().fluidID +"@"+getTank().getFluid().amount+"mb" : "Empty"));
            nbt.setCompoundTag("FluidTank", this.getTank().writeToNBT(new NBTTagCompound()));
        }
    }

    /** Is the internal tank full */
    public boolean isFull()
    {
        return this.getTank().getFluidAmount() >= this.getTank().getCapacity();
    }

    public FluidTank getTank()
    {
        if (this.fluidTank == null)
        {
            this.fluidTank = new FluidTank(this.getTankSize());
        }
        return this.fluidTank;
    }

    /** gets the max storage limit of the tank */
    public abstract int getTankSize();

}
