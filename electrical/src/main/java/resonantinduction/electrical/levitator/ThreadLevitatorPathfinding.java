/**
 * 
 */
package resonantinduction.electrical.levitator;

import universalelectricity.api.vector.Vector3;

/**
 * @author Calclavia
 * 
 */
public class ThreadLevitatorPathfinding extends Thread
{
	private boolean isCompleted = false;
	private PathfinderLevitator pathfinder;
	private Vector3 start;

	public ThreadLevitatorPathfinding(PathfinderLevitator pathfinder, Vector3 start)
	{
		this.pathfinder = pathfinder;
		this.start = start;
		this.setPriority(Thread.MIN_PRIORITY);
	}

	@Override
	public void run()
	{
		this.pathfinder.findNodes(this.start);
		this.isCompleted = true;
	}

	public PathfinderLevitator getPath()
	{
		if (this.isCompleted)
		{
			return this.pathfinder;
		}

		return null;
	}
}
