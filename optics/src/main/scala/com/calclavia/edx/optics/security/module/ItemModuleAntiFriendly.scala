package com.calclavia.edx.optics.security.module

import java.util

import com.calclavia.edx.optics.api.machine.Projector
import nova.core.component.misc.Damageable
import nova.core.component.misc.Damageable.DamageType
import nova.core.entity.component.Player
import nova.core.util.transform.vector.Vector3i

class ItemModuleAntiFriendly extends ItemModuleDefense {

	override def onCreateField(projector: Projector, field: util.Set[Vector3i]): Boolean = {
		val entities = getEntitiesInField(projector)

		entities.view
			.filter(entity => entity.has(classOf[Damageable]) && /*!(entity.isInstanceOf[IMob] && !entity.isInstanceOf[INpc]) && */ !entity.has(classOf[Player]))
			.map(_.get(classOf[Damageable]))
			.foreach(entity => {
			entity.damage(Double.PositiveInfinity, DamageType.generic)
		})

		return false
	}

	override def getID: String = "moduleAntiFriendly"

}