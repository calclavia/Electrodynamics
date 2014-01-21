package resonantinduction.mechanical.fluid.network;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map.Entry;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import resonantinduction.api.fluid.IFluidConnector;
import resonantinduction.api.fluid.IFluidNetwork;
import resonantinduction.api.fluid.IFluidPipe;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.utility.FluidUtility;

/** The network for pipe fluid transfer. getNodes() is NOT used.
 * 
 * @author DarkGuardsman */
public class PipeNetwork extends FluidNetwork
{
    public HashMap<IFluidHandler, EnumSet<ForgeDirection>> connectionMap = new HashMap<IFluidHandler, EnumSet<ForgeDirection>>();
    public int maxFlowRate = 0;
    public int maxPressure = 0;

    @Override
    public void update()
    {
        /*
         * Slight delay to allow visual effect to take place before draining the pipe's internal
         * tank
         */
        FluidStack stack = this.getTank().getFluid().copy();
        int count = this.connectionMap.size();

        for (Entry<IFluidHandler, EnumSet<ForgeDirection>> entry : this.connectionMap.entrySet())
        {
            int sideCount = entry.getValue().size();
            for (ForgeDirection dir : entry.getValue())
            {
                int volPer = (stack.amount / count);
                int volPerSide = (volPer / sideCount);

                stack.amount -= entry.getKey().fill(dir, FluidUtility.getStack(stack, Math.min(volPerSide, this.maxFlowRate)), true);

                if (sideCount > 1)
                    --sideCount;
                if (volPer <= 0)
                    break;
            }

            if (count > 1)
                count--;

            if (stack == null || stack.amount <= 0)
            {
                stack = null;
                break;
            }
        }

        this.getTank().setFluid(stack);
        // TODO check for change before rebuilding
        this.reconstructTankInfo();
    }

    @Override
    public boolean canUpdate()
    {
        return getTank().getFluidAmount() > 0 && connectionMap.size() > 0 && getConnectors().size() > 0;
    }

    @Override
    public boolean continueUpdate()
    {
        return canUpdate();
    }

    @Override
    public void reconstruct()
    {
        this.connectionMap.clear();
        this.maxFlowRate = Integer.MAX_VALUE;
        this.maxPressure = Integer.MAX_VALUE;
        super.reconstruct();
    }

    @Override
    public void reconstructConnector(IFluidConnector part)
    {
        super.reconstructConnector(part);
        if (part instanceof IFluidPipe)
        {
            if (((IFluidPipe) part).getMaxFlowRate() < this.maxFlowRate)
                this.maxFlowRate = ((IFluidPipe) part).getMaxFlowRate();

            if (((IFluidPipe) part).getMaxPressure() < this.maxPressure)
                this.maxPressure = ((IFluidPipe) part).getMaxPressure();
        }
        for (int i = 0; i < 6; i++)
        {
            if (part.getConnections()[i] instanceof IFluidHandler && !(part.getConnections()[i] instanceof IFluidPipe))
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
    public FluidStack drain(IFluidConnector source, ForgeDirection from, FluidStack resource, boolean doDrain)
    {
        return null;
    }

    @Override
    public FluidStack drain(IFluidConnector source, ForgeDirection from, int resource, boolean doDrain)
    {
        return null;
    }

    @Override
    public Class getConnectorClass()
    {
        return IFluidPipe.class;
    }

    @Override
    public IFluidNetwork newInstance()
    {
        return new PipeNetwork();
    }
}
