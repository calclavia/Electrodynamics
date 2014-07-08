package mffs.security.module

import java.util.List

import mffs.base.ItemModule
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import resonant.api.mffs.modules.IInterdictionMatrixModule
import resonant.api.mffs.security.IInterdictionMatrix
import resonant.lib.utility.LanguageUtility
import resonant.lib.wrapper.WrapList._

class ItemModuleDefense extends ItemModule with IInterdictionMatrixModule
{
  override def addInformation(itemStack: ItemStack, player: EntityPlayer, info: List[_], b: Boolean)
  {
    info.add("\u00a74" + LanguageUtility.getLocal("tile.mffs:interdictionMatrix.name"))
    super.addInformation(itemStack, player, info, b)
  }

  def onDefend(interdictionMatrix: IInterdictionMatrix, entityLiving: EntityLivingBase): Boolean =
  {
    return false
  }
}