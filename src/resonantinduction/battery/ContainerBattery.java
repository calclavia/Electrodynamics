package resonantinduction.battery;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import resonantinduction.battery.BatteryManager.SlotBattery;
import resonantinduction.battery.BatteryManager.SlotOut;

public class ContainerBattery extends Container
{
	private TileEntityBattery tileEntity;
	
	public ContainerBattery(InventoryPlayer inventory, TileEntityBattery unit)
	{
		tileEntity = unit;
		addSlotToContainer(new SlotBattery(unit, 0, 8, 22));
		addSlotToContainer(new SlotOut(unit, 1, 8, 58));
		addSlotToContainer(new SlotBattery(unit, 2, 31, 22));
		addSlotToContainer(new SlotBattery(unit, 3, 31, 58));
		
		int slotX;
		
        for(slotX = 0; slotX < 3; ++slotX)
        {
            for(int slotY = 0; slotY < 9; ++slotY)
            {
                addSlotToContainer(new Slot(inventory, slotY + slotX * 9 + 9, 8 + slotY * 18, 125 + slotX * 18));
            }
        }

        for(slotX = 0; slotX < 9; ++slotX)
        {
            addSlotToContainer(new Slot(inventory, slotX, 8 + slotX * 18, 183));
        }
        
        tileEntity.openChest();
        tileEntity.playersUsing.add(inventory.player);
    }
    
	@Override
    public void onContainerClosed(EntityPlayer entityplayer)
    {
		super.onContainerClosed(entityplayer);
		tileEntity.closeChest();
		tileEntity.playersUsing.remove(entityplayer);
    }
	
	@Override
    public boolean canInteractWith(EntityPlayer entityplayer)
	{
        return tileEntity.isUseableByPlayer(entityplayer);
	}
}
