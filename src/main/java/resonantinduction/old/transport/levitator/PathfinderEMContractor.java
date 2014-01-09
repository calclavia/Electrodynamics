/**
 * 
 */
package resonantinduction.old.transport.levitator;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.path.IPathCallBack;
import calclavia.lib.path.Pathfinder;
import calclavia.lib.path.PathfinderAStar;

/**
 * Uses the well known A* Pathfinding algorithm.
 * 
 * @author Calclavia
 * 
 */
public class PathfinderEMContractor extends PathfinderAStar
{
	private World world;

	private double maxSearchDistance;

	public PathfinderEMContractor(final World world, final Vector3 goal)
	{
		super(new IPathCallBack()
		{
			@Override
			public Set<Vector3> getConnectedNodes(Pathfinder finder, Vector3 currentNode)
			{
				Set<Vector3> neighbors = new HashSet<Vector3>();

				for (int i = 0; i < 6; i++)
				{
					Vector3 neighbor = currentNode.clone().modifyPositionFromSide(ForgeDirection.getOrientation(i));

					if (TileEMLevitator.canBePath(world, neighbor))
					{
						neighbors.add(neighbor);
					}
				}

				return neighbors;
			}

			@Override
			public boolean onSearch(Pathfinder finder, Vector3 start, Vector3 node)
			{
				return !(start.distance(node) < (start.distance(goal) * 2));
			}
		}, goal);

		this.world = world;
	}

	@Override
	public boolean findNodes(Vector3 start)
	{
		int blockCount = 0;

		for (int i = 0; i < 6; i++)
		{
			ForgeDirection direction = ForgeDirection.getOrientation(i);
			Vector3 neighbor = this.goal.clone().translate(new Vector3(direction.offsetX, direction.offsetY, direction.offsetZ));

			if (!TileEMLevitator.canBePath(this.world, neighbor))
			{
				blockCount++;
			}
		}

		if (blockCount >= 6)
		{
			return false;
		}

		return super.findNodes(start);
	}
}
