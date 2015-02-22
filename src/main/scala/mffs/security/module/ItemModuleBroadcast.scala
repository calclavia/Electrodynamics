package mffs.security.module

import java.util.Set

import mffs.field.TileElectromagneticProjector
import mffs.security.MFFSPermissions

class ItemModuleBroadcast extends ItemModuleDefense
{
	override def onProject(projector: IProjector, fields: Set[Vector3d]): Boolean =
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