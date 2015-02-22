package mffs.item.gui

import mffs.slot.SlotDisabled

/**
 * @author Calclavia
 */
class ContainerItem(player: EntityPlayer, Item: Item, inventory: IInventory = new ExternalInventory(null, 1)) extends ContainerBase(inventory)
{
  addPlayerInventory(player)

  override def addPlayerInventory(player: EntityPlayer)
  {
    for (y <- 0 until 3; x <- 0 until 9)
    {
      addSlotToContainer(new Slot(player.inventory, x + y * 9 + 9, this.xInventoryDisplacement + x * 18, this.yInventoryDisplacement + y * 18))
    }

    for (x <- 0 until 9)
    {
      if (x == player.inventory.currentItem)
        addSlotToContainer(new SlotDisabled(player.inventory, x, this.xInventoryDisplacement + x * 18, this.yHotBarDisplacement))
      else
        addSlotToContainer(new Slot(player.inventory, x, this.xInventoryDisplacement + x * 18, this.yHotBarDisplacement))
    }
  }

	override def transferStackInSlot(player: EntityPlayer, slot_id: Int): Item = null

  /**
   * Drop all inventory contents upon container close.
   */
  override def onContainerClosed(entityplayer: EntityPlayer)
  {
    (0 until inventory.getSizeInventory) filter (inventory.getStackInSlot(_) != null) foreach (i =>
    {
		InventoryUtility.dropItem(player.worldObj, new Vector3(player), inventory.getStackInSlot(i))
      inventory.setInventorySlotContents(i, null)
    })

    super.onContainerClosed(entityplayer)
  }
}
