package mffs.item.gui

import mffs.item.card.ItemCardFrequency

/**
 * @author Calclavia
 */
class ContainerFrequency(player: EntityPlayer, Item: Item) extends ContainerItem(player, Item, new CopyInventory(Item, 1))
{
  addSlotToContainer(new SlotSpecific(inventory, 0, 81, 101, classOf[ItemCardFrequency]))
}
