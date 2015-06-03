package com.calclavia.edx.mffs.security.module

import java.util

import com.calclavia.edx.mffs.api.machine.Projector
import com.calclavia.edx.mffs.field.BlockProjector
import com.calclavia.edx.mffs.security.MFFSPermissions
import nova.core.component.misc.Damageable
import nova.core.entity.component.Player
import nova.core.game.Game
import nova.core.inventory.Inventory
import nova.core.util.transform.vector.Vector3i

class ItemModuleAntiPersonnel extends ItemModuleDefense {

	override def onCreateField(projector: Projector, field: util.Set[Vector3i]): Boolean = {
		val proj = projector.asInstanceOf[BlockProjector]
		val entities = getEntitiesInField(projector)

		entities.view
			.collect { case entity if entity.has(classOf[Player]) && entity.has(classOf[Damageable]) => entity }
			.filter(p => !projector.hasPermission(p.getID, MFFSPermissions.defense))
			.foreach(
		    entity => {
			    val player = entity.get(classOf[Player])
			    (0 until player.getInventory.size())
				    .filter(player.getInventory.get(_) != null)
				    .foreach(
			        i => {
				        val stackInSlot = player.getInventory.get(i)
				        if (stackInSlot.isPresent) {
					        proj.get(classOf[Inventory]).add(stackInSlot.get)
					        player.getInventory.remove(i, stackInSlot.get().count)
				        }
			        }
				    )

			    entity.get(classOf[Damageable]).damage(1000)
			    Game.network.sendChat(player, Game.language.translate("message.moduleAntiPersonnel.death"))
		    }
			)

		return false
	}

	override def getID: String = "moduleAntiPersonnel"

}