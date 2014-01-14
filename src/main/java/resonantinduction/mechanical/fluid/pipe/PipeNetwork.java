package resonantinduction.mechanical.fluid.pipe;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import resonantinduction.api.fluid.IFluidPart;
import resonantinduction.mechanical.fluid.network.FluidNetwork;

/** @author DarkGuardsman */
public class PipeNetwork extends FluidNetwork
{
    public HashMap<IFluidHandler, EnumSet<ForgeDirection>> connectionMap = new HashMap<IFluidHandler, EnumSet<ForgeDirection>>();

    @Override
    public void update()
    {
        //Slight delay to allow visual effect to take place before draining the pipe tanks
        if (this.ticks % 3 == 0)
        {
            //TODO find a home for all fluid stored
            //TODO limit input per machine by pipe connected
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
