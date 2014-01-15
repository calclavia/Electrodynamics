package resonantinduction.mechanical.fluid.tank;

import java.util.LinkedHashSet;
import java.util.Set;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidStack;
import resonantinduction.api.fluid.IFluidPart;
import resonantinduction.mechanical.fluid.network.FluidNetwork;
import universalelectricity.core.net.NetworkTickHandler;
import calclavia.lib.utility.FluidUtility;

/** Network that handles connected tanks
 * 
 * @author DarkGuardsman */
public class TankNetwork extends FluidNetwork
{
    public TankNetwork()
    {
        NetworkTickHandler.addNetwork(this);
    }

    @Override
    public void reloadTanks()
    {
        System.out.println("TankNetwork: Balancing fluid");
        FluidStack fillStack = this.getTank().getFluid();
        int lowestY = 255, highestY = 0;

        if (fillStack == null || fillStack.getFluid().isGaseous())
        {
            System.out.println("TankNetwork: Stack is null or a gas");
            super.reloadTanks();
        }
        else if (this.getConnectors().size() > 0)
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
                Set<IFluidPart> parts = new LinkedHashSet<IFluidPart>();

                for (IFluidPart part : this.getConnectors())
                {
                    if (part instanceof IFluidPart && ((TileEntity) part).yCoord == y)
                    {
                        parts.add((IFluidPart) part);
                    }
                }
                if (!parts.isEmpty())
                {
                    System.out.println("TankNetwork: balancing level: " + y);
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
