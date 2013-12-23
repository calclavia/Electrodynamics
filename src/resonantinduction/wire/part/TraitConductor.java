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
		for (IConductor conductor : this.interfaces)
		{
			return conductor.getConnections();
		}

		return null;
	}

	@Override
	public IEnergyNetwork getNetwork()
	{
		for (IConductor conductor : this.interfaces)
		{
			return conductor.getNetwork();
		}

		return null;
	}

	@Override
	public void setNetwork(IEnergyNetwork network)
	{
		for (IConductor conductor : this.interfaces)
		{
			conductor.setNetwork(network);
		}
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
		/**
		 * Try out different sides to try to inject energy into.
		 */
		if (this.partMap(from.ordinal()) == null)
		{
			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
			{
				if (dir != from.getOpposite())
				{
					TMultiPart part = this.partMap(dir.ordinal());

					if (this.interfaces.contains(part))
					{
						return ((IConductor) part).onReceiveEnergy(from, receive, doReceive);
					}
				}
			}
		}

		return 0;
	}

	@Override
	public long onExtractEnergy(ForgeDirection from, long extract, boolean doExtract)
	{
		return 0;
	}

	@Override
	public float getResistance()
	{
		long energyLoss = 0;

		if (this.interfaces.size() > 0)
		{
			for (IConductor conductor : this.interfaces)
			{
				energyLoss += conductor.getResistance();
			}

			energyLoss /= this.interfaces.size();
		}

		return energyLoss;
	}

	@Override
	public long getTransferCapacity()
	{
		long capacitance = 0;

		if (this.interfaces.size() > 0)
		{
			for (IConductor conductor : this.interfaces)
			{
				capacitance += conductor.getTransferCapacity();
			}

			capacitance /= this.interfaces.size();
		}

		return capacitance;
	}

}
