package resonantinduction.mechanical.fluid.tank;

import java.util.LinkedHashSet;
import java.util.Set;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidStack;
import resonantinduction.api.fluid.IFluidConnector;
import resonantinduction.mechanical.fluid.network.FluidNetwork;

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
        else if (this.getConnectors().size() > 0)
        {
            fillStack = fillStack.copy();
            for (IFluidConnector part : this.getConnectors())
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
                Set<IFluidConnector> parts = new LinkedHashSet<IFluidConnector>();

                for (IFluidConnector part : this.getConnectors())
                {
                    if (part instanceof IFluidConnector && ((TileEntity) part).yCoord == y)
                    {
                        parts.add((IFluidConnector) part);
                    }
                }
                if (!parts.isEmpty())
                {
                    this.fillTankSet(fillStack, parts);
                }

                if (fillStack == null || fillStack.amount <= 0)
                {
                    break;
                }
            }
        }
    }
}
