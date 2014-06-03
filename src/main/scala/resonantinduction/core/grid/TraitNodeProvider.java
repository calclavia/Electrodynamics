package resonantinduction.core.grid;

import net.minecraftforge.common.ForgeDirection;
import resonant.api.grid.INode;
import resonant.api.grid.INodeProvider;
import codechicken.multipart.PartMap;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;

//TODO: Move to UE 3.2
public class TraitNodeProvider extends TileMultipart implements INodeProvider
{
	@Override
	public INode getNode(Class<? extends INode> nodeType, ForgeDirection from)
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
