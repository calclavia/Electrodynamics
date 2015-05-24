/**
 *
 */
package com.calclavia.edx.core

import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.{Item, ItemStack}

/**
 * @author Calclavia
 *
 */
object EDXCreativeTab extends CreativeTabs(CreativeTabs.getNextID, "edx")
{
  var itemStack: ItemStack = null

  override def getTabIconItem: Item = itemStack.getItem
}