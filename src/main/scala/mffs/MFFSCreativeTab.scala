package mffs

import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.Item

object MFFSCreativeTab extends CreativeTabs(CreativeTabs.getNextID, "MFFS")
{
  override def getTabIconItem: Item = ModularForceFieldSystem.itemFocusMatix
}