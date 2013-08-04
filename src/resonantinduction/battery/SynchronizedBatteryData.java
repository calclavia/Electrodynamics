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
	
	public boolean didTick;
	
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
