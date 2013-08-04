/**
 * 
 */
package resonantinduction.battery;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
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

	@Override
	public void updateEntity()
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
