package resonantinduction.electrical.multimeter;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class GraphF extends Graph<Float>
{
	public GraphF(String name, int maxPoints)
	{
		super(name, maxPoints);
	}

	@Override
	public void queue(Float value)
	{
		queue += value;
	}

	@Override
	public void doneQueue()
	{
		super.doneQueue();
		queue = 0f;
	}

	@Override
	public Float getDefault()
	{
		return 0f;
	}

	@Override
	public void load(NBTTagCompound nbt)
	{
		super.load(nbt);

		NBTTagList nbtList = nbt.getTagList("DataPoints");

		for (int i = 0; i < nbtList.tagCount(); ++i)
		{
			NBTTagCompound nbtPoint = (NBTTagCompound) nbtList.tagAt(i);
			points.add(nbtPoint.getFloat("data"));
		}
	}

	@Override
	public NBTTagCompound save()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		NBTTagList data = new NBTTagList();

		for (Float value : points)
		{
			NBTTagCompound nbtPoint = new NBTTagCompound();
			nbtPoint.setFloat("data", value);
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
	public Float getAverage()
	{
		if (points.size() > 0)
		{
			float average = 0;

			for (float point : points)
			{
				average += point;
			}

			average /= points.size();

			return average;
		}

		return 0f;
	}
}
