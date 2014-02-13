package resonantinduction.mechanical.fluid.transport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.api.vector.Vector3;
import calclavia.lib.utility.FluidUtility;

/**
 * A simpler path Finder used to find drainable or fillable tiles
 * 
 * @author DarkGuardsman
 */
public class LiquidPathFinder
{
	/** Curent world this pathfinder will operate in */
	private World world;
	/** List of all nodes traveled by the path finder */
	public Set<Vector3> nodeList = new HashSet<Vector3>();
	/** List of all nodes that match the search parms */
	public Set<Vector3> results = new HashSet<Vector3>();
	public final List<Vector3> sortedResults = new ArrayList<Vector3>();
	/** Are we looking for liquid fillable blocks */
	private boolean fill = false;
	/** priority search direction either up or down only */
	private ForgeDirection priority;
	/** Limit on the searched nodes per run */
	private int resultLimit = 200;
	private int resultsFound = 0;
	private int resultRun = resultLimit;
	private int runs = 0;
	/** Start location of the pathfinder used for range calculations */
	private Vector3 start;
	/** Range to limit the search to */
	private double range;
	/** List of forgeDirection to use that are shuffled to prevent strait lines */
	List<ForgeDirection> shuffledDirections = new ArrayList<ForgeDirection>();

	public LiquidPathFinder(final World world, final int resultLimit, final double range)
	{
		this.range = range;
		this.world = world;
		if (fill)
		{
			priority = ForgeDirection.DOWN;
		}
		else
		{
			priority = ForgeDirection.UP;
		}
		this.resultLimit = resultLimit;
		this.reset();
		shuffledDirections.add(ForgeDirection.EAST);
		shuffledDirections.add(ForgeDirection.WEST);
		shuffledDirections.add(ForgeDirection.NORTH);
		shuffledDirections.add(ForgeDirection.SOUTH);
	}

	public void addNode(Vector3 vec)
	{
		if (!this.nodeList.contains(vec))
		{
			this.nodeList.add(vec);
		}
	}

	public void addResult(Vector3 vec)
	{
		if (!this.results.contains(vec))
		{
			this.resultsFound++;
			this.results.add(vec);
		}
	}

	/**
	 * Searches for nodes attached to the given node
	 * 
	 * @return True on success finding, false on failure.
	 */
	public boolean findNodes(Vector3 node)
	{
		if (node == null)
		{
			return false;
		}
		this.addNode(node);

		if (this.isValidResult(node))
		{
			this.addResult(node);
		}

		if (this.isDone(node.clone()))
		{
			return false;
		}

		if (find(this.priority, node.clone()))
		{
			return true;
		}

		Collections.shuffle(shuffledDirections);
		Collections.shuffle(shuffledDirections);

		for (ForgeDirection direction : shuffledDirections)
		{
			if (find(direction, node.clone()))
			{
				return true;
			}
		}

		if (find(this.priority.getOpposite(), node.clone()))
		{
			return true;
		}

		return false;
	}

	/**
	 * Find all node attached to the origin mode in the given direction
	 * 
	 * Note: Calls findNode if the next code is valid
	 */
	public boolean find(ForgeDirection direction, Vector3 origin)
	{
		this.runs++;
		Vector3 vec = origin.clone().translate(direction);
		double distance = vec.toVector2().distance(this.start.toVector2());

		if (distance <= this.range && isValidNode(vec))
		{
			if (this.fill && FluidUtility.drainBlock(world, vec, false) != null || FluidUtility.isFillableFluid(world, vec))
			{
				for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
				{
					Vector3 veb = vec.clone().translate(dir);
					if (FluidUtility.isFillableBlock(world, veb))
					{
						this.addNode(veb);
						if (this.isValidResult(veb))
						{
							this.addResult(veb);
						}
					}
				}
			}

			if (this.findNodes(vec))
			{
				return true;
			}
		}
		return false;
	}

	/** Checks to see if this node is valid to path find threw */
	public boolean isValidNode(Vector3 pos)
	{
		if (pos == null)
		{
			return false;
		}
		/* Check if the chunk is loaded to prevent action outside of the loaded area */
		Chunk chunk = this.world.getChunkFromBlockCoords(pos.intX(), pos.intZ());
		if (chunk == null || !chunk.isChunkLoaded)
		{
			return false;
		}

		return FluidUtility.drainBlock(world, pos, false) != null || FluidUtility.isFillableFluid(world, pos);
	}

