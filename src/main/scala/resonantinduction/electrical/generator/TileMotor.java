package resonantinduction.electrical.generator;

import java.util.EnumSet;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import resonant.api.IRotatable;
import resonant.api.grid.INode;
import resonant.api.grid.INodeProvider;
import resonant.lib.grid.NodeRegistry;
import resonant.lib.prefab.tile.TileElectrical;
import resonantinduction.core.interfaces.IMechanicalNode;
import universalelectricity.api.energy.EnergyStorageHandler;

/**
 * A kinetic energy to electrical energy converter.
 * 
 * @author Calclavia
 */
public class TileMotor extends TileElectrical implements IRotatable, INodeProvider
{
	protected IMechanicalNode node;

	/** Generator turns KE -> EE. Inverted one will turn EE -> KE. */
	public boolean isInversed = true;
	private byte gearRatio;

	public TileMotor()
	{
		energy = new EnergyStorageHandler(1000000);
		node = NodeRegistry.get(this, IMechanicalNode.class);

		if (node != null)
			node.setLoad(0.5);
	}

	public byte toggleGearRatio()
	{
		return gearRatio = (byte) ((gearRatio + 1) % 3);
	}

	@Override
	public void initiate()
	{
		super.initiate();
		if (node != null)
		    node.reconstruct();
	}

	@Override
	public void invalidate()
	{
		node.deconstruct();
		super.invalidate();
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (node != null)
		{
		    node.update(0.05f);

			if (!isInversed)
			{
				receiveMechanical();
				produce();
			}
			else
			{
				produceMechanical();
			}
		}
	}

	public void receiveMechanical()
	{
		double power = node.getEnergy();

		long receive = energy.receiveEnergy((long) power, true);

		if (receive > 0)
		{
			double percentageUsed = receive / power;
			node.apply(this, -node.getTorque() * percentageUsed, -node.getAngularSpeed() * percentageUsed);
		}
	}

	public void produceMechanical()
	{
		long extract = energy.extractEnergy(energy.getEnergy(), false);

		if (extract > 0)
		{
			long torqueRatio = (long) ((gearRatio + 1) / 2.2d * (extract));

			if (torqueRatio > 0)
			{
				final double maxAngularVelocity = extract / (float) torqueRatio;

				final double maxTorque = (extract) / maxAngularVelocity;

				double setAngularVelocity = maxAngularVelocity;
				double setTorque = maxTorque;

				double currentTorque = Math.abs(node.getTorque());

				if (currentTorque != 0)
					setTorque = Math.min(setTorque, maxTorque) * (node.getTorque() / currentTorque);

				double currentVelo = Math.abs(node.getAngularSpeed());
				if (currentVelo != 0)
					setAngularVelocity = Math.min(+setAngularVelocity, maxAngularVelocity) * (node.getAngularSpeed() / currentVelo);

				node.apply(this, setTorque - node.getTorque(), setAngularVelocity - node.getAngularSpeed());
				energy.extractEnergy((long) Math.abs(setTorque * setAngularVelocity), true);
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

	@Override
	public INode getNode(Class<? extends INode> nodeType, ForgeDirection from)
	{
		if (from == getDirection() || from == getDirection().getOpposite())
		{
			if (nodeType.isAssignableFrom(node.getClass()))
				return node;
		}

		return null;
	}
	
	 @Override
	    public String toString()
	    {
	        return "[TileMotor]" + x() + "x " + y() + "y " + z() + "z ";
	    }
}
