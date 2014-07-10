package mffs.item.gui

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import resonant.lib.utility.inventory.ExternalInventory

/**
 * A temporary inventory used by items for copying
 * @author Calclavia
 */

class CopyInventory(itemStack: ItemStack, slots: Int) extends ExternalInventory(null, slots)
{
  override def markDirty
  {
    if (itemStack.getTagCompound != null && getStackInSlot(0) != null)
      getStackInSlot(0).setTagCompound(itemStack.getTagCompound.copy().asInstanceOf[NBTTagCompound])
  }
}
