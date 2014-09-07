package resonantinduction.core.prefab;

import codechicken.multipart.PartMap;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
import net.minecraftforge.common.util.ForgeDirection;
import universalelectricity.api.core.grid.INode;
import universalelectricity.api.core.grid.INodeProvider;

/**
 * Created by robert on 8/13/2014.
 */
public class TraitNodeProvider extends TileMultipart implements INodeProvider
{
	@Override
	public INode getNode(Class<? extends INode> nodeType, ForgeDirection from)
	{
		TMultiPart nodePart = partMap(from.ordinal());

		if (nodePart == null)
		{
			nodePart = partMap(PartMap.CENTER.ordinal());
		}
		if (nodePart instanceof INodeProvider)
		{
			return ((INodeProvider) nodePart).getNode(nodeType, from);
		}
		return null;
	}
}
