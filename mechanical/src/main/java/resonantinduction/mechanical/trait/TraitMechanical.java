package resonantinduction.mechanical.trait;

import java.util.HashSet;
import java.util.Set;

import net.minecraftforge.common.ForgeDirection;
import resonantinduction.mechanical.energy.network.IMechanicalNodeProvider;
import resonantinduction.mechanical.energy.network.MechanicalNode;
import codechicken.multipart.PartMap;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;

public class TraitMechanical extends TileMultipart implements IMechanicalNodeProvider
{
	public Set<IMechanicalNodeProvider> mechanicalInterfaces = new HashSet<IMechanicalNodeProvider>();

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

		if (part instanceof IMechanicalNodeProvider)
		{
			this.mechanicalInterfaces.add((IMechanicalNodeProvider) part);
		}
	}

	@Override
	public void partRemoved(TMultiPart part, int p)
	{
		super.partRemoved(part, p);

		if (part instanceof IMechanicalNodeProvider)
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
	public MechanicalNode getNode(ForgeDirection from)
	{
		TMultiPart part = this.partMap(from.ordinal());

		if (part == null)
		{
			part = partMap(PartMap.CENTER.ordinal());
		}

		if (part instanceof IMechanicalNodeProvider)
		{
			return ((IMechanicalNodeProvider) part).getNode(from);
		}

		return null;

	}
}
