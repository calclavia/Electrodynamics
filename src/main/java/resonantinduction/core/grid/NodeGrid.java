package resonantinduction.core.grid;

import java.util.AbstractMap;

import net.minecraftforge.common.ForgeDirection;
import resonantinduction.mechanical.energy.network.MechanicalNode;
import universalelectricity.api.net.IUpdate;

public class NodeGrid<N extends INode> extends Grid<N> implements IUpdate
{
	public NodeGrid(Class<? extends N> type)
	{
		super(type);
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
				node.update(1 / 20f);
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

	@Override
	protected void reconstructNode(N node)
	{
		node.setGrid(this);

		AbstractMap<Object, ForgeDirection> connections = node.getConnections();

		for (Object connection : connections.keySet())
		{
			if (isValidNode(connection) && connection instanceof INode)
			{
				INode connectedNode = (INode) connection;

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
}
