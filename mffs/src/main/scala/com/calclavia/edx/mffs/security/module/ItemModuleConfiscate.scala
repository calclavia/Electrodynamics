package com.calclavia.edx.mffs.security.module

import java.util

import com.calclavia.edx.mffs.api.machine.Projector
import com.calclavia.edx.mffs.field.BlockProjector
import com.calclavia.edx.mffs.security.MFFSPermissions
import nova.core.entity.component.Player
import nova.core.game.Game
import nova.core.inventory.Inventory
import nova.core.util.transform.vector.Vector3i

class ItemModuleConfiscate extends ItemModuleDefense {

	override def onCreateField(projector: Projector, field: util.Set[Vector3i]): Boolean = {
		val proj = projector.asInstanceOf[BlockProjector]
		val entities = getEntitiesInField(projector)

		entities.view
			.filter(_.has(classOf[Player]))
			.map(_.get(classOf[Player]))
			.filter(player => !proj.hasPermission(player.getPlayerID, MFFSPermissions.bypassConfiscation))
			.foreach(
		    player => {
			    val filterItems = proj.getFilterItems
			    val inventory = player.getInventory

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
					    proj.get(classOf[Inventory]).add(opItem.get())
					    inventory.remove(i, opItem.get().count())
				    }

				    if (relevantSlots.size > 0) {
					    Game.network.sendChat(player, Game.language.translate("message.moduleConfiscate.confiscate"))
				    }
			    })
		    })
		return false
	}

	override def getID: String = "moduleConfiscate"
}