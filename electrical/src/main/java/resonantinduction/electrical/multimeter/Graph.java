package resonantinduction.electrical.multimeter;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;

/**
 * Graph for the multimeter
 * 
 * @author Calclavia
 * 
 */
public abstract class Graph<V extends Comparable<V>>
{
	public final String name;

	private final int maxPoints;

	/**
	 * Each point represents a tick.
	 */
	protected List<V> points = new ArrayList<V>();
	private V peak = getDefault();

	/**
	 * Queue for the next update to insert into the graph.
	 */
	protected V queue = getDefault();

	public Graph(String name, int maxPoints)
	{
		this.name = name;
		this.maxPoints = maxPoints;
	}

	public void add(V y)
	{
		if (y.compareTo(peak) > 0)
		{
			peak = y;
		}

		points.add(0, y);

		if (points.size() > maxPoints)
		{
			if (points.get(maxPoints) == peak)
			{
				peak = getDefault();
			}

			points.remove(maxPoints);
		}
	}

	public V getPeak()
	{
		return peak;
	}

	public V get(int x)
	{
		return points.size() > x ? points.get(x) : getDefault();
	}

	public V get()
	{
		return get(0);
	}

	public abstract void queue(V value);

	public void doneQueue()
	{
		add(queue);
	}

	protected abstract V getDefault();

	public void load(NBTTagCompound nbt)
	{
		points.clear();
	}

	public abstract NBTTagCompound save();

	public abstract double getDouble();

	public abstract V getAverage();
}
