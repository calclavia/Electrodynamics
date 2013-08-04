/**
 * 
 */
package resonantinduction.battery;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import resonantinduction.api.IBattery;
import resonantinduction.base.TileEntityBase;

/**
 * A modular battery with no GUI.
 * 
 * @author Calclavia
 */
public class TileEntityBattery extends TileEntityBase
{
	public Set<ItemStack> inventory = new HashSet<ItemStack>();
	
	private byte[] sideStatus = new byte[] { 0, 0, 0, 0, 0, 0 };
	
	public SynchronizedBatteryData structure;
	
	public boolean prevStructure;
	
	public boolean clientHasStructure;
	
	public int inventoryID;

	@Override
	public void updateEntity()
	{
		ticks++;
		//DO NOT SUPER
		
		if(worldObj.isRemote)
		{
			if(structure == null)
			{
				structure = new SynchronizedBatteryData();
			}
			
			prevStructure = clientHasStructure;
		}
		
		if(playersUsing.size() > 0 && ((worldObj.isRemote && !clientHasStructure) || (!worldObj.isRemote && structure == null)))
		{
			for(EntityPlayer player : playersUsing)
			{
				player.closeScreen();
			}
		}
		
		if(!worldObj.isRemote)
		{
			if(inventoryID != -1 && structure == null)
			{
				BatteryManager.updateCache(inventoryID, inventory, this);
			}
			
			if(structure == null && ticks == 5)
			{
				update();
			}
			
			if(prevStructure != (structure != null))
			{
				//packet
			}
			
			prevStructure = structure != null;
			
			if(structure != null)
			{
				structure.didTick = false;
				
				if(inventoryID != -1)
				{
					BatteryManager.updateCache(inventoryID, structure.inventory, this);
					
					inventory = structure.inventory;
				}
			}
		}
	}
	
	@Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
        super.readFromNBT(nbtTags);
        
        if(structure == null)
        {
	        inventoryID = nbtTags.getInteger("inventoryID");
	
	        if(inventoryID != BatteryManager.WILDCARD)
	        {
	            NBTTagList tagList = nbtTags.getTagList("Items");
	            inventory = new HashSet<ItemStack>();

	            for(int tagCount = 0; tagCount < tagList.tagCount(); tagCount++)
	            {
	                NBTTagCompound tagCompound = (NBTTagCompound)tagList.tagAt(tagCount);

                    inventory.add(ItemStack.loadItemStackFromNBT(tagCompound));
	            }
	        }
        }
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        nbtTags.setInteger("inventoryID", inventoryID);
        
        NBTTagList tagList = new NBTTagList();

        for(ItemStack itemStack : inventory)
        {
            if(itemStack != null)
            {
                NBTTagCompound tagCompound = new NBTTagCompound();
                itemStack.writeToNBT(tagCompound);
                tagList.appendTag(tagCompound);
            }
        }

        nbtTags.setTag("Items", tagList);
    }
	
	public void update()
	{
		if(!worldObj.isRemote && (structure == null || !structure.didTick))
		{
			new BatteryUpdateProtocol(this).updateBatteries();
			
			if(structure != null)
			{
				structure.didTick = true;
			}
		}
	}

	public float getMaxEnergyStored()
	{
		float max = 0;

		for (ItemStack itemStack : inventory)
		{
			if (itemStack != null)
			{
				if (itemStack.getItem() instanceof IBattery)
				{
					max += ((IBattery) itemStack.getItem()).getMaxEnergyStored();
				}
			}
		}

		return max;
	}
}
