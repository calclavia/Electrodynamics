package com.calclavia.edx.quantum.machine.quantum

import com.calclavia.edx.quantum.QuantumContent
import QuantumContent
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.inventory.{Container, IInventory, Slot}
import net.minecraft.item.ItemStack

class ContainerQuantumAssembler extends Container
{
  private var tileEntity: TileQuantumAssembler = null

  def this(par1InventoryPlayer: InventoryPlayer, tileEntity: TileQuantumAssembler)
  {
    this()
    this.tileEntity = tileEntity
    this.addSlotToContainer(new Slot(tileEntity.asInstanceOf[IInventory], 0, 80, 40))
    this.addSlotToContainer(new Slot(tileEntity.asInstanceOf[IInventory], 1, 53, 56))
    this.addSlotToContainer(new Slot(tileEntity.asInstanceOf[IInventory], 2, 107, 56))
    this.addSlotToContainer(new Slot(tileEntity.asInstanceOf[IInventory], 3, 53, 88))
    this.addSlotToContainer(new Slot(tileEntity.asInstanceOf[IInventory], 4, 107, 88))
    this.addSlotToContainer(new Slot(tileEntity.asInstanceOf[IInventory], 5, 80, 103))
    this.addSlotToContainer(new Slot(tileEntity.asInstanceOf[IInventory], 6, 80, 72))

    for (var3 <- 0 to 3)
    {
      var var4: Int = 0
      for (var4 <- 0 to 9)
      {
        this.addSlotToContainer(new Slot(par1InventoryPlayer, var4 + var3 * 9 + 9, 8 + var4 * 18, 148 + var3 * 18))

      }
    }
    for (var3 <- 0 to 9)
    {
      this.addSlotToContainer(new Slot(par1InventoryPlayer, var3, 8 + var3 * 18, 206))
    }
  }

  override def onContainerClosed(entityplayer: EntityPlayer)
  {
    super.onContainerClosed(entityplayer)
  }

  def canInteractWith(par1EntityPlayer: EntityPlayer): Boolean =
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
      if (par1 > 6)
      {
        if (itemStack.getItem eq QuantumContent.itemDarkMatter)
        {
          if (!this.mergeItemStack(itemStack, 0, 6, false))
          {
            return null
          }
        }
        else if (!this.getSlot(6).getHasStack)
        {
          if (!this.mergeItemStack(itemStack, 6, 7, false))
          {
            return null
          }
        }
      }
      else if (!this.mergeItemStack(itemStack, 7, 36 + 7, false))
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