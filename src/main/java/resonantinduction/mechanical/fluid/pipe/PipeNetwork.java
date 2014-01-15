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
import universalelectricity.core.net.NetworkTickHandler;
import calclavia.lib.utility.FluidUtility;

/** @author DarkGuardsman */
public class PipeNetwork extends FluidNetwork
{
    public HashMap<IFluidHandler, EnumSet<ForgeDirection>> connectionMap = new HashMap<IFluidHandler, EnumSet<ForgeDirection>>();

    public PipeNetwork()
    {
        NetworkTickHandler.addNetwork(this);
    }

    @Override
    public void update()
    {
        super.update();
        //Slight delay to allow visual effect to take place before draining the pipe's internal tank
        if (this.ticks % 2 == 0 && this.getTank().getFluidAmount() > 0)
        {
            FluidStack stack = this.getTank().getFluid().copy();
            int count = this.connectionMap.size();
            for (Entry<IFluidHandler, EnumSet<ForgeDirection>> entry : this.connectionMap.entrySet())
            {
                int volPer = stack.amount / count;
                int sideCount = entry.getValue().size();
                for (ForgeDirection dir : entry.getValue())
                {
                    int volPerSide = volPer / sideCount;
                    int maxFill = 1000;
                    TileEntity entity = new Vector3((TileEntity) entry.getKey()).modifyPositionFromSide(dir).getTileEntity(((TileEntity) entry.getKey()).worldObj);
                    if (entity instanceof IFluidPipe)
                    {
                        maxFill = ((IFluidPipe) entity).getMaxFlowRate();
                    }
                    int fill = entry.getKey().fill(dir, FluidUtility.getStack(stack, Math.min(volPerSide, maxFill)), true);
                    volPer -= fill;
                    stack.amount -= fill;
                    if (sideCount > 1)
                        --sideCount;

                    if (volPer <= 0)
                        break;
                }
                if (count > 1)
                    count--;
                if (stack == null || stack.amount <= 0)
                    break;
            }
            this.getTank().setFluid(stack);
            //TODO check for change before rebuilding
            this.rebuildTank();
        }
    }

    @Override
    public boolean canUpdate()
    {
        return true;
    }

    @Override
    public boolean continueUpdate()
    {
        return true;
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
