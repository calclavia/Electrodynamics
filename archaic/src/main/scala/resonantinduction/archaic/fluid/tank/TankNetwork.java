package resonantinduction.archaic.fluid.tank;

import java.util.HashMap;
import java.util.LinkedList;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidStack;
import resonantinduction.core.fluid.FluidDistributionetwork;
import resonantinduction.core.fluid.IFluidDistribution;

/** Network that handles connected tanks
 * 
 * @author DarkGuardsman */
public class TankNetwork extends FluidDistributionetwork
{
    public TankNetwork()
    {
        super();
        needsUpdate = true;
    }

    @Override
    public void update()
    {
        final FluidStack networkTankFluid = getTank().getFluid();
        int lowestY = 255;
        int highestY = 0;
        int connectorCount = 0;
        int totalFluid = networkTankFluid != null ? networkTankFluid.amount : 0;

        //If we only have one tank only fill one tank

        if (getConnectors().size() > 0)
        {

            IFluidDistribution tank = ((IFluidDistribution) getConnectors().toArray()[0]);
            if (getConnectors().size() == 1)
            {
                tank.getInternalTank().setFluid(networkTankFluid);
                tank.onFluidChanged();
                needsUpdate = false;
                return;
            }
            if (networkTankFluid != null)
            {
                //If fluid is gaseous fill all tanks equally
                if (networkTankFluid.getFluid().isGaseous())
                {
                    connectorCount = this.getConnectors().size();
                    for (IFluidDistribution connector : this.getConnectors())
                    {
                        FluidStack input = networkTankFluid.copy();
                        input.amount = (totalFluid / connectorCount) + (totalFluid % connectorCount);
                        connector.getInternalTank().setFluid(null);
                        totalFluid -= connector.getInternalTank().fill(input, true);
                        connector.onFluidChanged();

                        if (connectorCount > 0)
                            connectorCount--;
                    }
                }
                else
                {
                    HashMap<Integer, LinkedList<IFluidDistribution>> heightMap = new HashMap<Integer, LinkedList<IFluidDistribution>>();

                    //Build map of all tanks by their y level
                    for (IFluidDistribution connector : this.getConnectors())
                    {
                        if (connector instanceof TileEntity)
                        {
                            LinkedList<IFluidDistribution> list = new LinkedList<IFluidDistribution>();
                            int yCoord = ((TileEntity) connector).yCoord;

                            if (yCoord < lowestY)
                            {
                                lowestY = yCoord;
                            }

                            if (yCoord > highestY)
                            {
                                highestY = yCoord;
                            }

                            if (heightMap.containsKey(yCoord))
                            {
                                list = heightMap.get(yCoord);
                            }
                            list.add(connector);
                            heightMap.put(yCoord, list);
                        }
                    }

                    //Loop threw levels
                    for (int yLevel = lowestY; yLevel <= highestY; yLevel++)
                    {
                        if (heightMap.containsKey(yLevel))
                        {
                            connectorCount = heightMap.get(yLevel).size();

                            if (connectorCount <= 0)
                                continue;
                            //Loop threw tanks in each level
                            for (IFluidDistribution connector : heightMap.get(yLevel))
                            {
                                //If tank is empty clear internal and move on
                                if (totalFluid <= 0)
                                {
                                    connector.getInternalTank().setFluid(null);
                                    connector.onFluidChanged();
                                    continue;
                                }

                                FluidStack input = networkTankFluid.copy();
                                input.amount = (totalFluid / connectorCount) + (totalFluid % connectorCount);
                                connector.getInternalTank().setFluid(null);
                                totalFluid -= connector.getInternalTank().fill(input, true);
                                connector.onFluidChanged();

                                if (connectorCount > 1)
                                    connectorCount--;

                            }
                        }
                    }
                }
            }
            else
            {
                //In the cases the tank is empty just clear all tanks
                //instead of doing additional logic that is wasting ticks
                for (IFluidDistribution connector : this.getConnectors())
                {
                    connector.getInternalTank().setFluid(null);
                    connector.onFluidChanged();
                }
            }
        }
        needsUpdate = false;
    }

    @Override
    public TankNetwork newInstance()
    {
        return new TankNetwork();
    }
}
