package mffs.field.module

import java.util.{Set => JSet}

import mffs.base.ItemModule
import mffs.field.TileElectromagneticProjector
import mffs.security.access.MFFSPermissions
import net.minecraft.entity.Entity
import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP}
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import resonant.api.mffs.IProjector
import universalelectricity.core.transform.region.Cuboid
import universalelectricity.core.transform.vector.Vector3

import scala.collection.JavaConversions._

class ItemModuleRepulsion extends ItemModule
{
  setCost(8)

  override def onProject(projector: IProjector, fields: JSet[Vector3]): Boolean =
  {
    val tile = projector.asInstanceOf[TileEntity]
    val repulsionVelocity = Math.max(projector.getModuleCount(this) / 20, 1.2)
    val volume = new Cuboid(-projector.getNegativeScale, projector.getPositiveScale + 1) + (new Vector3(tile) + projector.getTranslation)
    val entities = tile.getWorldObj.getEntitiesWithinAABB(classOf[Entity], volume.toAABB) map (_.asInstanceOf[Entity])

    //TODO: Check parallel
    entities.par
            .filter(
              entity =>
              {
                if (fields.contains(new Vector3(entity).floor) || projector.getMode.isInField(projector, new Vector3(entity)))
                {
                  if (entity.isInstanceOf[EntityPlayer])
                  {
                    val entityPlayer = entity.asInstanceOf[EntityPlayer]
                    return entityPlayer.capabilities.isCreativeMode || projector.hasPermission(entityPlayer.getGameProfile, MFFSPermissions.forceFieldWrap)
                  }
                  return true
                }

                return false
              })
            .foreach(
              entity =>
              {
                val repelDirection = new Vector3(entity) - ((new Vector3(entity).floor + 0.5).normalize)
                entity.motionX = repelDirection.x * Math.max(repulsionVelocity, Math.abs(entity.motionX))
                entity.motionY = repelDirection.y * Math.max(repulsionVelocity, Math.abs(entity.motionY))
                entity.motionZ = repelDirection.z * Math.max(repulsionVelocity, Math.abs(entity.motionZ))
                //TODO: May NOT be thread safe!
                entity.moveEntity(entity.motionX, entity.motionY, entity.motionZ)
                entity.onGround = true

                if (entity.isInstanceOf[EntityPlayerMP])
                {
                  entity.asInstanceOf[EntityPlayerMP].playerNetServerHandler.setPlayerLocation(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch)
                }
              })

    return true
  }

  override def onDestroy(projector: IProjector, field: JSet[Vector3]): Boolean =
  {
    projector.asInstanceOf[TileElectromagneticProjector].sendFieldToClient
    return false
  }

  override def requireTicks(moduleStack: ItemStack): Boolean =
  {
    return true
  }
}