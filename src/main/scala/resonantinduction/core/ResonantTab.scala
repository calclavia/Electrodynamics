/**
 *
 */
package resonantinduction.core

import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.{ItemStack, Item}

/**
 * @author Calclavia
 *
 */
object ResonantTab extends CreativeTabs(CreativeTabs.getNextID, "ResonantInduction")
{
  var itemStack: ItemStack = null

  override def getTabIconItem: Item = itemStack.getItem
}