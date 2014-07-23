package resonantinduction.archaic.crate

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import resonant.api.IInventoryProvider
import resonant.lib.utility.inventory.ExternalInventory
import scala.util.control.Breaks._

class InventoryCrate(crate: IInventoryProvider) extends ExternalInventory(crate: IInventoryProvider, 512)
{

  /** Clones the single stack into an inventory format for automation interaction */
  def buildInventory(sampleStack: ItemStack)
  {
    this.containedItems = new Array[ItemStack](this.getSizeInventory)
    if (sampleStack != null && sampleStack.getItem != null)
    {
      var baseStack: ItemStack = sampleStack.copy
      var itemsLeft: Int = baseStack.stackSize
        for(slot <- this.getContainedItems.length)
        {
            val stackL: Int = Math.min(Math.min(itemsLeft, baseStack.getMaxStackSize), this.getInventoryStackLimit)
            var st = baseStack.copy
            st.stackSize = stackL

            this.setInventorySlotContents(slot, st)
            itemsLeft -= stackL

            if (baseStack.stackSize <= 0)
            {
              baseStack = null
              break
            }
      }
    }
  }

  override def getSizeInventory: Int =
  {
    if (this.host.isInstanceOf[TileCrate])
    {
      return (this.host.asInstanceOf[TileCrate]).getSlotCount
    }
    return 512
  }

  override def save(nbt: NBTTagCompound)
  {
  }

  override def load(nbt: NBTTagCompound)
  {
    if (nbt.hasKey("Items"))
    {
      super.load(nbt)
    }
  }
}