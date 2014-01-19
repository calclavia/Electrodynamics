package resonantinduction.electrical.generator;

import java.util.EnumSet;

import net.minecraft.nbt.NBTTagCompound;
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
	/** Generator turns KE -> EE. Inverted one will turn EE -> KE. */
	public boolean isInversed = false;

	private float torqueRatio = 8000;

	public TileGenerator()
	{
		energy = new EnergyStorageHandler(10000);
		this.ioMap = 728;
	}

	public float toggleRatio()
	{
		return torqueRatio = (torqueRatio + 1000) % energy.getMaxExtract();
	}

	@Override
	public void updateEntity()
	{
		if (this.isFunctioning())
		{
			if (!isInversed)
			{
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
					{
						float angularVelocity = extract / torqueRatio;
						long torque = (long) (extract / angularVelocity);
						((IMechanical) mechanical).onReceiveEnergy(getOuputDirection().getOpposite(), torque, angularVelocity, true);
					}
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
	public long onReceiveEnergy(ForgeDirection from, long torque, float angularVelocity, boolean doReceive)
	{
		return energy.receiveEnergy((long) (torque * angularVelocity), doReceive);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		isInversed = nbt.getBoolean("isInversed");
		torqueRatio = nbt.getFloat("torqueRatio");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setBoolean("isInversed", isInversed);
		nbt.setFloat("torqueRatio", torqueRatio);
	}

	@Override
	public boolean isClockwise()
	{
		return false;
	}

	@Override
	public void setRotation(boolean isClockwise)
	{
		
	}
}
