package mffs.security.module

import java.util.Set

import mffs.ModularForceFieldSystem
import net.minecraft.entity.monster.IMob
import net.minecraft.entity.{EntityLivingBase, INpc}
import nova.core.util.transform.Vector3d
import resonantengine.api.mffs.machine.IProjector

class ItemModuleAntiHostile extends ItemModuleDefense
{
  override def onProject(projector: IProjector, fields: Set[Vector3d]): Boolean =
  {
    val entities = getEntitiesInField(projector)

    entities.view
      .filter(entity => entity.isInstanceOf[EntityLivingBase] && entity.isInstanceOf[IMob] && !entity.isInstanceOf[INpc])
      .map(_.asInstanceOf[EntityLivingBase])
      .foreach(_.attackEntityFrom(ModularForceFieldSystem.damageFieldShock, 20))

    return false
  }
}