package resonantinduction.battery;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.item.ItemStack;
import resonantinduction.base.Vector3;

public class SynchronizedBatteryData 
{
	public Set<Vector3> locations = new HashSet<Vector3>();
	
	public Set<ItemStack> inventory = new HashSet<ItemStack>();
	
	public int length;
	
	public int width;
	
	public int height;
	
	public boolean isMultiblock;
	
	public boolean didTick;
	
	public int getVolume()
	{
		return length*width*height;
	}
	
	public int getMaxCells()
	{
		return getVolume()*BatteryManager.CELLS_PER_BATTERY;
	}
	
	public static SynchronizedBatteryData getBase(TileEntityBattery tileEntity, Set<ItemStack> inventory)
	{
		SynchronizedBatteryData structure = getBase(tileEntity);
		structure.inventory = inventory;
		
		return structure;
	}
	
	public static SynchronizedBatteryData getBase(TileEntityBattery tileEntity)
	{
		SynchronizedBatteryData structure = new SynchronizedBatteryData();
		structure.length = 1;
		structure.width = 1;
		structure.height = 1;
		structure.locations.add(new Vector3(tileEntity));
		
		return structure;
	}
	
	@Override
	public int hashCode()
	{
		int code = 1;
		code = 31 * locations.hashCode();
		code = 31 * length;
		code = 31 * width;
		code = 31 * height;
		return code;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(!(obj instanceof SynchronizedBatteryData))
		{
			return false;
		}
		
		SynchronizedBatteryData data = (SynchronizedBatteryData)obj;
		
		if(!data.locations.equals(locations))
		{
			return false;
		}
		
		if(data.length != length || data.width != width || data.height != height)
		{
			return false;
		}
		
		return true;
	}
}
