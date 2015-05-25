package com.calclavia.edx.mffs.security.module

import java.util

import com.calclavia.edx.mffs.api.machine.Projector
import com.calclavia.edx.mffs.field.BlockProjector
import com.calclavia.edx.mffs.security.MFFSPermissions
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
			.map(_.get(classOf[Player]).get())
			.filter(p => !projector.hasPermission(p.getID, MFFSPermissions.defense))
			.foreach(Game.instance.networkManager.sendChat(_, Game.instance.languageManager.translate("message.moduleWarn.warn")))
		return false
	}

	override def getID: String = "moduleBroadcast"
}