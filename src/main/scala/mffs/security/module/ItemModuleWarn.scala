package mffs.security.module

import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ChatComponentText
import resonant.api.mffs.security.IInterdictionMatrix
import resonant.lib.utility.LanguageUtility

class ItemModuleWarn extends ItemModuleDefense
{
  override def onDefend(interdictionMatrix: IInterdictionMatrix, entityLiving: EntityLivingBase): Boolean =
  {
    val hasPermission: Boolean = false

    if (!hasPermission && entityLiving.isInstanceOf[EntityPlayer])
    {
      (entityLiving.asInstanceOf[EntityPlayer]).addChatMessage(new ChatComponentText("[" + interdictionMatrix.getInventoryName + "] " + LanguageUtility.getLocal("message.moduleWarn.warn")))
    }

    return false
  }
}