package mffs.security.module

import java.util.Set

import mffs.ModularForceFieldSystem

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