package resonantinduction.mechanical.fluid.tank;

import java.util.LinkedHashSet;
import java.util.Set;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidStack;
import resonantinduction.api.fluid.IFluidConnector;
import resonantinduction.api.fluid.IFluidNetwork;
import resonantinduction.mechanical.fluid.network.FluidNetwork;

/** Network that handles connected tanks
 * 
 * @author DarkGuardsman */
public class TankNetwork extends FluidNetwork
{
    @Override
    public void distributeConnectors()
    {
        FluidStack fillStack = this.getTank().getFluid();
        int lowestY = 255, highestY = 0;

        if (fillStack == null || fillStack.getFluid().isGaseous())
        {
            super.distributeConnectors();
        }
        else if (this.getConnectors().size() > 0)
        {
            fillStack = fillStack.copy();

            for (IFluidConnector connector : this.getConnectors())
            {
                connector.getInternalTank().setFluid(null);
                connector.onFluidChanged();

                if (connector instanceof TileEntity && ((TileEntity) connector).yCoord < lowestY)
                {
                    lowestY = ((TileEntity) connector).yCoord;
                }
                if (connector instanceof TileEntity && ((TileEntity) connector).yCoord > highestY)
                {
                    highestY = ((TileEntity) connector).yCoord;
                }
            }

            // TODO Add path finder to prevent filling when tanks are only connected at the top
            for (int y = lowestY; y <= highestY; y++)
            {
                Set<IFluidConnector> parts = new LinkedHashSet<IFluidConnector>();

                for (IFluidConnector part : this.getConnectors())
                {
                    if (part instanceof IFluidConnector && ((TileEntity) part).yCoord == y)
                    {
                        parts.add(part);
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

    @Override
    public IFluidNetwork newInstance()
    {
        return new TankNetwork();
    }

    @Override
    public int getPressure()
    {
        //TODO implement a compression system that would cause a tank to build up pressure greater than normal ATM
        return 0;
    }

}
