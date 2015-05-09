package mffs.security.module

import java.util

import mffs.api.machine.Projector
import mffs.field.BlockProjector
import mffs.security.MFFSPermissions
import nova.core.game.Game
import nova.core.inventory.components.InventoryProvider
import nova.core.player.Player
import nova.core.util.Direction
import nova.core.util.transform.Vector3i

import scala.collection.convert.wrapAll._

class ItemModuleConfiscate extends ItemModuleDefense {

	override def onCreateField(projector: Projector, field: util.Set[Vector3i]): Boolean = {
		val proj = projector.asInstanceOf[BlockProjector]
		val entities = getEntitiesInField(projector)

		entities.view
			.filter(_.isInstanceOf[Player])
			.map(_.asInstanceOf[Player])
			.filter(player => !proj.hasPermission(player.getID, MFFSPermissions.bypassConfiscation))
			.foreach(
				player => {
					val filterItems = proj.getFilterItems
					val inventory = player.asInstanceOf[InventoryProvider].getInventory.head

					val relevantSlots = (0 until inventory.size)
						.filter(
							i => {
								val opCheckStack = inventory.get(i)
								opCheckStack.isPresent && proj.isInvertedFilter != (filterItems exists (_.sameItemType(opCheckStack.get)))
							}
						)

					relevantSlots foreach (i => {
						val opItem = inventory.get(i)
						if (opItem.isPresent) {
							proj.getInventory(Direction.UNKNOWN).head.add(opItem.get())
							inventory.remove(i, opItem.get().count())
						}

						if (relevantSlots.size > 0) {
							Game.instance.networkManager.sendChat(player, Game.instance.languageManager.translate("message.moduleConfiscate.confiscate"))
						}
					})
				})
		return false
	}

	override def getID: String = "moduleConfiscate"
}