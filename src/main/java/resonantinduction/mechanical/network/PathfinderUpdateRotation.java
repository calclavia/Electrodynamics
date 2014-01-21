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
public class PathfinderUpdateRotation extends ConnectionPathfinder<IMechanical>
{
	private boolean currentRotationFlag = true;
	private Set<IMechanical> prevClosedSet;
	private IMechanicalNetwork network;

	public PathfinderUpdateRotation(IMechanical first, IMechanicalNetwork network, Set<IMechanical> prevClosedSet)
	{
		super(IMechanical.class, first);
		this.currentRotationFlag = first.isClockwise();
		this.prevClosedSet = prevClosedSet;
		this.network = network;
	}

	public boolean findNodes(IMechanical currentNode)
	{
		this.closedSet.add(currentNode);

		currentNode.setClockwise(currentRotationFlag);

		for (IMechanical node : this.getConnectedNodes(currentNode))
		{
			if (!this.closedSet.contains(node))
			{
				currentRotationFlag = (node.isRotationInversed() && currentNode.isRotationInversed()) ? !currentNode.isClockwise() : currentNode.isClockwise();

				if ((prevClosedSet != null && prevClosedSet.contains(node)) && (node.isClockwise() != currentRotationFlag))
				{
					// We have conflicting rotations. Network is now stuck.
					network.setPower(0, 0);
				}

				findNodes(node);
			}
		}

		return false;
	}
}
