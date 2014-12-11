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
object RICreativeTab extends CreativeTabs(CreativeTabs.getNextID, "resonantinduction")
{
  var itemStack: ItemStack = null

  override def getTabIconItem: Item =itemStack.getItem
}