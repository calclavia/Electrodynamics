package mffs.security.module

import java.util.Set

import mffs.field.TileElectromagneticProjector
import mffs.security.MFFSPermissions
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ChatComponentTranslation
import resonant.api.mffs.machine.IProjector
import resonant.lib.transform.vector.Vector3

class ItemModuleBroadcast extends ItemModuleDefense
{
  override def onProject(projector: IProjector, fields: Set[Vector3]): Boolean =
  {
    val proj = projector.asInstanceOf[TileElectromagneticProjector]
    val entities = getEntitiesInField(projector)

    //TODO: Add custom broadcast messages
    entities.view
    .filter(_.isInstanceOf[EntityPlayer])
    .map(_.asInstanceOf[EntityPlayer])
    .filter(p => !projector.hasPermission(p.getGameProfile, MFFSPermissions.defense))
    .foreach(_.addChatMessage(new ChatComponentTranslation("message.moduleWarn.warn")))
    return false
  }
}