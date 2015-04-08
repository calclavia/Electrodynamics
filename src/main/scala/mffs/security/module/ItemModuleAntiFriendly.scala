package mffs.security.module

import java.util

import mffs.api.machine.Projector
import nova.core.entity.components.Damageable
import nova.core.entity.components.Damageable.DamageType
import nova.core.player.Player
import nova.core.util.transform.Vector3i

class ItemModuleAntiFriendly extends ItemModuleDefense {

	override def onCreateField(projector: Projector, field: util.Set[Vector3i]): Boolean = {
		val entities = getEntitiesInField(projector)

		entities.view
			.filter(entity => entity.isInstanceOf[Damageable] && /*!(entity.isInstanceOf[IMob] && !entity.isInstanceOf[INpc]) && */ !entity.isInstanceOf[Player])
			.map(_.asInstanceOf[Damageable])
			.foreach(entity => {
			entity.damage(Double.PositiveInfinity, DamageType.generic)
		})

		return false
	}
}