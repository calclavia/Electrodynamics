package mffs.security.module

import java.util.List

import mffs.base.ItemModule
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import resonant.lib.utility.LanguageUtility
import resonant.lib.wrapper.WrapList._

class ItemModuleDefense extends ItemModule
{
  override def addInformation(itemStack: ItemStack, player: EntityPlayer, info: List[_], b: Boolean)
  {
    info.add("\u00a74" + LanguageUtility.getLocal("info.module.defense"))
    super.addInformation(itemStack, player, info, b)
  }

}