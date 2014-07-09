package mffs.item.gui

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import resonant.lib.gui.{ContainerDummy, ContainerBase}
import resonant.lib.utility.inventory.ExternalInventory

/**
 * @author Calclavia
 */
class ContainerItem(player: EntityPlayer, itemStack: ItemStack, inventory: IInventory = new ExternalInventory(null, 1)) extends ContainerDummy//(inventory)
{
  //addPlayerInventonry(player)
}