	public boolean isValidResult(Vector3 node)
	{
		if (this.fill)
		{
			return FluidUtility.isFillableBlock(world, node) || FluidUtility.isFillableFluid(world, node);
		}
		else
		{
			return FluidUtility.drainBlock(world, node, false) != null;
		}
	}

	/** Checks to see if we are done pathfinding */
	public boolean isDone(Vector3 vec)
	{
		if (this.runs > 1000)
		{
			return true;
		}
		if (this.resultsFound >= this.resultRun)
			if (this.results.size() >= this.resultLimit || this.nodeList.size() >= 10000)
			{
				return true;
			}
		return false;
	}

	/** Called to execute the pathfinding operation. */
	public LiquidPathFinder start(final Vector3 startNode, int runCount, final boolean fill)
	{
		this.start = startNode;
		this.fill = fill;
		this.runs = 0;
		this.resultsFound = 0;
		this.resultRun = runCount;
		this.find(ForgeDirection.UNKNOWN, startNode);

		this.refresh();
		this.sortBlockList(start, results, fill);
		return this;
	}

	public LiquidPathFinder reset()
	{
		this.nodeList.clear();
		this.results.clear();
		return this;
	}

	public LiquidPathFinder refresh()
	{
		Iterator<Vector3> it = this.nodeList.iterator();
		while (it.hasNext())
		{
			Vector3 vec = it.next();
			if (!this.isValidNode(vec))
			{
				it.remove();
			}
			if (this.isValidResult(vec))
			{
				this.addResult(vec);
			}
		}
		it = this.results.iterator();
		while (it.hasNext())
		{
			Vector3 vec = it.next();
			if (!this.isValidResult(vec))
			{
				it.remove();
			}
			if (this.isValidNode(vec))
			{
				this.addNode(vec);
			}
		}
		return this;
	}

	/**
	 * Used to sort a list of vector3 locations using the vector3's distance from one point and
	 * elevation in the y axis
	 * 
	 * Sorting is ordered from the highest-furthest block and consequently will have its adjacent
	 * blocks as the "next blocks" to make sure the fluid can easily remove infinite fluids like
	 * water.
	 * 
	 * @param start - start location to measure distance from
	 * @param results2 - list of vectors to sort
	 * @param prioritizeHighest - sort highest y value to the top.
	 * 
	 * Note: Height takes priority over distance.
	 */
	public void sortBlockList(final Vector3 start, final Set<Vector3> set, final boolean prioritizeHighest)
	{
		try
		{
			sortedResults.clear();
			sortedResults.addAll(set);

			// The highest and furthest fluid block for drain. Closest fluid block for fill.
			Vector3 bestFluid = null;

			if (fill)
			{
				bestFluid = start;
			}
			else
			{
				for (Vector3 checkPos : set)
				{
					if (bestFluid == null)
					{
						bestFluid = checkPos;
					}

					/**
					 * We're draining, so we want the highest furthest block first.
					 */
					if (prioritizeHighest)
					{
						if (checkPos.y > bestFluid.y)
						{
							bestFluid = checkPos;
						}
						else if (checkPos.y == bestFluid.y && start.distance(checkPos) > start.distance(bestFluid))
						{
							bestFluid = checkPos;
						}
					}
					else if (start.distance(checkPos) > start.distance(bestFluid))
					{
						bestFluid = checkPos;
					}
				}
			}

			final Vector3 optimalFluid = bestFluid;
			Collections.sort(sortedResults, new Comparator<Vector3>()
			{
				@Override
				public int compare(Vector3 vecA, Vector3 vecB)
				{
					if (vecA.equals(vecB))
						return 0;

					return vecA.distance(optimalFluid) < vecB.distance(optimalFluid) ? -1 : 1;
				}
			});
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public LiquidPathFinder setWorld(World world2)
	{
		if (world2 != this.world)
		{
			this.reset();
			this.world = world2;
		}
		return this;
	}
}
