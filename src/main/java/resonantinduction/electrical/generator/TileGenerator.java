package resonantinduction.electrical.generator;

import java.util.EnumSet;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.mechanical.network.IMechanical;
import resonantinduction.mechanical.network.IMechanicalNetwork;
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
	private int torqueRatio = 8000;

	public TileGenerator()
	{
		energy = new EnergyStorageHandler(10000);
	}

	public float toggleRatio()
	{
		return torqueRatio = (int) ((torqueRatio + 1000) % energy.getMaxExtract() + 1000);
	}

	@Override
	public void updateEntity()
	{

		if (!isInversed)
		{
			receiveMechanical(this.getDirection());
			receiveMechanical(this.getDirection().getOpposite());
			produce();
		}
		else
		{
			produceMechanical(this.getDirection());
			produceMechanical(this.getDirection().getOpposite());
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
				long receive = energy.receiveEnergy((long) Math.abs(mech.getTorque() * mech.getAngularVelocity()), true);

				if (receive > 0)
				{
					mech.setTorque((long) (mech.getTorque() * 0.5));
					mech.setAngularVelocity(mech.getAngularVelocity() * 0.5f);
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
					final float maxAngularVelocity = energy.getEnergyCapacity() / (float) torqueRatio;
					final long maxTorque = (long) ((double) energy.getEnergyCapacity() / maxAngularVelocity);
					float setAngularVelocity = extract / (float) torqueRatio;
					long setTorque = (long) (((double) extract) / setAngularVelocity);

					long currentTorque = Math.abs(mech.getTorque());

					if (currentTorque != 0)
					{
						setTorque = Math.min(+setTorque, maxTorque) * (mech.getTorque() / currentTorque);

						if (setTorque < currentTorque)
						{
							setTorque = (long) Math.max(setTorque, currentTorque * (currentTorque / maxTorque));
						}
					}

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
		torqueRatio = nbt.getInteger("torqueRatio");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setBoolean("isInversed", isInversed);
		nbt.setInteger("torqueRatio", torqueRatio);
	}
}
