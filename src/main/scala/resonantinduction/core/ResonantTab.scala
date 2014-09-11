/**
 *
 */
package resonantinduction.core

import net.minecraft.creativetab.CreativeTabs
import net.minecraft.init.Items
import net.minecraft.item.{Item, ItemStack}

/**
 * @author Calclavia
 *
 */
object ResonantTab extends CreativeTabs(CreativeTabs.getNextID, "resonantinduction")
{
  var itemStack: ItemStack = null

  override def getTabIconItem: Item =
  {
    if (itemStack != null)
      return itemStack.getItem
    else
      return Items.iron_ingot
  }

  def itemStack(item: ItemStack)
  { itemStack = item }

  def tab = this
}