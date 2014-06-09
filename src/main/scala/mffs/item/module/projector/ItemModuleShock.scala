package mffs.item.module.projector

import mffs.ModularForceFieldSystem
import mffs.item.module.ItemModule
import net.minecraft.entity.Entity
import net.minecraft.world.World
import net.minecraft.item.ItemStack
import calclavia.api.mffs.security.{IBiometricIdentifier, Permission}
import net.minecraft.entity.player.EntityPlayer

class ItemModuleShock(i: Int) extends ItemModule(i, "moduleShock")
{
	override def onCollideWithForceField(world: World, x: Int, y: Int, z: Int, entity: Entity, moduleStack: ItemStack): Boolean =
	{
		if (entity.isInstanceOf[EntityPlayer])
		{
			val entityPlayer = entity.asInstanceOf[EntityPlayer]

			val biometricIdentifier: IBiometricIdentifier = ModularForceFieldSystem.blockForceField.getProjector(world, x, y, z).getBiometricIdentifier()

			if (biometricIdentifier != null)
			{
				if (biometricIdentifier.isAccessGranted(entityPlayer.username, Permission.FORCE_FIELD_WARP))
				{
					return false
				}
			}
		}

		entity.attackEntityFrom(ModularForceFieldSystem.damagefieldShock, moduleStack.stackSize)
		return false
	}
}