package com.calclavia.edx.mffs.field.module

import java.util.{Set => JSet}

import com.calclavia.edx.mffs.api.machine.Projector
import com.calclavia.edx.mffs.base.ItemModule
import com.calclavia.edx.mffs.security.MFFSPermissions
import nova.core.entity.Entity
import nova.core.entity.component.{Player, RigidBody}
import nova.core.util.transform.vector.{Vector3d, Vector3i}

/**
 * Generates a repulsion field instead of a solid one made out of blocks.
 * Entities are repelled from entering the force field.
 * @author Calclavia 
 */
class ItemModuleRepulsion extends ItemModule {
	setCost(8)

	override def getID: String = "moduleRepulsion"

	override def onCreateField(projector: Projector, field: JSet[Vector3i]): Boolean = {
		val repellForce = Vector3d.one * Math.max(projector.getSidedModuleCount(factory()) / 20, 1.2)

		getEntitiesInField(projector).par
			.collect {
			case player: Player if projector.hasPermission(player.getID, MFFSPermissions.forceFieldWarp) => player
			case entity: Entity => entity
		}
			.foreach(
		    entity => {
			    val repelDirection = entity.transform.position) -(entity.transform.position.toInt.toDouble + 0.5).normalize
			    val rigidBody = entity.get(classOf[RigidBody]).get
			    val velocity = rigidBody.velocity
			    val force = repelDirection * repellForce.max(velocity.abs)
			    rigidBody.addForce(force)
			    //TODO: May NOT be thread safe!
		    })
		return true
	}

	//TODO: Send field to client

	override def requireTicks(): Boolean = true
}