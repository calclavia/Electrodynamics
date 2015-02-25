package mffs.field.module

import java.util.{Set => JSet}

import mffs.api.machine.Projector
import mffs.base.ItemModule
import mffs.field.BlockProjector
import mffs.security.MFFSPermissions
import nova.core.util.transform.Vector3i

import scala.collection.convert.wrapAll._

/**
 * Generates a repulsion field instead of a solid one made out of blocks.
 * Entities are repelled from entering the force field.
 * @author Calclavia 
 */
class ItemModuleRepulsion extends ItemModule {
	setCost(8)

	override def getID: String = "moduleRepulsion"

	override def onCreateField(projector: Projector, field: JSet[Vector3i]): Boolean = {
		val repulsionVelocity = Math.max(projector.getSidedModuleCount(this) / 20, 1.2)

		getEntitiesInField(projector).par
			.filter(
				entity => {
					if (fields.contains(new Vector3d(entity).floor) || projector.getMode.isInField(projector, new Vector3d(entity))) {
						if (entity.isInstanceOf[EntityPlayer]) {
							val entityPlayer = entity.asInstanceOf[EntityPlayer]
							return entityPlayer.capabilities.isCreativeMode || projector.hasPermission(entityPlayer.getGameProfile, MFFSPermissions.forceFieldWarp)
						}
						return true
					}

					return false
				})
			.foreach(
				entity => {
					val repelDirection = new Vector3d(entity) - ((new Vector3d(entity).floor + 0.5).normalize)
					entity.motionX = repelDirection.x * Math.max(repulsionVelocity, Math.abs(entity.motionX))
					entity.motionY = repelDirection.y * Math.max(repulsionVelocity, Math.abs(entity.motionY))
					entity.motionZ = repelDirection.z * Math.max(repulsionVelocity, Math.abs(entity.motionZ))
					//TODO: May NOT be thread safe!
					entity.moveEntity(entity.motionX, entity.motionY, entity.motionZ)
					entity.onGround = true

					if (entity.isInstanceOf[EntityPlayerMP]) {
						entity.asInstanceOf[EntityPlayerMP].playerNetServerHandler.setPlayerLocation(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch)
					}
				})
		return true
	}

	override def onDestroy(projector: IProjector, field: JSet[Vector3d]): Boolean = {
		projector.asInstanceOf[BlockProjector].sendFieldToClient
		return false
	}

	override def requireTicks(moduleStack: Item): Boolean = true
}