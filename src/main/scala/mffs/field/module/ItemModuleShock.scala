package mffs.field.module

import mffs.ModularForceFieldSystem
import mffs.base.ItemModule
import mffs.security.access.MFFSPermissions
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import resonant.api.mffs.security.IBiometricIdentifier

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
				if (biometricIdentifier.isAccessGranted(entityPlayer.username, MFFSPermissions.FORCE_FIELD_WARP))
				{
					return false
				}
			}
		}

		entity.attackEntityFrom(ModularForceFieldSystem.damagefieldShock, moduleStack.stackSize)
		return false
	}
}