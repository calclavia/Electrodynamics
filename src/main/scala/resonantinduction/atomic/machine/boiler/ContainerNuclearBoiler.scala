package resonantinduction.atomic.machine.boiler

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.{IInventory, Slot}
import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.FluidContainerRegistry
import resonant.lib.prefab.gui.ContainerBase
import resonant.lib.prefab.slot.{SlotEnergyItem, SlotSpecific}
import resonantinduction.atomic.AtomicContent

/**
 * Nuclear boiler container
 */
object ContainerNuclearBoiler
{
    private final val slotCount: Int = 4
}

class ContainerNuclearBoiler(player: EntityPlayer, tileEntity: TileNuclearBoiler) extends ContainerBase(player, tileEntity.asInstanceOf[IInventory])
{
    //Constructor
    this.addSlotToContainer(new SlotEnergyItem(tileEntity.asInstanceOf[IInventory], 0, 56, 26))
    this.addSlotToContainer(new Slot(tileEntity.asInstanceOf[IInventory], 1, 25, 50))
    this.addSlotToContainer(new Slot(tileEntity.asInstanceOf[IInventory], 2, 136, 50))
    this.addSlotToContainer(new SlotSpecific(tileEntity.asInstanceOf[IInventory], 3, 81, 26, new ItemStack(AtomicContent.itemYellowCake), new ItemStack(AtomicContent.blockUraniumOre)))
    this.addPlayerInventory(player)

    override def canInteractWith(par1EntityPlayer: EntityPlayer): Boolean =
    {
        return this.tileEntity.isUseableByPlayer(par1EntityPlayer)
    }

    /**
     * Called to transfer a stack from one inventory to the other eg. when shift clicking.
     */
    override def transferStackInSlot(par1EntityPlayer: EntityPlayer, slotID: Int): ItemStack =
    {
        var var2: ItemStack = null
        val slot: Slot = this.inventorySlots.get(slotID).asInstanceOf[Slot]
        if (slot != null && slot.getHasStack)
        {
            val itemStack: ItemStack = slot.getStack
            var2 = itemStack.copy
            if (slotID >= slotCount)
            {
                if (this.getSlot(0).isItemValid(itemStack))
                {
                    if (!this.mergeItemStack(itemStack, 0, 1, false))
                    {
                        return null
                    }
                }
                else if (AtomicContent.FLUIDSTACK_WATER.isFluidEqual(FluidContainerRegistry.getFluidForFilledItem(itemStack)))
                {
                    if (!this.mergeItemStack(itemStack, 1, 2, false))
                    {
                        return null
                    }
                }
                else if (this.getSlot(3).isItemValid(itemStack))
                {
                    if (!this.mergeItemStack(itemStack, 3, 4, false))
                    {
                        return null
                    }
                }
                else if (slotID < 27 + slotCount)
                {
                    if (!this.mergeItemStack(itemStack, 27 + slotCount, 36 + slotCount, false))
                    {
                        return null
                    }
                }
                else if (slotID >= 27 + slotCount && slotID < 36 + slotCount && !this.mergeItemStack(itemStack, 4, 30, false))
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
                slot.putStack(null.asInstanceOf[ItemStack])
            }
            else
            {
                slot.onSlotChanged
            }
            if (itemStack.stackSize == var2.stackSize)
            {
                return null
            }
            slot.onPickupFromSlot(par1EntityPlayer, itemStack)
        }
        return var2
    }
}