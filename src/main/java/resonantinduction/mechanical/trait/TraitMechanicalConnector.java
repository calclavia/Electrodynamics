package resonantinduction.mechanical.trait;

import java.util.HashSet;
import java.util.Set;

import net.minecraftforge.common.ForgeDirection;
import resonantinduction.mechanical.network.IMechanical;
import resonantinduction.mechanical.network.IMechanicalConnector;
import resonantinduction.mechanical.network.IMechanicalNetwork;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;

public class TraitMechanicalConnector extends TileMultipart implements IMechanicalConnector
{
	public Set<IMechanicalConnector> mechanicalConnectorInterfaces = new HashSet<IMechanicalConnector>();

	@Override
	public void copyFrom(TileMultipart that)
	{
		super.copyFrom(that);

		if (that instanceof TraitMechanicalConnector)
		{
			this.mechanicalConnectorInterfaces = ((TraitMechanicalConnector) that).mechanicalConnectorInterfaces;
		}
	}

	@Override
	public void bindPart(TMultiPart part)
	{
		super.bindPart(part);

		if (part instanceof IMechanicalConnector)
		{
			this.mechanicalConnectorInterfaces.add((IMechanicalConnector) part);
		}
	}

	@Override
	public void partRemoved(TMultiPart part, int p)
	{
		super.partRemoved(part, p);

		if (part instanceof IMechanicalConnector)
		{
			this.mechanicalConnectorInterfaces.remove(part);
		}
	}

	@Override
	public void clearParts()
	{
		super.clearParts();
		this.mechanicalConnectorInterfaces.clear();
	}

	@Override
	public boolean canConnect(ForgeDirection direction)
	{
		for (IMechanicalConnector connector : this.mechanicalConnectorInterfaces)
		{
			if (connector.canConnect(direction.getOpposite()))
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public long onReceiveEnergy(ForgeDirection from, long torque, float angularVelocity, boolean doReceive)
	{
		TMultiPart part = this.partMap(from.ordinal());

		if (part != null)
		{
			if (this.mechanicalConnectorInterfaces.contains(part))
			{
				return ((IMechanicalConnector) part).onReceiveEnergy(from, torque, angularVelocity, doReceive);
			}
		}

		return 0;
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
}
