package resonantinduction.electrical.generator;

import resonantinduction.mechanical.network.IMechanical;
import universalelectricity.api.energy.EnergyStorageHandler;
import calclavia.lib.prefab.tile.TileElectrical;

/**
 * A kinetic energy to electrical energy converter.
 * 
 * @author Calclavia
 * 
 */
public class TileGenerator extends TileElectrical implements IMechanical
{
	private long power;

	/** Generator turns KE -> EE. Inverted one will turn EE -> KE. */
	private boolean isInversed = false;

	public TileGenerator()
	{
		energy = new EnergyStorageHandler(10000);
	}

	@Override
	public void updateEntity()
	{
		if (this.isFunctioning())
		{
			if (!isInversed)
			{
				this.energy.receiveEnergy(power, true);
				this.produce();
			}
			else
			{
				// TODO:Do something here to set mechanical energy.
			}
		}
	}

	private boolean isFunctioning()
	{
		return true;
	}

	@Override
	public void setKineticEnergy(long energy)
	{
		this.power = energy;
	}
}
