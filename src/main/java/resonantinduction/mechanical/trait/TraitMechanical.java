package resonantinduction.mechanical.trait;

import java.util.HashSet;
import java.util.Set;

import net.minecraftforge.common.ForgeDirection;
import resonantinduction.mechanical.network.IMechanical;
import resonantinduction.mechanical.network.IMechanicalNetwork;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;

public class TraitMechanical extends TileMultipart implements IMechanical
{
	public Set<IMechanical> mechanicalInterfaces = new HashSet<IMechanical>();

	@Override
	public void copyFrom(TileMultipart that)
	{
		super.copyFrom(that);

		if (that instanceof TraitMechanical)
		{
			this.mechanicalInterfaces = ((TraitMechanical) that).mechanicalInterfaces;
		}
	}

	@Override
	public void bindPart(TMultiPart part)
	{
		super.bindPart(part);

		if (part instanceof IMechanical)
		{
			this.mechanicalInterfaces.add((IMechanical) part);
		}
	}

	@Override
	public void partRemoved(TMultiPart part, int p)
	{
		super.partRemoved(part, p);

		if (part instanceof IMechanical)
		{
			this.mechanicalInterfaces.remove(part);
		}
	}

	@Override
	public void clearParts()
	{
		super.clearParts();
		this.mechanicalInterfaces.clear();
	}

	@Override
	public boolean canConnect(ForgeDirection direction)
	{
		for (IMechanical connector : this.mechanicalInterfaces)
		{
			if (connector.canConnect(direction.getOpposite()))
			{
				return true;
			}
		}

		return false;
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

	@Override
	public Object[] getConnections()
	{
		return null;
	}

	@Override
	public IMechanicalNetwork getNetwork()
	{
		return null;
	}

	@Override
	public void setNetwork(IMechanicalNetwork network)
	{

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

	@Override
	public boolean isRotationInversed()
	{
		return false;
	}
}
