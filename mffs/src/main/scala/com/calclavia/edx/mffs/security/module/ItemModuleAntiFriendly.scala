package com.calclavia.edx.mffs.security.module

import java.util

import com.calclavia.edx.mffs.api.machine.Projector
import nova.core.component.misc.Damageable
import nova.core.component.misc.Damageable.DamageType
import nova.core.entity.component.Player
import nova.core.util.transform.vector.Vector3i

class ItemModuleAntiFriendly extends ItemModuleDefense {

	override def onCreateField(projector: Projector, field: util.Set[Vector3i]): Boolean = {
		val entities = getEntitiesInField(projector)

		entities.view
			.filter(entity => entity.get(classOf[Damageable]).isPresent && /*!(entity.isInstanceOf[IMob] && !entity.isInstanceOf[INpc]) && */ !entity.get(classOf[Player]).isPresent)
			.map(_.get(classOf[Damageable]).get())
			.foreach(entity => {
			entity.damage(Double.PositiveInfinity, DamageType.generic)
		})

		return false
	}

	override def getID: String = "moduleAntiFriendly"

}