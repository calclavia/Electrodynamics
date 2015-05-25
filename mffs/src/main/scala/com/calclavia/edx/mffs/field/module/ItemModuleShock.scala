package com.calclavia.edx.mffs.field.module

import com.calclavia.edx.mffs.base.ItemModule
import com.calclavia.edx.mffs.field.BlockForceField
import com.calclavia.edx.mffs.security.MFFSPermissions
import nova.core.block.Block
import nova.core.entity.Entity
import nova.core.entity.component.Damageable.DamageType
import nova.core.entity.component.{Damageable, Player}

class ItemModuleShock extends ItemModule {

	override def getID: String = "moduleShock"

	override def onFieldCollide(block: Block, entity: Entity): Boolean = {
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