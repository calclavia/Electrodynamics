package edx.quantum.machine.accelerator

import edx.quantum.QuantumContent
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.{IInventory, Slot, SlotFurnace}
import net.minecraft.item.ItemStack
import resonant.lib.prefab.gui.ContainerBase

/**
 * Accelerator container
 */
class ContainerAccelerator(player: EntityPlayer, tileEntity: TileAccelerator) extends ContainerBase(player, tileEntity.asInstanceOf[IInventory])
{
  //Constructor
  addSlotToContainer(new Slot(tileEntity.asInstanceOf[IInventory], 0, 132, 26))
  addSlotToContainer(new Slot(tileEntity.asInstanceOf[IInventory], 1, 132, 51))
  addSlotToContainer(new SlotFurnace(player, tileEntity.asInstanceOf[IInventory], 2, 132, 75))
  addSlotToContainer(new SlotFurnace(player, tileEntity.asInstanceOf[IInventory], 3, 106, 75))
  addPlayerInventory(player)

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
      if (par1 > 2)
      {
        if (itemStack.getItem eq QuantumContent.itemCell)
        {
          if (!this.mergeItemStack(itemStack, 1, 2, false))
          {
            return null
          }
        }
        else if (!this.mergeItemStack(itemStack, 0, 1, false))
        {
          return null
        }
      }
      else if (!this.mergeItemStack(itemStack, 3, 36 + 3, false))
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