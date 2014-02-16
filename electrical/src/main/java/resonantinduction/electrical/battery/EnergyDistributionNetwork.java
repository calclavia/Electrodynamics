package resonantinduction.electrical.battery;

import java.util.Arrays;

import universalelectricity.core.net.Network;

public class EnergyDistributionNetwork extends Network<EnergyDistributionNetwork, TileEnergyDistribution>
{
	public void redistribute(TileEnergyDistribution... exclusion)
	{
		long totalEnergy = 0;
		long totalCapacity = 0;

		for (TileEnergyDistribution energyContainer : this.getConnectors())
		{
			totalEnergy += energyContainer.energy.getEnergy();
			totalCapacity += energyContainer.energy.getEnergyCapacity();
		}

		/**
		 * Apply energy loss.
		 */
		double percentageLoss = Math.max(0, (1 - (getConnectors().size() * 6 / 100d)));
		long energyLoss = (long) (percentageLoss * 100);
		totalEnergy -= energyLoss;

		int amountOfNodes = this.getConnectors().size() - exclusion.length;

		if (totalEnergy > 0 && amountOfNodes > 0)
		{
			long remainingEnergy = totalEnergy;

			TileEnergyDistribution firstNode = this.getFirstConnector();

			for (TileEnergyDistribution node : this.getConnectors())
			{
				if (node != firstNode && !Arrays.asList(exclusion).contains(node))
				{
					double percentage = ((double) node.energy.getEnergyCapacity() / (double) totalCapacity);
					long energyForBattery = Math.round(totalEnergy * percentage);
					node.energy.setEnergy(energyForBattery);
					remainingEnergy -= energyForBattery;
				}
			}

			firstNode.energy.setEnergy(remainingEnergy);
		}
	}

	@Override
	protected void reconstructConnector(TileEnergyDistribution node)
	{
		node.setNetwork(this);
	}

	@Override
	public EnergyDistributionNetwork newInstance()
	{
		return new EnergyDistributionNetwork();
	}
}
