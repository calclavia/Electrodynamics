package mffs.slot

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.{IInventory, Slot}
import net.minecraft.item.ItemStack

/**
 * A disabled inventory slot.
 */
class SlotDisabled(inventory: IInventory, id: Int, par4: Int, par5: Int) extends Slot(inventory, id, par4, par5)
{
  override def isItemValid(itemStack: ItemStack): Boolean = false

  override def canTakeStack(par1EntityPlayer: EntityPlayer): Boolean = false
}