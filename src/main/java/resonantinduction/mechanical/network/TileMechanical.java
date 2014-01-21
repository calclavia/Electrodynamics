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

	private boolean isClockwise = false;

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
