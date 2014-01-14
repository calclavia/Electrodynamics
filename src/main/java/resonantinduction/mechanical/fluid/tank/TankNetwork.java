package resonantinduction.mechanical.fluid.tank;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidStack;
import resonantinduction.api.fluid.IFluidPart;
import resonantinduction.mechanical.fluid.network.FluidNetwork;
import calclavia.lib.utility.FluidHelper;

/** Network that handles connected tanks
 * 
 * @author DarkGuardsman */
public class TankNetwork extends FluidNetwork
{

    @Override
    public void reloadTanks()
    {
        FluidStack fillStack = this.getTank().getFluid();
        int lowestY = 255, highestY = 0;

        if (fillStack == null || fillStack.getFluid().isGaseous())
        {
            super.reloadTanks();
        }
        else if (this.getNodes().size() > 0)
        {
            fillStack = fillStack.copy();
            for (IFluidPart part : this.getConnectors())
            {
                part.getInternalTank().setFluid(null);
                if (part instanceof TileEntity && ((TileEntity) part).yCoord < lowestY)
                {
                    lowestY = ((TileEntity) part).yCoord;
                }
                if (part instanceof TileEntity && ((TileEntity) part).yCoord > highestY)
                {
                    highestY = ((TileEntity) part).yCoord;
                }
            }

            //TODO Add path finder to prevent filling when tanks are only connected at the top
            for (int y = lowestY; y <= highestY; y++)
            {               
                List<IFluidPart> parts = new ArrayList<IFluidPart>();
               
                for (IFluidPart part : this.getConnectors())
                {
                    if (part instanceof IFluidPart && ((TileEntity) part).yCoord == y)
                    {
                        parts.add((IFluidPart) part);
                    }
                }
                if (!parts.isEmpty())
                {
                    //TODO change this to use a percent system for even filling 
                    int partCount = parts.size();
                    for (IFluidPart part : parts)
                    {
                        fillStack.amount -= part.getInternalTank().fill(FluidHelper.getStack(fillStack, fillStack.amount / partCount), true);
                        if (partCount > 1)
                            partCount--;
                    }
                }

                if (fillStack == null || fillStack.amount <= 0)
                {
                    break;
                }
            }
        }
    }
}
