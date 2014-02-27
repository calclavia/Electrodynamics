package resonantinduction.mechanical.energy.turbine;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.api.mechanical.IMechanical;
import resonantinduction.api.mechanical.IMechanicalNetwork;
import resonantinduction.mechanical.energy.network.MechanicalNetwork;
import universalelectricity.api.energy.EnergyStorageHandler;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.prefab.turbine.TileTurbine;

public class TileMechanicalTurbine extends TileTurbine implements IMechanical
{
	public TileMechanicalTurbine()
	{
		super();
		energy = new EnergyStorageHandler(0);
	}

	/**
	 * Mechanical Methods
	 * 
	 * @return The connections.
	 */
	@Override
	public Object[] getConnections()
	{
		Object[] connections = new Object[6];

		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
		{
			TileEntity tile = new Vector3(this).translate(dir).getTileEntity(worldObj);

			if (tile instanceof IMechanical)
			{
				IMechanical mech = ((IMechanical) tile).getInstance(dir.getOpposite());

				// Don't connect with shafts
				if (mech != null && canConnect(dir, this) && mech.canConnect(dir.getOpposite(), this))
				{
					connections[dir.ordinal()] = mech;
					getNetwork().merge(mech.getNetwork());
				}
			}
		}

		return connections;
	}

	private IMechanicalNetwork network;

	@Override
	public IMechanicalNetwork getNetwork()
	{
		if (this.network == null)
		{
			this.network = new MechanicalNetwork();
			this.network.addConnector(this);
		}
		return this.network;
	}

	@Override
	public void setNetwork(IMechanicalNetwork network)
	{
		this.network = network;
	}

	@Override
	public float getRatio(ForgeDirection dir, Object source)
	{
		return 0.5f;
	}

	@Override
	public float getAngularVelocity()
	{
		return angularVelocity;
	}

	@Override
	public void setAngularVelocity(float velocity)
	{
		this.angularVelocity = velocity;
	}

	@Override
	public void setTorque(long torque)
	{
		this.torque = torque;
	}

	@Override
	public boolean inverseRotation(ForgeDirection dir, IMechanical with)
	{
		return true;
	}

	@Override
	public IMechanical getInstance(ForgeDirection dir)
	{
		return (IMechanical) getMultiBlock().get();
	}

	@Override
	public boolean canConnect(ForgeDirection from, Object source)
	{
		if (source instanceof IMechanical && !(source instanceof TileMechanicalTurbine))
		{
			/**
			 * Face to face stick connection.
			 */
			TileEntity sourceTile = getPosition().translate(from).getTileEntity(getWorld());

			if (sourceTile instanceof IMechanical)
			{
				IMechanical sourceInstance = ((IMechanical) sourceTile).getInstance(from.getOpposite());
				return sourceInstance == source && from == getDirection().getOpposite();
			}
		}
		return false;
	}
}
