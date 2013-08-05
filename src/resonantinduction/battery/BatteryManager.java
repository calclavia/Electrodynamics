package resonantinduction.battery;

import java.util.EnumSet;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import resonantinduction.api.IBattery;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class BatteryManager implements ITickHandler
{
	public static final int CELLS_PER_BATTERY = 16;
	
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData)
	{
		
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData)
	{
		
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
