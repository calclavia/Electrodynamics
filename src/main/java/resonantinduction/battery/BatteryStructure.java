package resonantinduction.battery;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.item.ItemStack;
import universalelectricity.api.energy.EnergyStorageHandler;
import universalelectricity.api.vector.Vector3;

public class BatteryStructure extends EnergyStorageHandler
{
	public BatteryStructure(TileBattery battery)
	{
		super(TileBattery.STORAGE);
		this.battery.add(battery);
	}

	public Set<TileBattery> battery = new LinkedHashSet<TileBattery>();

	public int length;

	public int width;

	public int height;

	public ItemStack tempStack;

	public boolean isMultiblock;

	public boolean didTick;

	public boolean wroteNBT;

	public int getVolume()
	{
		return this.battery.size();
	}

	/*
	 * @Override
	 * public int hashCode()
	 * {
	 * int code = 1;
	 * code = 31 * locations.hashCode();
	 * code = 31 * length;
	 * code = 31 * width;
	 * code = 31 * height;
	 * return code;
	 * }
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof BatteryStructure))
		{
			return false;
		}

		BatteryStructure data = (BatteryStructure) obj;

		if (!data.battery.equals(battery))
		{
			return false;
		}

		if (data.length != length || data.width != width || data.height != height)
		{
			return false;
		}

		return true;
	}

	public void merge(TileBattery tile)
	{
		// Merge structure.
		long energyToMerge = ((TileBattery) tile).structure.getEnergy();
		long capacityToMerge = ((TileBattery) tile).structure.getEnergyCapacity();
		this.battery.addAll(((TileBattery) tile).structure.battery);
		((TileBattery) tile).structure.battery.clear();
		this.resetReferences();
		this.setCapacity(capacityToMerge);
		this.receiveEnergy(energyToMerge, true);
	}

	public void resetReferences()
	{
		Iterator<TileBattery> it = this.battery.iterator();

		while (it.hasNext())
		{
			TileBattery tile = it.next();
			tile.structure = this;
		}
	}
}
