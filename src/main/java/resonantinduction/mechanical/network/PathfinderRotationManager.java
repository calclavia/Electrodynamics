package resonantinduction.mechanical.network;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import universalelectricity.api.net.IConnector;
import universalelectricity.core.net.ConnectionPathfinder;

/**
 * Sets proper rotations on all connected units in the mechanical network.
 * 
 * The pathfinder will find the first point and rotate all adjacent blocks next to it to be the
 * opposite of the original.
 * 
 * @author Calclavia
 * 
 */
public class PathfinderRotationManager extends ConnectionPathfinder<IMechanical>
{
	private boolean currentIsClockwise = true;
	private Set<IMechanical> prevClosedSet;

	public PathfinderRotationManager(IMechanical first, Set<IMechanical> prevClosedSet)
	{
		super(first);
		this.currentIsClockwise = first.isClockwise();
		this.prevClosedSet = prevClosedSet;
	}

	public boolean findNodes(IMechanical currentNode)
	{
		this.closedSet.add(currentNode);

		currentNode.setClockwise(currentIsClockwise);
		currentIsClockwise = !currentNode.isClockwise();

		for (IMechanical node : this.getConnectedNodes(currentNode))
		{
			if (!this.closedSet.contains(node))
			{
				if (prevClosedSet.contains(node) && node.isClockwise() != currentNode.isClockwise())
				{
					// We have conflicting gears. Network is now equal.
					currentNode.getNetwork().setPower(0, 0);
				}

				findNodes(node);
				currentIsClockwise = node.isRotationInversed() ? !currentNode.isClockwise() : currentNode.isClockwise();
			}
		}

		return false;
	}
}
