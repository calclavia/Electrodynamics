package resonantinduction.core.grid;

import net.minecraftforge.common.util.ForgeDirection;
import codechicken.multipart.PartMap;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
import universalelectricity.api.core.grid.INode;
import universalelectricity.api.core.grid.INodeProvider;

public class TraitNodeProvider extends TileMultipart implements INodeProvider
{
	@Override
	public <N extends INode> N getNode(Class<N> nodeType, ForgeDirection from)
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
