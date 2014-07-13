package mffs.security.module

import java.util.Set

import mffs.ModularForceFieldSystem
import net.minecraft.entity.monster.IMob
import net.minecraft.entity.{EntityLivingBase, INpc}
import resonant.api.mffs.machine.IProjector
import universalelectricity.core.transform.vector.Vector3

class ItemModuleAntiHostile extends ItemModuleDefense
{
  override def onProject(projector: IProjector, fields: Set[Vector3]): Boolean =
  {
    val entities = getEntitiesInField(projector)

    entities.view
      .filter(entity => entity.isInstanceOf[EntityLivingBase] && entity.isInstanceOf[IMob] && !entity.isInstanceOf[INpc])
      .map(_.asInstanceOf[EntityLivingBase])
      .foreach(_.attackEntityFrom(ModularForceFieldSystem.damageFieldShock, 20))

    return false
  }
}