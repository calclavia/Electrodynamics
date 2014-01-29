package resonantinduction.electrical.battery;

import java.util.Arrays;

import universalelectricity.core.net.Network;

public class BatteryNetwork extends Network<BatteryNetwork, TileBattery>
{
	public void redistribute(TileBattery... exclusion)
	{
		long totalEnergy = 0;
		long totalCapacity = 0;

		for (TileBattery battery : this.getConnectors())
		{
			totalEnergy += battery.energy.getEnergy();
			totalCapacity += battery.energy.getEnergyCapacity();
		}

		/**
		 * Apply energy loss.
		 */
		double percentageLoss = Math.max(0, (1 - ((double) (getConnectors().size() * 6) / 100d)));
		long energyLoss = (long) (percentageLoss * 10);
		totalEnergy -= energyLoss;

		int amountOfNodes = this.getConnectors().size() - exclusion.length;

		if (totalEnergy > 0 && amountOfNodes > 0)
		{
			long remainingEnergy = totalEnergy;

			TileBattery firstNode = this.getFirstConnector();

			for (TileBattery battery : this.getConnectors())
			{
				if (battery != firstNode && !Arrays.asList(exclusion).contains(battery))
				{
					double percentage = ((double) battery.energy.getEnergyCapacity() / (double) totalCapacity);
					long energyForBattery = (long) Math.round(totalEnergy * percentage);
					battery.energy.setEnergy(energyForBattery);
					remainingEnergy -= energyForBattery;
				}
			}

			firstNode.energy.setEnergy(remainingEnergy);
		}
	}

	@Override
	protected void reconstructConnector(TileBattery node)
	{
		node.setNetwork(this);
	}

	@Override
	public BatteryNetwork newInstance()
	{
		return new BatteryNetwork();
	}
}
