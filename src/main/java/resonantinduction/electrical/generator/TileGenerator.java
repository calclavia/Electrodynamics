package resonantinduction.electrical.generator;

import net.minecraftforge.common.ForgeDirection;
import resonantinduction.mechanical.network.IMechanical;
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
		energy = new EnergyStorageHandler(10000);
		this.ioMap = 728;
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
	public void setPower(long torque, float speed)
	{
		this.power = (long) Math.abs(torque * speed);
	}

	@Override
	public void onTorqueChange(ForgeDirection side, int speed)
	{
	}

	@Override
	public int getForceSide(ForgeDirection side)
	{
		return 0;
	}

}
