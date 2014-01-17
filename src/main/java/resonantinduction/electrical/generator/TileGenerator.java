package resonantinduction.electrical.generator;

import net.minecraftforge.common.ForgeDirection;
import resonantinduction.mechanical.network.IMechanical;
import resonantinduction.mechanical.network.IMechanicalNetwork;
import resonantinduction.mechanical.network.MechanicalNetwork;
import universalelectricity.api.energy.EnergyStorageHandler;
import calclavia.lib.prefab.tile.TileElectrical;

/**
 * A kinetic energy to electrical energy converter.
 * 
 * @author Calclavia
 */
public class TileGenerator extends TileElectrical implements IMechanical
{
	private long power;

	/** Generator turns KE -> EE. Inverted one will turn EE -> KE. */
	public boolean isInversed = false;

	public TileGenerator()
	{
		energy = new EnergyStorageHandler(10000, 100);
		this.ioMap = 728;
	}

	@Override
	public void updateEntity()
	{
		if (this.isFunctioning())
		{
			if (!isInversed)
			{
				this.power -= this.energy.receiveEnergy(power, true);
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
	public void onReceiveEnergy(ForgeDirection from, long torque, float angularVelocity)
	{
		energy.receiveEnergy((long) (torque * angularVelocity), true);
	}
}
