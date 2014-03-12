package resonantinduction.core.grid;

import java.util.HashSet;
import java.util.Set;

import net.minecraftforge.common.ForgeDirection;
import codechicken.multipart.PartMap;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;

public class TraitNodeProvider extends TileMultipart implements INodeProvider
{
	public Set<INodeProvider> mechanicalInterfaces = new HashSet<INodeProvider>();

	@Override
	public void copyFrom(TileMultipart that)
	{
		super.copyFrom(that);

		if (that instanceof TraitNodeProvider)
		{
			this.mechanicalInterfaces = ((TraitNodeProvider) that).mechanicalInterfaces;
		}
	}

	@Override
	public void bindPart(TMultiPart part)
	{
		super.bindPart(part);

		if (part instanceof INodeProvider)
		{
			this.mechanicalInterfaces.add((INodeProvider) part);
		}
	}

	@Override
	public void partRemoved(TMultiPart part, int p)
	{
		super.partRemoved(part, p);

		if (part instanceof INodeProvider)
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
	public <N extends INode> N getNode(Class<? super N> nodeType, ForgeDirection from)
	{
		TMultiPart part = this.partMap(from.ordinal());

		if (part == null)
		{
			part = partMap(PartMap.CENTER.ordinal());
		}

		if (part instanceof INodeProvider)
		{
			return ((INodeProvider) part).getNode(nodeType, from);
		}

		return null;
	}
}
