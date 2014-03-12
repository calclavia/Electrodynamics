package resonantinduction.core.grid;

import net.minecraftforge.common.ForgeDirection;

/**
 * @author Calclavia
 * 
 * @param <N> - Node type.
 */
public interface INodeProvider
{
	/**
	 * @param nodeType - The type of node we are looking for.
	 * @param from - The direction.
	 * @return Returns the node object.
	 */
	public <N extends INode> N getNode(Class<? super N> nodeType, ForgeDirection from);
}
