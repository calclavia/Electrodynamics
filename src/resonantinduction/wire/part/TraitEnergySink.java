package resonantinduction.wire.part;

import ic2.api.energy.tile.IEnergySink;

import java.util.HashSet;
import java.util.Set;

import universalelectricity.api.CompatibilityType;
import universalelectricity.api.energy.IConductor;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;

public class TraitEnergySink extends TileMultipart implements IEnergySink
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
	public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection from)
	{
		if (this.partMap(from.ordinal()) == null)
		{
			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
			{
				if (dir != from.getOpposite())
				{
					TMultiPart part = this.partMap(dir.ordinal());

					if (this.interfaces.contains(part))
					{
						return ((IConductor) part).canConnect(from);
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

			if (this.interfaces.contains(part))
			{
				return ((IConductor) part).onReceiveEnergy(ForgeDirection.UNKNOWN, Integer.MAX_VALUE, false) * CompatibilityType.INDUSTRIALCRAFT.ratio;
			}
		}

		return demanded;
	}

	@Override
	public double injectEnergyUnits(ForgeDirection from, double amount)
	{
		double consumed = 0;

		if (this.partMap(from.ordinal()) == null)
		{
			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
			{
				if (dir != from.getOpposite())
				{
					TMultiPart part = this.partMap(dir.ordinal());

					if (this.interfaces.contains(part))
					{
						consumed = ((IConductor) part).onReceiveEnergy(from, (long) (amount * CompatibilityType.INDUSTRIALCRAFT.reciprocal_ratio), true) * CompatibilityType.INDUSTRIALCRAFT.ratio;
					}
				}
			}
		}

		return amount - consumed;
	}

	@Override
	public int getMaxSafeInput()
	{
		return Integer.MAX_VALUE;
	}

}
