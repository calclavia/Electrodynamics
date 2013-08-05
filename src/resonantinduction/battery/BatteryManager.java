package resonantinduction.battery;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import resonantinduction.api.IBattery;

public class BatteryManager
{
	public static final int CELLS_PER_BATTERY = 16;
	
	public static class SlotOut extends Slot
	{
		public SlotOut(IInventory inventory, int index, int x, int y)
		{
			super(inventory, index, x, y);
		}
		
		@Override
		public boolean isItemValid(ItemStack itemstack)
		{
			return false;
		}
	}
	
	public static class SlotBattery extends Slot
	{
		public SlotBattery(IInventory inventory, int index, int x, int y)
		{
			super(inventory, index, x, y);
		}
		
		@Override
		public boolean isItemValid(ItemStack itemstack)
		{
			return itemstack.getItem() instanceof IBattery;
		}
	}
}
