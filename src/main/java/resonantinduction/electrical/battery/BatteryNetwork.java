package resonantinduction.electrical.battery;

import java.util.Arrays;

import universalelectricity.core.net.Network;

public class BatteryNetwork extends Network<BatteryNetwork, TileBattery>
{
	public void redistribute(TileBattery... exclusion)
	{
		long totalEnergy = 0;

		for (TileBattery battery : this.getConnectors())
		{
			totalEnergy += battery.getEnergy(null);
		}

		int amountOfNodes = this.getConnectors().size() - exclusion.length;

		if (totalEnergy > 0 && amountOfNodes > 0)
		{
			long totalPerBattery = totalEnergy / amountOfNodes;
			long totalPerBatteryRemainder = totalPerBattery + totalEnergy % amountOfNodes;

			TileBattery firstNode = this.getFirstConnector();

			for (TileBattery battery : this.getConnectors())
			{
				if (!Arrays.asList(exclusion).contains(battery))
				{
					if (battery == firstNode)
					{
						battery.setEnergy(null, totalPerBatteryRemainder);
					}
					else
					{
						battery.setEnergy(null, totalPerBattery);
					}
				}
			}
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
