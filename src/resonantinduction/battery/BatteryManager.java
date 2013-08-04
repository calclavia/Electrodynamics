package resonantinduction.battery;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import resonantinduction.base.Vector3;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class BatteryManager implements ITickHandler
{
	public static final int WILDCARD = -1;
	
	public static Map<Integer, BatteryCache> dynamicInventories = new HashMap<Integer, BatteryCache>();
	
    /**
     * Grabs an inventory from the world's caches, and removes all the world's references to it.
     * @param world - world the cache is stored in
     * @param id - inventory ID to pull
     * @return correct Battery inventory cache
     */
    public static BatteryCache pullInventory(World world, int id)
    {
    	BatteryCache toReturn = dynamicInventories.get(id);
    	
    	for(Vector3 obj : dynamicInventories.get(id).locations)
    	{
    		TileEntityBattery tileEntity = (TileEntityBattery)obj.getTileEntity(world);
    		
    		if(tileEntity != null)
    		{
    			tileEntity.inventory = new HashSet<ItemStack>();
    			tileEntity.inventoryID = WILDCARD;
    		}
    	}
    	
    	dynamicInventories.remove(id);
    	
    	return toReturn;
    }
    
    /**
     * Updates a battery cache with the defined inventory ID with the parameterized values.
     * @param inventoryID - inventory ID of the battery
     * @param fluid - cached fluid of the battery
     * @param inventory - inventory of the battery
     * @param tileEntity - battery TileEntity
     */
    public static void updateCache(int inventoryID, HashSet<ItemStack> inventory, TileEntityBattery tileEntity)
    {
    	if(!dynamicInventories.containsKey(inventoryID))
    	{
    		BatteryCache cache = new BatteryCache();
    		cache.inventory = inventory;
    		cache.dimensionId = tileEntity.worldObj.provider.dimensionId;
    		cache.locations.add(new Vector3(tileEntity));
    		
    		dynamicInventories.put(inventoryID, cache);
    		
    		return;
    	}
    	
    	dynamicInventories.get(inventoryID).inventory = inventory;
    	
		dynamicInventories.get(inventoryID).locations.add(new Vector3(tileEntity));
    }
    
    /**
     * Grabs a unique inventory ID for a battery.
     * @return unique inventory ID
     */
    public static int getUniqueInventoryID()
    {
    	int id = 0;
    	
    	while(true)
    	{
    		for(Integer i : dynamicInventories.keySet())
    		{
    			if(id == i)
    			{
    				id++;
    				continue;
    			}
    		}
    		
    		return id;
    	}
    }
	
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData)
	{
		
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData)
	{
		if(tickData[0] instanceof World)
		{
			ArrayList<Integer> idsToKill = new ArrayList<Integer>();
			HashMap<Integer, HashSet<Vector3>> tilesToKill = new HashMap<Integer, HashSet<Vector3>>();
			
			World world = (World)tickData[0];
			
			if(!world.isRemote)
			{
				for(Map.Entry<Integer, BatteryCache> entry : dynamicInventories.entrySet())
				{
					int inventoryID = entry.getKey();
					
					if(entry.getValue().dimensionId == world.provider.dimensionId)
					{
						for(Vector3 obj : entry.getValue().locations)
						{
							TileEntityBattery tileEntity = (TileEntityBattery)obj.getTileEntity(world);
							
							if(tileEntity == null || tileEntity.inventoryID != inventoryID)
							{
								if(!tilesToKill.containsKey(inventoryID))
								{
									tilesToKill.put(inventoryID, new HashSet<Vector3>());
								}
								
								tilesToKill.get(inventoryID).add(obj);
							}
						}
					}
					
					if(entry.getValue().locations.isEmpty())
					{
						idsToKill.add(inventoryID);
					}
				}
				
				for(Map.Entry<Integer, HashSet<Vector3>> entry : tilesToKill.entrySet())
				{
					for(Vector3 obj : entry.getValue())
					{
						dynamicInventories.get(entry.getKey()).locations.remove(obj);
					}
				}
				
				for(int inventoryID : idsToKill)
				{	
					for(Vector3 obj : dynamicInventories.get(inventoryID).locations)
					{
						TileEntityBattery battery = (TileEntityBattery)obj.getTileEntity(world);
						
						if(battery != null)
						{
							battery.inventory = new HashSet<ItemStack>();
							battery.inventoryID = WILDCARD;
						}
					}
					
					dynamicInventories.remove(inventoryID);
				}
			}
		}
	}

	@Override
	public EnumSet<TickType> ticks()
	{
		return EnumSet.of(TickType.WORLD);
	}

	@Override
	public String getLabel()
	{
		return "BatteryMultiblockManager";
	}
	
	public static class BatteryCache
	{
		public Set<ItemStack> inventory = new HashSet<ItemStack>();
		public int dimensionId;
		
		public Set<Vector3> locations = new HashSet<Vector3>();
	}
}
