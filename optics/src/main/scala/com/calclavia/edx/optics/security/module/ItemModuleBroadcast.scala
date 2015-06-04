package com.calclavia.edx.optics.security.module

import java.util

import com.calclavia.edx.optics.api.machine.Projector
import com.calclavia.edx.optics.field.BlockProjector
import com.calclavia.edx.optics.security.MFFSPermissions
import nova.core.entity.component.Player
import nova.core.game.Game
import nova.core.util.transform.vector.Vector3i

class ItemModuleBroadcast extends ItemModuleDefense {
	override def onCreateField(projector: Projector, field: util.Set[Vector3i]): Boolean = {
		val proj = projector.asInstanceOf[BlockProjector]
		val entities = getEntitiesInField(projector)

		//TODO: Add custom broadcast messages
		entities.view
			.filter(_.has(classOf[Player]))
			.map(_.get(classOf[Player]))
			.filter(p => !projector.hasPermission(p.getID, MFFSPermissions.defense))
			.foreach(Game.network.sendChat(_, Game.language.translate("message.moduleWarn.warn")))
		return false
	}

	override def getID: String = "moduleBroadcast"
}