package resonantinduction.electrical.multimeter;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class GraphI extends Graph<Integer>
{
	public GraphI(String name, int maxPoints)
	{
		super(name, maxPoints);
	}

	@Override
	public void queue(Integer value)
	{
		queue += value;
	}

	@Override
	public void doneQueue()
	{
		super.doneQueue();
		queue = 0;
	}

	@Override
	public Integer getDefault()
	{
		return 0;
	}

	@Override
	public void load(NBTTagCompound nbt)
	{
		super.load(nbt);
		NBTTagList nbtList = nbt.getTagList("DataPoints");

		for (int i = 0; i < nbtList.tagCount(); ++i)
		{
			NBTTagCompound nbtPoint = (NBTTagCompound) nbtList.tagAt(i);
			points.add(nbtPoint.getInteger("data"));
		}
	}

	@Override
	public NBTTagCompound save()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		NBTTagList data = new NBTTagList();

		for (Integer value : points)
		{
			NBTTagCompound nbtPoint = new NBTTagCompound();
			nbtPoint.setInteger("data", value);
			data.appendTag(nbtPoint);
		}

		nbt.setTag("DataPoints", data);
		return nbt;
	}

	@Override
	public double getDouble()
	{
		return get();
	}

	@Override
	public Integer getAverage()
	{
		if (points.size() > 0)
		{
			int average = 0;

			for (int point : points)
			{
				average += point;
			}

			average /= points.size();

			return average;
		}

		return 0;
	}
}
