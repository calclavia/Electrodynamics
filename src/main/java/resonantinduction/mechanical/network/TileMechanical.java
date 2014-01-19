package resonantinduction.mechanical.network;

import net.minecraftforge.common.ForgeDirection;
import calclavia.lib.prefab.tile.TileAdvanced;

public class TileMechanical extends TileAdvanced implements IMechanicalConnector
{
	/** The mechanical connections this connector has made */
	protected Object[] connections = new Object[6];

	private IMechanicalNetwork network;

	@Override
	public long onReceiveEnergy(ForgeDirection from, long torque, float angularVelocity, boolean doReceive)
	{
		return this.getNetwork().onReceiveEnergy(torque, angularVelocity);
	}

	@Override
	public boolean canConnect(ForgeDirection direction)
	{
		return false;
	}

	@Override
	public Object[] getConnections()
	{
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
	public boolean sendNetworkPacket(long torque, float angularVelocity)
	{
		return false;
	}

	@Override
	public float getResistance()
	{
		return 0;
	}
}
