package resonantinduction.old.energy.battery;

import java.util.Arrays;

import calclavia.lib.multiblock.structure.Structure;

public class BatteryStructure extends Structure<TileBattery>
{
	public void redistribute(TileBattery... exclusion)
	{
		long totalEnergy = 0;

		for (TileBattery battery : this.get())
		{
			totalEnergy += battery.getEnergy(null);
		}

		int amountOfNodes = this.get().size() - exclusion.length;

		if (totalEnergy > 0 && amountOfNodes > 0)
		{
			long totalPerBattery = totalEnergy / amountOfNodes;
			long totalPerBatteryRemainder = totalPerBattery + totalEnergy % amountOfNodes;

			TileBattery firstNode = this.getFirstNode();

			for (TileBattery battery : this.get())
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
	public Structure getNew()
	{
		return new BatteryStructure();
	}

	@Override
	protected void refreshNode(TileBattery node)
	{
		node.setNetwork(this);
	}
}
