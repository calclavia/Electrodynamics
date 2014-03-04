package resonantinduction.core.grid;

import net.minecraftforge.common.ForgeDirection;

public interface INodeProvider<N extends INode>
{
	public N getNode(ForgeDirection from);
}
