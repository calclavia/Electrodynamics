/**
 * 
 */
package resonantinduction.contractor;

import java.util.HashMap;
import java.util.Set;

import resonantinduction.base.Vector3;

/**
 * Uses the well known A* Pathfinding algorithm.
 * 
 * @author Calclavia
 * 
 */
public class Pathfinding
{
	public Set<Vector3> openSet;

	public HashMap<Vector3, Vector3> navMap;

	public HashMap<Vector3, Double> gScore, fScore;

	public Vector3 target;

	public Pathfinding(Vector3 target)
	{
		this.target = target;
	}

	public void findNodes(Vector3 start)
	{
		this.openSet.add(start);
		this.gScore.put(start, 0);
		this.fScore.put(start, this.gScore.get(start) + getEstimate(start, this.target));

		while (!this.openSet.isEmpty())
		{
			Vector3 currentNode = null;
			double lowestFScore = 0;

			for (Vector3 node : this.openSet)
			{
				if (currentNode == null || this.fScore.get(node) < lowestFScore)
				{
					currentNode = node;
					lowestFScore = this.fScore.get(node);
				}
			}

			if (currentNode == null)
			{
				break;
			}
		}
	}

	private double getEstimate(Vector3 start, Vector3 target2)
	{
		return null;
	}
}
