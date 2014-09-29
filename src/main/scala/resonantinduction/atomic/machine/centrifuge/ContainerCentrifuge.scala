package resonantinduction.atomic.machine.centrifuge

import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.inventory.{IInventory, Slot, SlotFurnace}
import net.minecraft.item.ItemStack
import resonant.lib.gui.ContainerBase
import resonant.lib.prefab.slot.SlotEnergyItem
import resonantinduction.atomic.Atomic

class ContainerCentrifuge(par1InventoryPlayer: InventoryPlayer, tileEntity: TileCentrifuge) extends ContainerBase(tileEntity)
{
    //Constructor
    this.addSlotToContainer(new SlotEnergyItem(tileEntity.asInstanceOf[IInventory], 0, 131, 26))
    this.addSlotToContainer(new Slot(tileEntity.asInstanceOf[IInventory], 1, 25, 50))
    this.addSlotToContainer(new SlotFurnace(par1InventoryPlayer.player, tileEntity.asInstanceOf[IInventory], 2, 81, 26))
    this.addSlotToContainer(new SlotFurnace(par1InventoryPlayer.player, tileEntity.asInstanceOf[IInventory], 3, 101, 26))
    this.addPlayerInventory(par1InventoryPlayer.player)

    override def onContainerClosed(entityplayer: EntityPlayer)
    {
        super.onContainerClosed(entityplayer)
    }

    override def canInteractWith(par1EntityPlayer: EntityPlayer): Boolean =
    {
        return this.tileEntity.isUseableByPlayer(par1EntityPlayer)
    }

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
            if (par1 >= slotCount)
            {
                if (this.getSlot(0).isItemValid(itemStack))
                {
                    if (!this.mergeItemStack(itemStack, 0, 1, false))
                    {
                        return null
                    }
                }
                else if (Atomic.isItemStackUraniumOre(itemStack))
                {
                    if (!this.mergeItemStack(itemStack, 1, 2, false))
                    {
                        return null
                    }
                }
                else if (Atomic.isItemStackEmptyCell(itemStack))
                {
                    if (!this.mergeItemStack(itemStack, 3, 4, false))
                    {
                        return null
                    }
                }
                else if (par1 < 27 + slotCount)
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