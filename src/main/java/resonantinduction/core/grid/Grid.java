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
public abstract class Grid<N extends INode> implements IGrid<N>, IUpdate
{
	/**
	 * A set of connectors (e.g conductors).
	 */
	private final Set<N> nodes = Collections.newSetFromMap(new WeakHashMap<N, Boolean>());

	private final Class<? extends N> nodeType;

	public Grid(Class<? extends N> type)
	{
		nodeType = type;
	}

	public abstract N newInstance();

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
	 * An grid update called only server side.
	 */
	@Override
	public void update()
	{
		synchronized (nodes)
		{
			for (INode node : nodes)
			{
				node.update();
			}
		}
	}

	@Override
	public boolean canUpdate()
	{
		return nodes.size() > 0;
	}

	@Override
	public boolean continueUpdate()
	{
		return canUpdate();
	}

	/**
	 * A simple reconstruct class to rebuild the grid.
	 */
	@Override
	public void reconstruct()
	{
		Iterator<N> it = new HashSet<N>(getNodes()).iterator();

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
