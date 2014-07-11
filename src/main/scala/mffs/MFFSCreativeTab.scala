package mffs

import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.{Item, ItemStack}

object MFFSCreativeTab extends CreativeTabs(CreativeTabs.getNextID, "MFFS")
{
  override def getTabIconItem: Item = new ItemStack(Content.fortronCapacitor).getItem
}