package resonantinduction.electrical.wire.trait;

import java.util.HashSet;
import java.util.Set;

import net.minecraftforge.common.ForgeDirection;
import universalelectricity.api.energy.IConductor;
import universalelectricity.api.energy.IEnergyNetwork;
import universalelectricity.api.net.IConnector;
import codechicken.multipart.PartMap;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;

public class TraitConductor extends TileMultipart implements IConductor
{
	public Set<IConductor> ueInterfaces = new HashSet<IConductor>();

	@Override
	public void copyFrom(TileMultipart that)
	{
		super.copyFrom(that);

		if (that instanceof TraitConductor)
		{
			this.ueInterfaces = ((TraitConductor) that).ueInterfaces;
		}
	}

	@Override
	public void bindPart(TMultiPart part)
	{
		super.bindPart(part);

		if (part instanceof IConductor)
		{
			this.ueInterfaces.add((IConductor) part);
		}
	}

	@Override
	public void partRemoved(TMultiPart part, int p)
	{
		super.partRemoved(part, p);

		if (part instanceof IConductor)
		{
			this.ueInterfaces.remove(part);
		}
	}

	@Override
	public void clearParts()
	{
		super.clearParts();
		this.ueInterfaces.clear();
	}

	@Override
	public Object[] getConnections()
	{
		for (IConductor conductor : this.ueInterfaces)
		{
			return conductor.getConnections();
		}

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
		for (IConductor conductor : this.ueInterfaces)
		{
			conductor.setNetwork(network);
		}
	}

	@Override
	public boolean canConnect(ForgeDirection direction, Object source)
	{
		for (IConductor conductor : this.ueInterfaces)
		{
			if (conductor.canConnect(direction.getOpposite(), source))
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
		if (partMap(from.ordinal()) instanceof IConductor)
		{
			return ((IConductor) partMap(from.ordinal())).onReceiveEnergy(from, receive, doReceive);
		}

		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
		{
			if (dir != from.getOpposite())
			{
				TMultiPart part = this.partMap(dir.ordinal());

				if (this.ueInterfaces.contains(part))
				{
					return ((IConductor) part).onReceiveEnergy(from, receive, doReceive);
				}
			}
		}

		if (partMap(PartMap.CENTER.ordinal()) instanceof IConductor)
		{
			return ((IConductor) partMap(PartMap.CENTER.ordinal())).onReceiveEnergy(from, receive, doReceive);
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

		if (this.ueInterfaces.size() > 0)
		{
			for (IConductor conductor : this.ueInterfaces)
			{
				energyLoss += conductor.getResistance();
			}

			energyLoss /= this.ueInterfaces.size();
		}

		return energyLoss;
	}

	@Override
	public long getCurrentCapacity()
	{
		long capacitance = 0;

		if (this.ueInterfaces.size() > 0)
		{
			for (IConductor conductor : this.ueInterfaces)
			{
				capacitance += conductor.getCurrentCapacity();
			}

			capacitance /= this.ueInterfaces.size();
		}

		return capacitance;
	}

	@Override
	public IConductor getInstance(ForgeDirection from)
	{
		/**
		 * Try out different sides to try to inject energy into.
		 */
		if (partMap(from.ordinal()) instanceof IConductor)
		{
			return (IConductor) partMap(from.ordinal());
		}

		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
		{
			TMultiPart part = this.partMap(dir.ordinal());

			if (this.ueInterfaces.contains(part))
			{
				return (IConductor) ((IConductor) part).getInstance(from);
			}
		}

		if (partMap(PartMap.CENTER.ordinal()) instanceof IConductor)
		{
			return (IConductor) (IConnector<IEnergyNetwork>) partMap(PartMap.CENTER.ordinal());
		}

		return null;
	}
}
