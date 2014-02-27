package resonantinduction.electrical.generator;

import java.util.EnumSet;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.api.mechanical.IMechanical;
import resonantinduction.api.mechanical.IMechanicalNetwork;
import universalelectricity.api.energy.EnergyStorageHandler;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.prefab.tile.IRotatable;
import calclavia.lib.prefab.tile.TileElectrical;

/**
 * A kinetic energy to electrical energy converter.
 * 
 * @author Calclavia
 */
public class TileGenerator extends TileElectrical implements IRotatable
{
	private IMechanicalNetwork network;

	/** Generator turns KE -> EE. Inverted one will turn EE -> KE. */
	public boolean isInversed = true;
	private byte gearRatio;

	public TileGenerator()
	{
		energy = new EnergyStorageHandler(1000000);
	}

	public byte toggleGearRatio()
	{
		return gearRatio = (byte) ((gearRatio + 1) % 3);
	}

	@Override
	public void updateEntity()
	{
		if (!isInversed)
		{
			receiveMechanical(getDirection());
			receiveMechanical(getDirection().getOpposite());
			produce();
		}
		else
		{
			produceMechanical(getDirection());
			produceMechanical(getDirection().getOpposite());
		}

	}

	public void receiveMechanical(ForgeDirection inputDir)
	{
		Vector3 inputVector = new Vector3(this).translate(inputDir);
		TileEntity tile = inputVector.getTileEntity(worldObj);

		if (tile instanceof IMechanical)
		{
			IMechanical mech = ((IMechanical) tile).getInstance(inputDir.getOpposite());

			if (mech != null)
			{
				long power = (long) Math.abs(mech.getTorque() * mech.getAngularVelocity()) / 2;
				long receive = energy.receiveEnergy(power, true);

				if (receive > 0)
				{
					double percentageUsed = (double) receive / (double) power;
					mech.setTorque((long) (mech.getTorque() - (mech.getTorque() * percentageUsed)));
					mech.setAngularVelocity((float) (mech.getAngularVelocity() - (mech.getAngularVelocity() * percentageUsed)));
				}
			}
		}
	}

	public void produceMechanical(ForgeDirection outputDir)
	{
		Vector3 outputVector = new Vector3(this).translate(outputDir);
		TileEntity tile = outputVector.getTileEntity(worldObj);

		if (tile instanceof IMechanical)
		{
			IMechanical mech = ((IMechanical) tile).getInstance(outputDir.getOpposite());
			long extract = energy.extractEnergy(energy.getEnergy() / 2, false);

			if (mech != null)
			{
				if (extract > 0)
				{
					long torqueRatio = (long) ((gearRatio + 1) / 4d * (extract));

					if (torqueRatio > 0)
					{
						final float maxAngularVelocity = extract / (float) torqueRatio;

						final long maxTorque = (long) (((double) extract) / maxAngularVelocity);

						float setAngularVelocity = maxAngularVelocity;
						long setTorque = maxTorque;

						long currentTorque = Math.abs(mech.getTorque());

						if (currentTorque != 0)
							setTorque = Math.min(setTorque, maxTorque) * (mech.getTorque() / currentTorque);

						float currentVelo = Math.abs(mech.getAngularVelocity());
						if (currentVelo != 0)
							setAngularVelocity = Math.min(+setAngularVelocity, maxAngularVelocity) * (mech.getAngularVelocity() / currentVelo);

						mech.setTorque(setTorque);
						mech.setAngularVelocity(setAngularVelocity);
						energy.extractEnergy((long) Math.abs(setTorque * setAngularVelocity), true);
					}
				}
			}
		}
	}

	@Override
	public EnumSet<ForgeDirection> getInputDirections()
	{
		return getOutputDirections();
	}

	@Override
	public EnumSet<ForgeDirection> getOutputDirections()
	{
		EnumSet<ForgeDirection> dirs = EnumSet.allOf(ForgeDirection.class);
		dirs.remove(this.getDirection());
		dirs.remove(this.getDirection().getOpposite());
		return dirs;
	}

	@Override
	public ForgeDirection getDirection()
	{
		return ForgeDirection.getOrientation(this.getBlockMetadata());
	}

	@Override
	public void setDirection(ForgeDirection dir)
	{
		this.worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, dir.ordinal(), 3);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		isInversed = nbt.getBoolean("isInversed");
		gearRatio = nbt.getByte("gear");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setBoolean("isInversed", isInversed);
		nbt.setByte("gear", gearRatio);
	}
}
