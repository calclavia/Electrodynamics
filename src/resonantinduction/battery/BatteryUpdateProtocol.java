package resonantinduction.battery;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import resonantinduction.base.ListUtil;
import resonantinduction.base.Vector3;

public class BatteryUpdateProtocol
{
	/** The battery nodes that have already been iterated over. */
	public Set<TileEntityBattery> iteratedNodes = new HashSet<TileEntityBattery>();
	
	/** The structures found, all connected by some nodes to the pointer. */
	public SynchronizedBatteryData structureFound = null;
	
	/** The original block the calculation is getting run from. */
	public TileEntity pointer;
	
	public BatteryUpdateProtocol(TileEntity tileEntity)
	{
		pointer = tileEntity;
	}
	
	/**
	 * Recursively loops through each node connected to the given TileEntity.
	 * @param tile - the TileEntity to loop over
	 */
	public void loopThrough(TileEntity tile)
	{
		if(structureFound == null)
		{
	    	World worldObj = tile.worldObj;
	    	
			int origX = tile.xCoord, origY = tile.yCoord, origZ = tile.zCoord;
			
			boolean isCorner = true;
			boolean rightBlocks = true;
			 
			Set<Vector3> locations = new HashSet<Vector3>();
			 
			int xmin = 0, xmax = 0, ymin = 0, ymax = 0, zmin = 0, zmax = 0;
			 
			int x = 0, y = 0, z = 0;
			 
			if((isBattery(origX + 1, origY, origZ) && isBattery(origX - 1, origY, origZ)) ||
					(isBattery(origX, origY + 1, origZ) && isBattery(origX, origY - 1, origZ)) ||
					(isBattery(origX, origY, origZ + 1) && isBattery(origX, origY, origZ - 1)))
			{
			    isCorner = false;
			}
			
			if(isCorner)
			{
			    if(isBattery(origX+1, origY, origZ))
			    {
			        xmin = 0;
			        
			        while(isBattery(origX+x+1, origY, origZ))
	        		{
			            x++;
			        }
			        
			        xmax = x;
			    }
			    else {
			        xmax = 0;
			        
			        while(isBattery(origX+x-1, origY, origZ))
			        {
			            x--;
			        }
			        
			        xmin = x;
			    }
			   
			    if(isBattery(origX, origY+1, origZ))
			    {
			        ymin = 0;
			        
			        while(isBattery(origX, origY+y+1, origZ))
			        {
			            y++;
			        }
			        
			        ymax = y;
			    } 
			    else {
			        ymax = 0;
			        
			        while(isBattery(origX, origY+y-1 ,origZ))
			        {
			            y--;
			        }
			        
			        ymin = y;
			    }
			   
			    if(isBattery(origX, origY, origZ+1))
			    {
			        zmin = 0;
			        
			        while(isBattery(origX, origY, origZ+z+1))
			        {
			            z++;
			        }
			        
			        zmax = z;
			    } 
			    else {
			        zmax = 0;
			        
			        while(isBattery(origX, origY, origZ+z-1))
			        {
			            z--;
			        }
			        
			        zmin = z;
			    }
			   
			    for(x = xmin; x <= xmax; x++)
			    {
			        for(y = ymin; y <= ymax; y++)
			        {
			            for(z = zmin; z <= zmax; z++)
			            {
		                    if(!isBattery(origX+x, origY+y, origZ+z))
		                    {
		                        rightBlocks = false;
		                        break;
		                    }
		                    else {
		                        locations.add(new Vector3(tile).translate(new Vector3(x, y, z)));
		                    }
			            }
			            
			            if(!rightBlocks)
			            {
			                break;
			            }
			        }
			        
			        if(!rightBlocks)
			        {
			        	break;
			        }
			    }
		    }
			
			if(locations.size() >= 1 && locations.size() < 512)
			{
				if(rightBlocks && isCorner)
				{
					SynchronizedBatteryData structure = new SynchronizedBatteryData();
					structure.locations = locations;
					structure.length = Math.abs(xmax-xmin)+1;
					structure.height = Math.abs(ymax-ymin)+1;
					structure.width = Math.abs(zmax-zmin)+1;
					
					if(structure.getVolume() > 1)
					{
						structure.isMultiblock = true;
					}
		
					if(structure.locations.contains(new Vector3(pointer)))
					{
						structureFound = structure;
					}
				}
			}
		}
		
		iteratedNodes.add((TileEntityBattery)tile);
		
		for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			TileEntity tileEntity = new Vector3(tile).getFromSide(side).getTileEntity(tile.worldObj);
			
			if(tileEntity instanceof TileEntityBattery)
			{
				if(!iteratedNodes.contains(tileEntity))
				{
					loopThrough(tileEntity);
				}
			}
		}
	}
	
	private boolean isBattery(int x, int y, int z)
	{
		if(pointer.worldObj.getBlockTileEntity(x, y, z) instanceof TileEntityBattery)
		{
			return true;
		}
		
		return false;
	}
	
	public void disperseCells()
	{
		SynchronizedBatteryData oldStructure = null;
		
		for(TileEntityBattery tile : iteratedNodes)
		{
			if(tile.structure.isMultiblock)
			{
				oldStructure = tile.structure;
				break;
			}
		}
		
		if(oldStructure != null)
		{
			int maxCells = iteratedNodes.size()*BatteryManager.CELLS_PER_BATTERY;
			
			//TODO eject these
			List<ItemStack> rejected = ListUtil.capRemains(oldStructure.inventory, maxCells);
			
			ArrayList<List<ItemStack>> inventories = ListUtil.split(ListUtil.cap(oldStructure.inventory, maxCells), iteratedNodes.size());
			List<TileEntityBattery> iterList = ListUtil.asList(iteratedNodes);
			
			boolean didVisibleInventory = false;
			
			for(int i = 0; i < iterList.size(); i++)
			{
				TileEntityBattery tile = iterList.get(i);
				tile.structure = SynchronizedBatteryData.getBase(tile, inventories.get(i));
				
				if(!didVisibleInventory)
				{
					tile.structure.visibleInventory = oldStructure.visibleInventory;
					didVisibleInventory = true;
				}
			}
		}
	}
	
	/**
	 * Runs the protocol and updates all batteries that make a part of the multiblock battery.
	 */
	public void updateBatteries()
	{
		loopThrough(pointer);
		
		if(structureFound != null)
		{
			for(TileEntityBattery tileEntity : iteratedNodes)
			{
				if(!structureFound.locations.contains(new Vector3(tileEntity)))
				{
					disperseCells();
					
					return;
				}
			}
			
			for(Vector3 obj : structureFound.locations)
			{
				TileEntityBattery tileEntity = (TileEntityBattery)obj.getTileEntity(pointer.worldObj);
				
				structureFound.inventory = ListUtil.merge(structureFound.inventory, tileEntity.structure.inventory);
				
				if(tileEntity.structure.hasVisibleInventory())
				{
					structureFound.visibleInventory = tileEntity.structure.visibleInventory;
				}
				
				tileEntity.structure = structureFound;
			}
			
			structureFound.inventory = ListUtil.cap(structureFound.inventory, structureFound.getMaxCells());
		}
		else {
			disperseCells();
		}
	}
}
