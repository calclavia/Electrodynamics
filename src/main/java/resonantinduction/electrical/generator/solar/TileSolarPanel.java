package resonantinduction.electrical.generator.solar;

import universalelectricity.api.energy.EnergyStorageHandler;
import calclavia.lib.prefab.tile.TileElectrical;

public class TileSolarPanel extends TileElectrical
{
	public TileSolarPanel()
	{
		this.energy = new EnergyStorageHandler(200);
		this.ioMap = 728;
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (!this.worldObj.isRemote)
		{
			if (this.worldObj.canBlockSeeTheSky(xCoord, yCoord + 1, zCoord) && !this.worldObj.provider.hasNoSky)
			{
				if (this.worldObj.isDaytime())
				{
					if (!(this.worldObj.isThundering() || this.worldObj.isRaining()))
					{
						this.energy.receiveEnergy(1, true);
						this.produce();
					}
				}
			}
		}
	}

}
