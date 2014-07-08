package mffs.security.module

import resonant.api.mffs.security.IInterdictionMatrix
import mffs.ModularForceFieldSystem
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.INpc
import net.minecraft.entity.monster.IMob

class ItemModuleAntiHostile extends ItemModuleDefense
{
  override def onDefend(interdictionMatrix: IInterdictionMatrix, entityLiving: EntityLivingBase): Boolean =
  {
    if (entityLiving.isInstanceOf[IMob] && !(entityLiving.isInstanceOf[INpc]))
    {
      entityLiving.attackEntityFrom(ModularForceFieldSystem.damageFieldShock, 20)
    }
    return false
  }
}