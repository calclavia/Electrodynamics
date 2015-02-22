package mffs.item.gui

/**
 * A temporary inventory used by items for copying
 * @author Calclavia
 */

class CopyInventory(Item: Item, slots: Int) extends ExternalInventory(null, slots)
{
  override def markDirty
  {
	  if (Item.getTagCompound != null && getStackInSlot(0) != null) {
		  getStackInSlot(0).setTagCompound(Item.getTagCompound.copy().asInstanceOf[NBTTagCompound])
	  }
  }
}
