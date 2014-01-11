package resonantinduction.electrical.wire.trait;

import java.util.HashSet;
import java.util.Set;

import net.minecraftforge.common.ForgeDirection;
import resonantinduction.electrical.wire.PartConductor;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
import cofh.api.energy.IEnergyHandler;

public class TraitEnergyHandler extends TileMultipart implements IEnergyHandler
{
	public Set<PartConductor> teConductorInterfaces = new HashSet<PartConductor>();
	public Set<IEnergyHandler> teInterfaces = new HashSet<IEnergyHandler>();

	@Override
	public void copyFrom(TileMultipart that)
	{
		super.copyFrom(that);

		if (that instanceof TraitEnergyHandler)
		{
			this.teConductorInterfaces = ((TraitEnergyHandler) that).teConductorInterfaces;
			this.teInterfaces = ((TraitEnergyHandler) that).teInterfaces;
		}
	}

	@Override
	public void bindPart(TMultiPart part)
	{
		super.bindPart(part);

		if (part instanceof IEnergyHandler)
		{
			if (part instanceof PartConductor)
			{
				this.teConductorInterfaces.add((PartConductor) part);
			}
			else
			{
				this.teInterfaces.add((IEnergyHandler) part);
			}
		}
	}

	@Override
	public void partRemoved(TMultiPart part, int p)
	{
		super.partRemoved(part, p);

		if (part instanceof IEnergyHandler)
		{
			if (part instanceof PartConductor)
			{
				this.teConductorInterfaces.remove(part);
			}
			else
			{
				this.teInterfaces.remove(part);
			}
		}
	}

	@Override
	public void clearParts()
	{
		super.clearParts();
		this.teInterfaces.clear();
		this.teConductorInterfaces.clear();
	}

	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate)
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

					if (this.teConductorInterfaces.contains(part))
					{
						return ((IEnergyHandler) part).receiveEnergy(from, maxReceive, simulate);
					}
				}
			}
		}

		/**
		 * Failed, try pure TE interfaces.
		 */
		for (IEnergyHandler handler : this.teInterfaces)
		{
			return handler.receiveEnergy(from, maxReceive, simulate);
		}

		return 0;
	}

	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate)
	{
		return 0;
	}

	@Override
	public boolean canInterface(ForgeDirection from)
	{
		if (this.partMap(from.ordinal()) == null)
		{
			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
			{
				if (dir != from.getOpposite())
				{
					TMultiPart part = this.partMap(dir.ordinal());

					if (this.teConductorInterfaces.contains(part))
					{
						return ((IEnergyHandler) part).canInterface(from);
					}
				}
			}
		}

		for (IEnergyHandler handler : this.teInterfaces)
		{
			return handler.canInterface(from);
		}

		return false;
	}

	@Override
	public int getEnergyStored(ForgeDirection from)
	{
		if (this.partMap(from.ordinal()) == null)
		{
			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
			{
				if (dir != from.getOpposite())
				{
					TMultiPart part = this.partMap(dir.ordinal());

					if (this.teConductorInterfaces.contains(part))
					{
						return ((IEnergyHandler) part).getEnergyStored(from);
					}
				}
			}
		}

		for (IEnergyHandler handler : this.teInterfaces)
		{
			return handler.getEnergyStored(from);
		}

		return 0;
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from)
	{
		if (this.partMap(from.ordinal()) == null)
		{
			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
			{
				if (dir != from.getOpposite())
				{
					TMultiPart part = this.partMap(dir.ordinal());

					if (this.teConductorInterfaces.contains(part))
					{
						return ((IEnergyHandler) part).getMaxEnergyStored(from);
					}
				}
			}
		}

		for (IEnergyHandler handler : this.teInterfaces)
		{
			return handler.getMaxEnergyStored(from);
		}

		return 0;
	}

}
