package com.calclavia.edx.mffs.field.module

import com.calclavia.edx.mffs.base.ItemModule
import com.calclavia.edx.mffs.field.BlockForceField
import com.calclavia.edx.mffs.security.MFFSPermissions
import nova.core.block.Block
import nova.core.component.misc.Damageable
import nova.core.component.misc.Damageable.DamageType
import nova.core.entity.Entity
import nova.core.entity.component.Player

class ItemModuleShock extends ItemModule {

	override def getID: String = "moduleShock"

	override def onFieldCollide(block: Block, entity: Entity): Boolean = {
		if (entity.has(classOf[Damageable]) && entity.has(classOf[Player])) {
			val player = entity.get(classOf[Player])
			if (block.asInstanceOf[BlockForceField].getProjector.hasPermission(player.getUsername, MFFSPermissions.forceFieldWarp)) {
				return true
			}

			entity.get(classOf[Damageable]).damage(count(), DamageType.generic)
		}

		return true
	}
}