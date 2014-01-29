package resonantinduction.electrical.multimeter;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

/**
 * Graph for the multimeter
 * 
 * @author Calclavia
 * 
 */
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
		return points.size() > x ? points.get(x) : 0;
	}

	public void load(NBTTagCompound nbt)
	{
		NBTTagList nbtList = nbt.getTagList("DataPoints");

		for (int i = 0; i < nbtList.tagCount(); ++i)
		{
			NBTTagCompound nbtPoint = (NBTTagCompound) nbtList.tagAt(i);
			add(nbtPoint.getLong("data"));
		}
	}

	public NBTTagCompound save()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		NBTTagList data = new NBTTagList();

		for (long value : points)
		{
			NBTTagCompound nbtPoint = new NBTTagCompound();
			nbtPoint.setLong("data", value);

			data.appendTag(nbtPoint);
		}

		nbt.setTag("DataPoints", data);
		return nbt;
	}
}
