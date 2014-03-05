package resonantinduction.core.grid;

import net.minecraftforge.common.ForgeDirection;

public interface INodeProvider<N extends Node>
{
	public N getNode(ForgeDirection from);
}
