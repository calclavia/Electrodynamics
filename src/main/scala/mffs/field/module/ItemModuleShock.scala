package mffs.field.module

import mffs.base.ItemModule
import mffs.field.BlockForceField
import mffs.security.MFFSPermissions
import nova.core.block.Block
import nova.core.entity.Entity
import nova.core.entity.components.Damageable
import nova.core.entity.components.Damageable.DamageType
import nova.core.player.Player

class ItemModuleShock extends ItemModule {

	override def getID: String = "moduleShock"

	override def onFieldCollide(block: Block, entity: Entity) {
		if (entity.isInstanceOf[Damageable]) {
			if (entity.isInstanceOf[Player]) {
				val entityPlayer = entity.asInstanceOf[Entity with Player]
				if (block.asInstanceOf[BlockForceField].getProjector.hasPermission(entityPlayer.getUsername, MFFSPermissions.forceFieldWarp)) {
					return true
				}
			}

			entity.asInstanceOf[Damageable].damage(count(), DamageType.generic)
		}

		return true
	}
}