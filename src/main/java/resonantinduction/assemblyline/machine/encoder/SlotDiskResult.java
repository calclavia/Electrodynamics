package resonantinduction.assemblyline.machine.encoder;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotDiskResult extends Slot
{

    public SlotDiskResult(IInventory par1iInventory, int par2, int par3, int par4)
    {
        super(par1iInventory, par2, par3, par4);
    }

    @Override
    public boolean isItemValid(ItemStack par1ItemStack)
    {
        return false;
    }

    @Override
    public void onPickupFromSlot(EntityPlayer par1EntityPlayer, ItemStack par2ItemStack)
    {
        super.onPickupFromSlot(par1EntityPlayer, par2ItemStack);

        if (this.inventory.getStackInSlot(0) != null)
        {
            this.inventory.getStackInSlot(0).stackSize--;
            if (this.inventory.getStackInSlot(0).stackSize <= 0)
            {
                this.inventory.setInventorySlotContents(0, null);
            }
        }
        /*
         * if (this.inventory.getStackInSlot(1) != null) {
         * this.inventory.getStackInSlot(1).stackSize--; if
         * (this.inventory.getStackInSlot(1).stackSize <= 1) {
         * this.inventory.setInventorySlotContents(1, null); } }
         */
    }
}
