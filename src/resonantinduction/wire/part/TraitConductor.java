package resonantinduction.wire.part;

import java.util.HashSet;
import java.util.Set;

import net.minecraftforge.common.ForgeDirection;
import universalelectricity.api.energy.IConductor;
import universalelectricity.api.energy.IEnergyNetwork;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;

public class TraitConductor extends TileMultipart implements IConductor
{
	public Set<IConductor> interfaces = new HashSet<IConductor>();

	@Override
	public void copyFrom(TileMultipart that)
	{
		super.copyFrom(that);

		if (that instanceof TraitConductor)
		{
			this.interfaces = ((TraitConductor) that).interfaces;
		}
	}

	@Override
	public void bindPart(TMultiPart part)
	{
		super.bindPart(part);

		if (part instanceof IConductor)
		{
			this.interfaces.add((IConductor) part);
		}
	}

	@Override
	public void partRemoved(TMultiPart part, int p)
	{
		super.partRemoved(part, p);

		if (part instanceof IConductor)
		{
			this.interfaces.remove(part);
		}
	}

	@Override
	public void clearParts()
	{
		super.clearParts();
		this.interfaces.clear();
	}

	@Override
	public Object[] getConnections()
	{
		return null;
	}

	@Override
	public IEnergyNetwork getNetwork()
	{
		return null;
	}

	@Override
	public void setNetwork(IEnergyNetwork network)
	{

	}

	@Override
	public boolean canConnect(ForgeDirection direction)
	{
		for (IConductor conductor : this.interfaces)
		{
			if (conductor.canConnect(direction))
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public long onReceiveEnergy(ForgeDirection from, long receive, boolean doReceive)
	{
		/*TMultiPart part = partMap(from.ordinal());

		if (part != null)
		{
			for (IConductor conductor : this.interfaces)
			{
				if (conductor == part)
				{
					conductor.onReceiveEnergy(from, receive, doReceive);
				}
			}
		}
*/
		return 0;
	}

	@Override
	public long onExtractEnergy(ForgeDirection from, long extract, boolean doExtract)
	{
		return 0;
	}

	@Override
	public long getEnergyLoss()
	{
		return 0;
	}

	@Override
	public long getEnergyCapacitance()
	{
		return 0;
	}

}
