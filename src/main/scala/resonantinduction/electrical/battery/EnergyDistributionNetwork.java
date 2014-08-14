package resonantinduction.electrical.battery;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import universalelectricity.core.grid.Grid;
import universalelectricity.core.grid.node.NodeEnergy;

/** Energy network designed to allow several tiles to act as if they share the same energy
 * level */
public class EnergyDistributionNetwork extends Grid<TileEnergyDistribution>
{
    public long totalEnergy = 0;
    public long totalCapacity = 0;

    public EnergyDistributionNetwork()
    {
        super(NodeEnergy.class);
    }

    public void redistribute(TileEnergyDistribution... exclusion)
    {
        int lowestY = 255, highestY = 0;

        totalEnergy = 0;
        totalCapacity = 0;

        for (TileEnergyDistribution connector : this.getNodes())
        {
            totalEnergy += connector.energy().getEnergy();
            totalCapacity += connector.energy().getEnergyCapacity();

            lowestY = Math.min(connector.yCoord, lowestY);
            highestY = Math.max(connector.yCoord, highestY);

            connector.renderEnergyAmount = 0;
        }

        /** Apply render */
        long remainingRenderEnergy = totalEnergy;

        for (int y = lowestY; y <= highestY; y++)
        {
            Set<TileEnergyDistribution> connectorsInlevel = new LinkedHashSet<TileEnergyDistribution>();

            for (TileEnergyDistribution connector : this.getNodes())
            {
                if (connector.yCoord == y)
                {
                    connectorsInlevel.add(connector);
                }
            }

            int levelSize = connectorsInlevel.size();
            long used = 0;

            for (TileEnergyDistribution connector : connectorsInlevel)
            {
                double tryInject = Math.min(remainingRenderEnergy / levelSize, connector.energy().getEnergyCapacity());
                connector.renderEnergyAmount = tryInject;
                used += tryInject;
            }

            remainingRenderEnergy -= used;

            if (remainingRenderEnergy <= 0)
                break;
        }

        /** Apply energy loss. */
        double percentageLoss = 0;// Math.max(0, (1 - (getConnectors().size() * 6 / 100d)));
        long energyLoss = (long) (percentageLoss * 100);
        totalEnergy -= energyLoss;

        int amountOfNodes = this.getNodes().size() - exclusion.length;

        if (totalEnergy > 0 && amountOfNodes > 0)
        {
            long remainingEnergy = totalEnergy;

            TileEnergyDistribution firstNode = this.getFirstNode();

            for (TileEnergyDistribution node : this.getNodes())
            {
                if (node != firstNode && !Arrays.asList(exclusion).contains(node))
                {
                    double percentage = ((double) node.energy().getEnergyCapacity() / (double) totalCapacity);
                    long energyForBattery = Math.max(Math.round(totalEnergy * percentage), 0);
                    node.energy().setEnergy(energyForBattery);
                    remainingEnergy -= energyForBattery;
                }
            }

            firstNode.energy().setEnergy(Math.max(remainingEnergy, 0));
        }
    }
}
