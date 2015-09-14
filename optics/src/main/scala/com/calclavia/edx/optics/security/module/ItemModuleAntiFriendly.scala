package com.calclavia.edx.optics.security.module

import java.util

import com.calclavia.edx.optics.api.machine.Projector
import nova.core.component.misc.Damageable
import nova.core.component.misc.Damageable.DamageType
import nova.core.entity.component.Player
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D

class ItemModuleAntiFriendly extends ItemModuleDefense {

	override def onCreateField(projector: Projector, field: util.Set[Vector3D]): Boolean = {
		val entities = getEntitiesInField(projector)

		entities.view
			.filter(entity => entity.components.has(classOf[Damageable]) && /*!(entity.isInstanceOf[IMob] && !entity.isInstanceOf[INpc]) && */ !entity.components.has(classOf[Player]))
			.map(_.components.get(classOf[Damageable]))
			.foreach(entity => {
			entity.damage(Double.PositiveInfinity, DamageType.generic)
		})

		return false
	}
}