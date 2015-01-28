package mffs.security.module

import java.util.Set

import mffs.ModularForceFieldSystem
import mffs.field.TileElectromagneticProjector
import mffs.security.MFFSPermissions
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ChatComponentTranslation
import nova.core.util.transform.Vector3d
import resonantengine.api.mffs.machine.IProjector

class ItemModuleAntiPersonnel extends ItemModuleDefense
{
    override def onProject(projector: IProjector, fields: Set[Vector3d]): Boolean =
  {
    val proj = projector.asInstanceOf[TileElectromagneticProjector]
    val entities = getEntitiesInField(projector)

    entities.view
    .filter(entity => entity.isInstanceOf[EntityPlayer])
    .map(_.asInstanceOf[EntityPlayer])
    .filter(player => !player.capabilities.isCreativeMode && !player.isEntityInvulnerable)
    .filter(p => !projector.hasPermission(p.getGameProfile, MFFSPermissions.defense))
    .foreach(
        player =>
        {
          (0 until player.inventory.getSizeInventory)
          .filter(player.inventory.getStackInSlot(_) != null)
          .foreach(
              i =>
              {
                proj.mergeIntoInventory(player.inventory.getStackInSlot(i))
                player.inventory.setInventorySlotContents(i, null)
              }
            )

          player.attackEntityFrom(ModularForceFieldSystem.damageFieldShock, 1000)
          player.addChatMessage(new ChatComponentTranslation("message.moduleAntiPersonnel.death"))
        }
      )

    return false
  }

}