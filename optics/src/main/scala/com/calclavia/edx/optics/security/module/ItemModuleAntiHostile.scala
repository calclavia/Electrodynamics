package com.calclavia.edx.optics.security.module

import java.util

import com.calclavia.edx.optics.api.machine.Projector
import nova.core.component.misc.Damageable
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D

class ItemModuleAntiHostile extends ItemModuleDefense {

	override def onCreateField(projector: Projector, field: util.Set[Vector3D]): Boolean = {
		val entities = getEntitiesInField(projector)

		//Check entity IDs.
		entities.view
			.filter(entity => entity.has(classOf[Damageable]) /* && entity.isInstanceOf[IMob] && !entity.isInstanceOf[INpc]*/)
			.map(_.get(classOf[Damageable]))
			.foreach(_.damage(20))

		return false
	}

	override def getID: String = "moduleAntiHostile"
}