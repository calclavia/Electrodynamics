package resonantinduction.electrical.multimeter;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class GraphD extends Graph<Double>
{
	public GraphD(String name, int maxPoints)
	{
		super(name, maxPoints);
	}

	@Override
	public void queue(Double value)
	{
		queue += value;
	}

	@Override
	public void doneQueue()
	{
		super.doneQueue();
		queue = 0d;
	}

	@Override
	public Double getDefault()
	{
		return 0d;
	}

	@Override
	public void load(NBTTagCompound nbt)
	{
		super.load(nbt);

		NBTTagList nbtList = nbt.getTagList("DataPoints");

		for (int i = 0; i < nbtList.tagCount(); ++i)
		{
			NBTTagCompound nbtPoint = (NBTTagCompound) nbtList.tagAt(i);
			points.add(nbtPoint.getDouble("data"));
		}
	}

	@Override
	public NBTTagCompound save()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		NBTTagList data = new NBTTagList();

		for (Double value : points)
		{
			NBTTagCompound nbtPoint = new NBTTagCompound();
			nbtPoint.setDouble("data", value);
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
	public Double getAverage()
	{
		if (points.size() > 0)
		{
			double average = 0;

			for (double point : points)
			{
				average += point;
			}

			average /= points.size();

			return average;
		}

		return 0d;
	}
}
