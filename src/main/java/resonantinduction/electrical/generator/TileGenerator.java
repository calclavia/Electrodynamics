package resonantinduction.electrical.generator;

import java.util.EnumSet;

import net.minecraftforge.common.ForgeDirection;
import resonantinduction.mechanical.network.IMechanical;
import universalelectricity.api.energy.EnergyStorageHandler;
import universalelectricity.api.vector.Vector3;
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
		energy = new EnergyStorageHandler(10000, 1000);
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
				Vector3 outputVector = new Vector3(this).modifyPositionFromSide(getOuputDirection());
				Object mechanical = outputVector.getTileEntity(worldObj);

				if (mechanical instanceof IMechanical)
				{
					long extract = energy.extractEnergy();

					if (extract > 0)
						((IMechanical) mechanical).onReceiveEnergy(getOuputDirection().getOpposite(), (long) (500), extract / 500f);
				}
			}
		}
	}

	@Override
	public EnumSet<ForgeDirection> getInputDirections()
	{
		EnumSet<ForgeDirection> dirs = EnumSet.noneOf(ForgeDirection.class);
		dirs.add(getInputDirection());
		return dirs;
	}

	@Override
	public EnumSet<ForgeDirection> getOutputDirections()
	{
		EnumSet<ForgeDirection> dirs = EnumSet.noneOf(ForgeDirection.class);
		dirs.add(getOuputDirection());
		return dirs;
	}

	public ForgeDirection getInputDirection()
	{
		return ForgeDirection.getOrientation(this.getBlockMetadata()).getOpposite();
	}

	public ForgeDirection getOuputDirection()
	{
		return ForgeDirection.getOrientation(this.getBlockMetadata());
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
