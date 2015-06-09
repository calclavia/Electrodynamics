package com.calclavia.edx.optics.security.module

import java.util

import com.calclavia.edx.optics.api.machine.Projector
import com.calclavia.edx.optics.field.BlockProjector
import com.calclavia.edx.optics.security.MFFSPermissions
import nova.core.component.misc.Damageable
import nova.core.entity.component.Player
import com.calclavia.edx.core.EDX
import nova.core.inventory.Inventory
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D

class ItemModuleAntiPersonnel extends ItemModuleDefense {

	override def onCreateField(projector: Projector, field: util.Set[Vector3D]): Boolean = {
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
				EDX.network.sendChat(player, EDX.language.translate("message.moduleAntiPersonnel.death"))
		    }
			)

		return false
	}

	override def getID: String = "moduleAntiPersonnel"

}