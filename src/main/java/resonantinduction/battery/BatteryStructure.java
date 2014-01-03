package resonantinduction.battery;

import java.util.Iterator;

import universalelectricity.api.net.IConnector;
import calclavia.lib.multiblock.structure.Structure;

public class BatteryStructure extends Structure<TileBattery>
{
	public void redistribute()
	{
		long totalEnergy = 0;

		for (TileBattery battery : this.get())
		{
			totalEnergy += battery.getEnergy(null);
		}

		long totalPerBattery = totalEnergy / this.get().size();
		long totalPerBatteryRemainder = totalPerBattery + totalEnergy % this.get().size();

		TileBattery firstNode = this.getFirstNode();

		for (TileBattery battery : this.get())
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
