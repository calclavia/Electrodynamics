package resonantinduction.electrical.multimeter;

import java.util.ArrayList;
import java.util.List;

public class Graph
{
	private final int maxPoints;

	/**
	 * Each point represents a tick.
	 */
	private List<Long> points = new ArrayList<Long>();
	private long peak;

	public Graph(int maxPoints)
	{
		this.maxPoints = maxPoints;
	}

	public void add(long y)
	{
		if (y > peak)
		{
			peak = y;
		}

		points.add(0, y);

		if (points.size() > maxPoints)
		{
			if (points.get(maxPoints) == peak)
			{
				peak = 0;
			}

			points.remove(maxPoints);
		}
	}

	public long getPeak()
	{
		return peak;
	}

	public long get(int x)
	{
		return points.get(x);
	}
}
