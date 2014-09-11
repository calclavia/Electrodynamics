package mffs.security.module

import java.util.Set

import mffs.field.TileElectromagneticProjector
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.{ChatComponentTranslation, ChatComponentText}
import resonant.api.mffs.machine.IProjector
import universalelectricity.core.transform.vector.Vector3

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
      .foreach(_.addChatMessage(new ChatComponentTranslation("message.moduleWarn.warn")))
    return false
  }
}