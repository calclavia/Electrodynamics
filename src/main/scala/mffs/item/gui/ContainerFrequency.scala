package mffs.item.gui

import mffs.item.card.ItemCardFrequency
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import resonant.lib.prefab.slot.SlotSpecific

/**
 * @author Calclavia
 */
class ContainerFrequency(player: EntityPlayer, itemStack: ItemStack) extends ContainerItem(player, itemStack, new CopyInventory(itemStack, 1))
{
  addSlotToContainer(new SlotSpecific(inventory, 0, 81, 101, classOf[ItemCardFrequency]))
}
