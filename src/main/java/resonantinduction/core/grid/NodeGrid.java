package resonantinduction.core.grid;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.minecraftforge.common.ForgeDirection;
import resonantinduction.mechanical.energy.network.MechanicalNode;
import universalelectricity.api.net.IConnectable;
import universalelectricity.api.net.IUpdate;

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
					connectedNode.getGrid().getNodes().clear();
					connectedNode.setGrid(this);
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
