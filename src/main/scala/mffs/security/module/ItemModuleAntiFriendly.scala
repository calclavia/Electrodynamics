package mffs.security.module

import resonant.api.mffs.security.IInterdictionMatrix
import mffs.ModularForceFieldSystem
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.INpc
import net.minecraft.entity.monster.IMob
import net.minecraft.entity.player.EntityPlayer

class ItemModuleAntiFriendly(i: Int) extends ItemModuleInterdictionMatrix(i, "moduleAntiFriendly")
{
	override def onDefend(interdictionMatrix: IInterdictionMatrix, entityLiving: EntityLivingBase): Boolean =
	{
		if (!(entityLiving.isInstanceOf[IMob] && !(entityLiving.isInstanceOf[INpc])) && !(entityLiving.isInstanceOf[EntityPlayer]))
		{
			entityLiving.setHealth(1)
			entityLiving.attackEntityFrom(ModularForceFieldSystem.damagefieldShock, 100)
		}

		return false
	}
}