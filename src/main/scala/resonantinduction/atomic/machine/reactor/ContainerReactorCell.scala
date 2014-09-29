package resonantinduction.atomic.machine.reactor

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import resonant.lib.gui.ContainerBase
import resonant.lib.prefab.slot.SlotSpecific
import resonantinduction.atomic.items.{ItemBreederFuel, ItemFissileFuel}

class ContainerReactorCell(player: EntityPlayer, tileEntity: TileReactorCell) extends ContainerBase(tileEntity)
{
    //Constructor
    this.addSlotToContainer(new SlotSpecific(tileEntity, 0, 79, 17, classOf[ItemFissileFuel], classOf[ItemBreederFuel]))
    this.addPlayerInventory(player)

    /**
     * Called to transfer a stack from one inventory to the other eg. when shift clicking.
     */
    override def transferStackInSlot(par1EntityPlayer: EntityPlayer, par1: Int): ItemStack =
    {
        var var2: ItemStack = null
        val var3: Slot = this.inventorySlots.get(par1).asInstanceOf[Slot]
        if (var3 != null && var3.getHasStack)
        {
            val itemStack: ItemStack = var3.getStack
            var2 = itemStack.copy
            if (par1 >= this.slotCount)
            {
                if (this.getSlot(0).isItemValid(itemStack))
                {
                    if (!this.mergeItemStack(itemStack, 0, 1, false))
                    {
                        return null
                    }
                }
                else if (par1 < 27 + this.slotCount)
                {
                    if (!this.mergeItemStack(itemStack, 27 + slotCount, 36 + slotCount, false))
                    {
                        return null
                    }
                }
                else if (par1 >= 27 + slotCount && par1 < 36 + slotCount && !this.mergeItemStack(itemStack, 4, 30, false))
                {
                    return null
                }
            }
            else if (!this.mergeItemStack(itemStack, slotCount, 36 + slotCount, false))
            {
                return null
            }
            if (itemStack.stackSize == 0)
            {
                var3.putStack(null.asInstanceOf[ItemStack])
            }
            else
            {
                var3.onSlotChanged
            }
            if (itemStack.stackSize == var2.stackSize)
            {
                return null
            }
            var3.onPickupFromSlot(par1EntityPlayer, itemStack)
        }
        return var2
    }
}