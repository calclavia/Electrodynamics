package resonantinduction.electrical.generator;

import java.util.EnumSet;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.mechanical.network.IMechanical;
import resonantinduction.mechanical.network.IMechanicalNetwork;
import resonantinduction.mechanical.network.MechanicalNetwork;
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
	public boolean isInversed = false;
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
			// energy.receiveEnergy(getNetwork().getPower(), true);
			produce();
		}
		else
		{
			produceMechanical(this.getDirection());
			produceMechanical(this.getDirection().getOpposite());
		}

	}

	public void produceMechanical(ForgeDirection outputDir)
	{
		Vector3 outputVector = new Vector3(this).translate(outputDir);
		TileEntity tile = outputVector.getTileEntity(worldObj);

		if (tile instanceof IMechanical)
		{
			IMechanical mech = ((IMechanical) tile).getInstance(outputDir.getOpposite());
			long extract = energy.extractEnergy(false);

			if (extract > 0)
			{
				final float maxAngularVelocity = energy.getEnergyCapacity() / (float) torqueRatio;
				final long maxTorque = (long) ((double) energy.getEnergyCapacity() / maxAngularVelocity);
				float addAngularVelocity = extract / (float) torqueRatio;
				long addTorque = (long) (((double) extract) / addAngularVelocity);
				long setTorque = Math.min(mech.getTorque() + addTorque, maxTorque);
				float setAngularVelocity = Math.min(mech.getAngularVelocity() + addAngularVelocity, maxAngularVelocity);
				mech.setTorque(setTorque);
				mech.setAngularVelocity(setAngularVelocity);
				energy.extractEnergy((long) (setTorque * setAngularVelocity), true);
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
