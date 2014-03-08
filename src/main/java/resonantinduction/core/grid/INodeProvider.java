package resonantinduction.core.grid;

import net.minecraftforge.common.ForgeDirection;

/**
 * Any inheritance of INodeProvider should have a method "getNode()"
 * 
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
	public Object getNode(Class<? extends Node> nodeType, ForgeDirection from);
}
