package mffs.security.module

import java.util.Set

import mffs.field.TileElectromagneticProjector
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ChatComponentText
import resonant.api.mffs.machine.IProjector
import resonant.lib.utility.LanguageUtility
import universalelectricity.core.transform.vector.Vector3

class ItemModuleBroadcast extends ItemModuleDefense
{
  override def onProject(projector: IProjector, fields: Set[Vector3]): Boolean =
  {
    val proj = projector.asInstanceOf[TileElectromagneticProjector]
    val entities = getEntitiesInField(projector)

    //TODO: CUSTOM BROADCAST MESSAGE!
    entities.view
      .filter(_.isInstanceOf[EntityPlayer])
      .map(_.asInstanceOf[EntityPlayer])
      .foreach(_.addChatMessage(new ChatComponentText("[" + proj.getInventoryName + "] " + LanguageUtility.getLocal("message.moduleWarn.warn"))))

    return false
  }
}