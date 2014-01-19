package resonantinduction.electrical.generator;

import java.util.EnumSet;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.mechanical.network.IMechanical;
import universalelectricity.api.energy.EnergyStorageHandler;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.prefab.tile.IRotatable;
import calclavia.lib.prefab.tile.TileElectrical;

/**
 * A kinetic energy to electrical energy converter.
 * 
 * @author Calclavia
 */
public class TileGenerator extends TileElectrical implements IMechanical, IRotatable
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
				produce();
			}
			else
			{
				produceMechanical(this.getDirection());
				produceMechanical(this.getDirection().getOpposite());
			}
		}
	}

	public void produceMechanical(ForgeDirection outputDir)
	{
		Vector3 outputVector = new Vector3(this).modifyPositionFromSide(outputDir);
		TileEntity mechanical = outputVector.getTileEntity(worldObj);

		if (mechanical instanceof IMechanical)
		{
			long extract = energy.extractEnergy(false);

			if (extract > 0)
			{
				float angularVelocity = extract / torqueRatio;
				long torque = (long) (extract / angularVelocity);
				energy.extractEnergy(((IMechanical) mechanical).onReceiveEnergy(outputDir.getOpposite(), torque, angularVelocity, true), true);
			}
		}
	}

	@Override
	public EnumSet<ForgeDirection> getInputDirections()
	{
		return this.getOutputDirections();
	}

	@Override
	public EnumSet<ForgeDirection> getOutputDirections()
	{
		EnumSet<ForgeDirection> dirs = EnumSet.allOf(ForgeDirection.class);
		dirs.remove(this.getDirection());
		dirs.remove(this.getDirection().ordinal());
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

	private boolean isFunctioning()
	{
		return true;
	}

	@Override
	public long onReceiveEnergy(ForgeDirection from, long torque, float angularVelocity, boolean doReceive)
	{
		if (!this.isInversed)
		{
			return energy.receiveEnergy((long) (torque * angularVelocity), doReceive);
		}
		return 0;
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
	public void setClockwise(boolean isClockwise)
	{

	}

}
