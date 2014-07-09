package mffs.item.gui

import mffs.item.card.ItemCardFrequency
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import resonant.lib.prefab.slot.SlotSpecific

/**
 * @author Calclavia
 */
class ContainerFrequency(player: EntityPlayer, itemStack: ItemStack) extends ContainerItem(player, itemStack)
{
  //addSlotToContainer(new SlotSpecific(inventory, 0, 0, 0, classOf[ItemCardFrequency]))
}
