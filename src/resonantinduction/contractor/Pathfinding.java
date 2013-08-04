/**
 * 
 */
package resonantinduction.contractor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import net.minecraftforge.common.ForgeDirection;
import resonantinduction.base.Vector3;

/**
 * Uses the well known A* Pathfinding algorithm.
 * 
 * @author Calclavia
 * 
 */
public class Pathfinding
{
	public Set<Vector3> openSet, closedSet;

	public HashMap<Vector3, Vector3> navMap;

	public HashMap<Vector3, Double> gScore, fScore;

	public Vector3 target;

	public Set<Vector3> results;

	public Pathfinding(Vector3 target)
	{
		this.target = target;
	}

	public boolean find(Vector3 start)
	{
		this.openSet.add(start);
		this.gScore.put(start, (double) 0);
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

			// Break case here;

			if (currentNode.equals(this.target))
			{
				this.results = this.reconstructPath(this.navMap, this.target);
				return true;
			}

			this.openSet.remove(currentNode);
			this.closedSet.add(currentNode);

			for (int i = 0; i < 6; i++)
			{
				ForgeDirection direction = ForgeDirection.getOrientation(i);
				Vector3 neighbor = currentNode.clone().translate(new Vector3(direction.offsetX, direction.offsetY, direction.offsetZ));
				
			}
		}
	}

	private Set<Vector3> reconstructPath(HashMap<Vector3, Vector3> naviMap, Vector3 currentNode)
	{
		Set<Vector3> path = new HashSet<Vector3>();
		path.add(currentNode);

		if (naviMap.containsKey(currentNode))
		{
			path.addAll(this.reconstructPath(naviMap, currentNode));
		}

		return path;
	}

	private double getEstimate(Vector3 start, Vector3 target2)
	{
		return start.distance(target2);
	}
}
