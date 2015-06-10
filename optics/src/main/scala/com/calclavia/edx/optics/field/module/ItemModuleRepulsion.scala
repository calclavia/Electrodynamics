package com.calclavia.edx.optics.field.module

import java.util.{Set => JSet}

import com.calclavia.edx.optics.api.machine.Projector
import com.calclavia.edx.optics.component.ItemModule
import com.calclavia.edx.optics.security.MFFSPermissions
import nova.core.entity.Entity
import nova.core.entity.component.{Player, RigidBody}
import nova.core.util.math.Vector3DUtil
import nova.scala.wrapper.VectorWrapper._
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D
/**
 * Generates a repulsion field instead of a solid one made out of blocks.
 * Entities are repelled from entering the force field.
 * @author Calclavia 
 */
class ItemModuleRepulsion extends ItemModule {
	setCost(8)

	override def getID: String = "moduleRepulsion"

	override def onCreateField(projector: Projector, field: JSet[Vector3D]): Boolean = {
		val repellForce = Vector3DUtil.ONE * Math.max(projector.getSidedModuleCount(factory()) / 20, 1.2)

		getEntitiesInField(projector).par
			.collect {
			case entity if entity.getOp(classOf[Player]).isPresent && projector.hasPermission(entity.get(classOf[Player]).getPlayerID, MFFSPermissions.forceFieldWarp) => entity
			case entity: Entity => entity
		}
			.foreach(
		    entity => {
			    val repelDirection = entity.transform.position - (entity.transform.position + 0.5).normalize
			    val rigidBody = entity.get(classOf[RigidBody])
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