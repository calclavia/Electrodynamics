package mffs.security.module

import mffs.ModularForceFieldSystem
import net.minecraft.entity.{EntityLivingBase, INpc}
import net.minecraft.entity.monster.IMob
import net.minecraft.entity.player.EntityPlayer
import resonant.api.mffs.security.IInterdictionMatrix

class ItemModuleAntiFriendly extends ItemModuleDefense
{
  override def onDefend(interdictionMatrix: IInterdictionMatrix, entityLiving: EntityLivingBase): Boolean =
  {
    if (!(entityLiving.isInstanceOf[IMob] && !(entityLiving.isInstanceOf[INpc])) && !(entityLiving.isInstanceOf[EntityPlayer]))
    {
      entityLiving.setHealth(1)
      entityLiving.attackEntityFrom(ModularForceFieldSystem.damageFieldShock, 100)
    }

    return false
  }
}