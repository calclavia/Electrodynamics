package mffs.security.module

import java.util

import mffs.api.machine.Projector
import mffs.field.BlockProjector
import mffs.security.MFFSPermissions
import nova.core.entity.components.Damageable
import nova.core.game.Game
import nova.core.player.Player
import nova.core.util.Direction
import nova.core.util.transform.Vector3i

import scala.collection.convert.wrapAll._

class ItemModuleAntiPersonnel extends ItemModuleDefense {

	override def onCreateField(projector: Projector, field: util.Set[Vector3i]): Boolean = {
		val proj = projector.asInstanceOf[BlockProjector]
		val entities = getEntitiesInField(projector)

		entities.view
			.collect { case player: Player with Damageable => player }
			.filter(p => !projector.hasPermission(p.getID, MFFSPermissions.defense))
			.foreach(
				player => {
					(0 until player.getInventory.size())
						.filter(player.getInventory.get(_) != null)
						.foreach(
							i => {
								val stackInSlot = player.getInventory.get(i)
								if (stackInSlot.isPresent) {
									proj.getInventory(Direction.UNKNOWN).head.add(stackInSlot.get)
									player.getInventory.remove(i, stackInSlot.get().count)
								}
							}
						)

					player.damage(1000)
					Game.instance.networkManager.sendChat(player, Game.instance.languageManager.getLocal("message.moduleAntiPersonnel.death"))
				}
			)

		return false
	}

	override def getID: String = "moduleAntiPersonnel"

}