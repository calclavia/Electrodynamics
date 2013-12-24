package resonantinduction.wire.part;

import ic2.api.energy.tile.IEnergySink;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;

public class TraitEnergySink extends TileMultipart implements IEnergySink
{
	public Set<IEnergySink> icInterfaces = new HashSet<IEnergySink>();

	@Override
	public void copyFrom(TileMultipart that)
	{
		super.copyFrom(that);

		if (that instanceof TraitEnergySink)
		{
			this.icInterfaces = ((TraitEnergySink) that).icInterfaces;
		}
	}

	@Override
	public void bindPart(TMultiPart part)
	{
		super.bindPart(part);

		if (part instanceof IEnergySink)
		{
			this.icInterfaces.add((IEnergySink) part);
		}
	}

	@Override
	public void partRemoved(TMultiPart part, int p)
	{
		super.partRemoved(part, p);

		if (part instanceof IEnergySink)
		{
			this.icInterfaces.remove(part);
		}
	}

	@Override
	public void clearParts()
	{
		super.clearParts();
		this.icInterfaces.clear();
	}

	@Override
	public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection from)
	{
		if (this.partMap(from.ordinal()) == null)
		{
			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
			{
				if (dir != from.getOpposite())
				{
					TMultiPart part = this.partMap(dir.ordinal());

					if (this.icInterfaces.contains(part))
					{
						return ((IEnergySink) part).acceptsEnergyFrom(emitter, from);
					}
				}
			}
		}

		return false;
	}

	@Override
	public double demandedEnergyUnits()
	{
		double demanded = 0;

		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
		{
			TMultiPart part = this.partMap(dir.ordinal());

			if (this.icInterfaces.contains(part))
			{
				return ((IEnergySink) part).demandedEnergyUnits();
			}
		}

		return demanded;
	}

	@Override
	public double injectEnergyUnits(ForgeDirection from, double amount)
	{
		if (this.partMap(from.ordinal()) == null)
		{
			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
			{
				if (dir != from.getOpposite())
				{
					TMultiPart part = this.partMap(dir.ordinal());

					if (this.icInterfaces.contains(part))
					{
						return ((IEnergySink) part).injectEnergyUnits(from, amount);
					}
				}
			}
		}

		return amount;
	}

	@Override
	public int getMaxSafeInput()
	{
		return Integer.MAX_VALUE;
	}

}
