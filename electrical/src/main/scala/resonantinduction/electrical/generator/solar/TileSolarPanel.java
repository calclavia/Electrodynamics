package resonantinduction.electrical.generator.solar;

import resonantinduction.electrical.battery.TileEnergyDistribution;
import universalelectricity.api.energy.EnergyStorageHandler;

public class TileSolarPanel extends TileEnergyDistribution
{
	public TileSolarPanel()
	{
		this.energy = new EnergyStorageHandler(800);
		this.ioMap = 728;
	}

	@Override
	public void updateEntity()
	{
		if (!this.worldObj.isRemote)
		{
			if (this.worldObj.canBlockSeeTheSky(xCoord, yCoord + 1, zCoord) && !this.worldObj.provider.hasNoSky)
			{
				if (this.worldObj.isDaytime())
				{
					if (!(this.worldObj.isThundering() || this.worldObj.isRaining()))
					{
						this.energy.receiveEnergy(25, true);
						markDistributionUpdate |= produce() > 0;
					}
				}
			}
		}

		super.updateEntity();
	}

}
