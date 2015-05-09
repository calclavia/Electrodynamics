package mffs.security.module

import java.util

import mffs.api.machine.Projector
import mffs.field.BlockProjector
import mffs.security.MFFSPermissions
import nova.core.game.Game
import nova.core.player.Player
import nova.core.util.transform.Vector3i

class ItemModuleBroadcast extends ItemModuleDefense {
	override def onCreateField(projector: Projector, field: util.Set[Vector3i]): Boolean = {
		val proj = projector.asInstanceOf[BlockProjector]
		val entities = getEntitiesInField(projector)

		//TODO: Add custom broadcast messages
		entities.view
			.filter(_.isInstanceOf[Player])
			.map(_.asInstanceOf[Player])
			.filter(p => !projector.hasPermission(p.getID, MFFSPermissions.defense))
			.foreach(Game.instance.networkManager.sendChat(_, Game.instance.languageManager.translate("message.moduleWarn.warn")))
		return false
	}

	override def getID: String = "moduleBroadcast"
}