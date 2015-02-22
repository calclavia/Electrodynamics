package mffs.slot

/**
 * A disabled inventory slot.
 */
class SlotDisabled(inventory: IInventory, id: Int, par4: Int, par5: Int) extends Slot(inventory, id, par4, par5)
{
	override def isItemValid(Item: Item): Boolean = false

  override def canTakeStack(par1EntityPlayer: EntityPlayer): Boolean = false
}