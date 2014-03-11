package resonantinduction.core.grid;

import java.util.HashMap;

/**
 * A dynamic node loader for registering different nodes for different node interfaces.
 * 
 * @author Calclavia
 * 
 */
public class NodeRegistry
{
	private static final HashMap<Class, Class> INTERFACE_NODE_MAP = new HashMap<Class, Class>();

	public static void register(Class nodeInterface, Class nodeClass)
	{
		INTERFACE_NODE_MAP.put(nodeInterface, nodeClass);
	}

	public static <N> Class<? extends N> get(INodeProvider parent, Class<N> nodeInterface)
	{
		Class nodeClass = INTERFACE_NODE_MAP.get(nodeInterface);

		try
		{
			return (Class<? extends N>) nodeClass.getConstructor(INodeProvider.class).newInstance(parent);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}
}