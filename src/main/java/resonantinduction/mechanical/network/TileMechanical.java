package resonantinduction.mechanical.network;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.prefab.tile.TileAdvanced;

public abstract class TileMechanical extends TileAdvanced implements IMechanical
{
	/** The mechanical connections this connector has made */
	protected Object[] connections = new Object[6];
	private IMechanicalNetwork network;
	protected float angularVelocity;
	protected long torque;
	public float angle = 0;

	@Override
	public void updateEntity()
	{
		super.updateEntity();
		angle += angularVelocity / 20;
		torque *= getLoad();
		angularVelocity *= getLoad();
	}

	protected float getLoad()
	{
		return 0.5f;
	}

	@Override
	public void invalidate()
	{
		getNetwork().split(this);
		super.invalidate();
	}

	@Override
	public Object[] getConnections()
	{
		connections = new Object[6];

		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
		{
			TileEntity tile = new Vector3(this).translate(dir).getTileEntity(worldObj);

			if (tile instanceof IMechanical)
			{
				IMechanical mech = (IMechanical) ((IMechanical) tile).getInstance(dir.getOpposite());

				if (mech != null && canConnect(dir) && mech.canConnect(dir.getOpposite()))
				{
					connections[dir.ordinal()] = mech;
					getNetwork().merge(mech.getNetwork());
				}
			}
		}

		return connections;
	}

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
	public long getTorque()
	{
		return torque;
	}

	@Override
	public void setTorque(long torque)
	{
		this.torque = torque;
	}

	@Override
	public float getRatio()
	{
		return 0.5f;
	}

	@Override
	public IMechanical getInstance(ForgeDirection from)
	{
		return this;
	}
}
