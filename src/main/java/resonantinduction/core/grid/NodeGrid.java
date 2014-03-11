package resonantinduction.core.grid;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Iterator;

import net.minecraftforge.common.ForgeDirection;

public abstract class NodeGrid<N extends Node> extends Grid<N>
{
	public NodeGrid(Class<? extends N> type)
	{
		super(type);
	}

	@Override
	protected void reconstructNode(N node)
	{
		node.recache();
		node.setGrid(this);

		AbstractMap<Object, ForgeDirection> connections = node.getConnections();

		for (Object connection : connections.keySet())
		{
			if (isValidNode(connection) && connection instanceof Node)
			{
				Node connectedNode = (Node) connection;

				if (connectedNode.getGrid() != this)
				{
					synchronized (connectedNode.getGrid().getNodes())
					{
						connectedNode.getGrid().getNodes().clear();
					}

					add((N) connectedNode);
					reconstructNode((N) connectedNode);
				}
			}
		}
	}

	@Override
	public void deconstruct()
	{
		synchronized (nodes)
		{
			Iterator<N> it = new HashSet<N>(nodes).iterator();

			while (it.hasNext())
			{
				N node = it.next();
				node.setGrid(null);
				node.reconstruct();
			}

			nodes.clear();
		}
	}
}
