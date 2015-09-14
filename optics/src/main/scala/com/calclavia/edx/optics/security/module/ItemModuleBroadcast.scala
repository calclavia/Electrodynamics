package com.calclavia.edx.optics.security.module

import java.util

import com.calclavia.edx.core.EDX
import com.calclavia.edx.optics.api.machine.Projector
import com.calclavia.edx.optics.field.BlockProjector
import com.calclavia.edx.optics.security.MFFSPermissions
import nova.core.entity.component.Player
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D

class ItemModuleBroadcast extends ItemModuleDefense {
	override def onCreateField(projector: Projector, field: util.Set[Vector3D]): Boolean = {
		val proj = projector.asInstanceOf[BlockProjector]
		val entities = getEntitiesInField(projector)

		//TODO: Add custom broadcast messages
		entities.view
			.filter(_.components.has(classOf[Player]))
			.map(_.components.get(classOf[Player]))
			.filter(p => !projector.hasPermission(p.getID, MFFSPermissions.defense))
			.foreach(EDX.network.sendChat(_, EDX.language.translate("message.moduleWarn.warn")))
		return false
	}
}