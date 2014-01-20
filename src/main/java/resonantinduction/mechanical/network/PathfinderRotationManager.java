package resonantinduction.mechanical.network;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import universalelectricity.api.net.IConnector;

/**
 * Sets proper rotations on all connected units in the mechanical network.
 * 
 * The pathfinder will find the first point and rotate all adjacent blocks next to it to be the
 * opposite of the original.
 * 
 * @author Calclavia
 * 
 */
public class PathfinderRotationManager
{
	/** A list of nodes that the pathfinder already went through. */
	public final Set<IConnector> closedSet = new LinkedHashSet<IConnector>();

	/** The resulted path found by the pathfinder. Could be null if no path was found. */
	public final Set<IConnector> results = new LinkedHashSet<IConnector>();

	private final IConnector targetConnector;
	private final List<IConnector> ignoreConnector;

	public PathfinderRotationManager(IConnector targetConnector, IConnector... ignoreConnector)
	{
		this.targetConnector = targetConnector;
		if (ignoreConnector != null)
		{
			this.ignoreConnector = Arrays.asList(ignoreConnector);
		}
		else
		{
			this.ignoreConnector = new ArrayList<IConnector>();
		}
	}

	/**
	 * A recursive function to find all connectors.
	 * 
	 * @return True on success finding, false on failure.
	 */
	public boolean findNodes(IConnector currentNode)
	{
		this.closedSet.add(currentNode);

		if (this.onSearch(currentNode))
		{
			return false;
		}

		for (IConnector node : this.getConnectedNodes(currentNode))
		{
			if (!this.closedSet.contains(node))
			{
				if (this.findNodes(node))
				{
					return true;
				}
			}
		}

		return false;
	}

	public Set<IConnector> getConnectedNodes(IConnector currentNode)
	{
		Set<IConnector> connectedNodes = new HashSet<IConnector>();

		if (currentNode != null)
		{
			for (int i = 0; i < currentNode.getConnections().length; i++)
			{
				Object obj = currentNode.getConnections()[i];

				if (obj instanceof IConnector && !this.ignoreConnector.contains(obj))
				{
					connectedNodes.add((IConnector) obj);
				}
			}
		}

		return connectedNodes;
	}

	public boolean onSearch(IConnector node)
	{
		if (node == this.targetConnector)
		{
			this.results.add(node);
			return true;
		}

		return false;
	}

	public void reset()
	{
		this.results.clear();
		this.closedSet.clear();
	}
}
