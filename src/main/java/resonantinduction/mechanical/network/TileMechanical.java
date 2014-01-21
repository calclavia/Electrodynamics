package resonantinduction.mechanical.network;

import net.minecraftforge.common.ForgeDirection;
import calclavia.lib.prefab.tile.TileAdvanced;

public abstract class TileMechanical extends TileAdvanced implements IMechanical
{
	/** The mechanical connections this connector has made */
	protected Object[] connections = new Object[6];

	private IMechanicalNetwork network;

	private boolean isClockwise = false;

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
	public int[] getLocation()
	{
		return new int[] { xCoord, yCoord, zCoord, 0 };
	}

	@Override
	public float getResistance()
	{
		return 0;
	}

	@Override
	public boolean isClockwise()
	{
		return isClockwise;
	}

	@Override
	public void setClockwise(boolean isClockwise)
	{
		this.isClockwise = isClockwise;
	}

	@Override
	public boolean isRotationInversed()
	{
		return true;
	}

	@Override
	public IMechanical getInstance(ForgeDirection from)
	{
		return this;
	}
}
