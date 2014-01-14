package resonantinduction.mechanical.fluid.pipe;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map.Entry;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import resonantinduction.api.fluid.IFluidPart;
import resonantinduction.api.fluid.IFluidPipe;
import resonantinduction.mechanical.fluid.network.FluidNetwork;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.utility.FluidUtility;

/** @author DarkGuardsman */
public class PipeNetwork extends FluidNetwork
{
    public HashMap<IFluidHandler, EnumSet<ForgeDirection>> connectionMap = new HashMap<IFluidHandler, EnumSet<ForgeDirection>>();

    @Override
    public void update()
    {
        super.update();
        //Slight delay to allow visual effect to take place before draining the pipe tanks
        if (this.ticks % 3 == 0 && this.getTank().getFluid() != null)
        {
            FluidStack stack = this.getTank().getFluid().copy();
            int count = this.connectionMap.size();
            for (Entry<IFluidHandler, EnumSet<ForgeDirection>> entry : this.connectionMap.entrySet())
            {
                int volPer = stack.amount / count;
                for (ForgeDirection dir : entry.getValue())
                {
                    int maxFill = 10000;
                    TileEntity entity = new Vector3((TileEntity) entry.getKey()).modifyPositionFromSide(dir).getTileEntity(((TileEntity) entry.getKey()).worldObj);
                    if (entity instanceof IFluidPipe)
                    {
                        maxFill = ((IFluidPipe) entity).getMaxFlowRate();
                    }
                    int fill = entry.getKey().fill(dir, FluidUtility.getStack(stack, Math.min(volPer, maxFill)), true);
                    volPer -= fill;
                    stack.amount -= fill;
                }
                if (count > 1)
                    count--;

            }

        }
    }

    @Override
    public boolean canUpdate()
    {
        return super.canUpdate() || this.getTank().getFluid() != null;
    }

    @Override
    public boolean continueUpdate()
    {
        return super.canUpdate() || this.getTank().getFluid() != null;
    }

    @Override
    public void reconstruct()
    {
        this.connectionMap.clear();
        super.reconstruct();
    }

    @Override
    public void buildPart(IFluidPart part)
    {
        super.buildPart(part);
        for (int i = 0; i < 6; i++)
        {
            if (part.getConnections()[i] instanceof IFluidHandler)
            {
                EnumSet<ForgeDirection> set = this.connectionMap.get(part.getConnections()[i]);
                if (set == null)
                {
                    set = EnumSet.noneOf(ForgeDirection.class);
                }
                set.add(ForgeDirection.getOrientation(i).getOpposite());
                this.connectionMap.put((IFluidHandler) part.getConnections()[i], set);
            }
        }
    }

    @Override
    public FluidStack drain(IFluidPart source, ForgeDirection from, FluidStack resource, boolean doDrain)
    {
        return null;
    }

    @Override
    public FluidStack drain(IFluidPart source, ForgeDirection from, int resource, boolean doDrain)
    {
        return null;
    }
}
