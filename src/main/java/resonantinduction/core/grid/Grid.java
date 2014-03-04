package resonantinduction.core.grid;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;

import universalelectricity.api.net.IUpdate;
import universalelectricity.core.net.ConnectionPathfinder;

/**
 * A grid specifying a connection with a series of nodes.
 * 
 * @author Calclavia
 * 
 * @param <N> - The node type.
 */
public abstract class Grid<N> implements IGrid<N>
{
	/**
	 * A set of connectors (e.g conductors).
	 */
	protected final Set<N> nodes = Collections.newSetFromMap(new WeakHashMap<N, Boolean>());
	private final Class<? extends N> nodeType;

	public Grid(Class<? extends N> type)
	{
		nodeType = type;
	}

	@Override
	public void add(N node)
	{
		synchronized (nodes)
		{
			nodes.add(node);
		}
	}

	@Override
	public void remove(N node)
	{
		synchronized (nodes)
		{
			nodes.remove(node);
		}
	}

	@Override
	public Set<N> getNodes()
	{
		return nodes;
	}

	/**
	 * A simple reconstruct class to rebuild the grid.
	 */
	@Override
	public void reconstruct()
	{
		synchronized (nodes)
		{
			Iterator<N> it = nodes.iterator();

			while (it.hasNext())
			{
				N node = it.next();

				if (isValidNode(node))
				{
					reconstructNode(node);
				}
				else
				{
					it.remove();
				}
			}
		}
	}

	public boolean isValidNode(Object node)
	{
		return nodeType.isAssignableFrom(node.getClass());
	}

	/**
	 * Gets the first connector in the set.
	 * 
	 * @return
	 */
	public N getFirstNode()
	{
		for (N node : getNodes())
		{
			return node;
		}

		return null;
	}

	protected void reconstructNode(N node)
	{

	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[" + hashCode() + ", Connectors: " + nodes.size() + "]";
	}
}
