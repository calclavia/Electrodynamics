/**
 * 
 */
package resonantinduction.battery;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
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
	
	public int inventoryID;
	
	public TileEntityBattery()
	{
		doPacket = false;
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();
	}
	
	@Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
        super.readFromNBT(nbtTags);
        
        if(structure == null)
        {
	        inventoryID = nbtTags.getInteger("inventoryID");
	
	        if(inventoryID != -1)
	        {
	        	//inventory
	        }
        }
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        nbtTags.setInteger("inventoryID", inventoryID);
        
        //inventory
    }
	
	public void update()
	{
		
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
