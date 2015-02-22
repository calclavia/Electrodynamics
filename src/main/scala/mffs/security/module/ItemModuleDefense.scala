package mffs.security.module

import java.util.List

import mffs.base.ItemModule

class ItemModuleDefense extends ItemModule
{
	override def addInformation(Item: Item, player: EntityPlayer, info: List[_], b: Boolean)
  {
	  info.add("\u00a74" + Game.instance.get.languageManager.getLocal("info.module.defense"))
	  super.addInformation(Item, player, info, b)
  }

}