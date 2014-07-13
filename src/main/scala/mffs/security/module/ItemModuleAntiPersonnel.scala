package mffs.security.module

import java.util.Set

import mffs.field.TileElectromagneticProjector
import mffs.{ModularForceFieldSystem, Settings}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ChatComponentText
import resonant.api.mffs.machine.IProjector
import resonant.lib.utility.LanguageUtility
import universalelectricity.core.transform.vector.Vector3

class ItemModuleAntiPersonnel extends ItemModuleDefense
{
  override def onProject(projector: IProjector, fields: Set[Vector3]): Boolean =
  {
    val proj = projector.asInstanceOf[TileElectromagneticProjector]
    val entities = getEntitiesInField(projector)

    entities.view
      .filter(entity => entity.isInstanceOf[EntityPlayer])
      .map(_.asInstanceOf[EntityPlayer])
      .filter(player => !player.capabilities.isCreativeMode && !player.isEntityInvulnerable)
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
          player.addChatMessage(new ChatComponentText("[" + proj.getInventoryName + "] " + LanguageUtility.getLocal("message.moduleAntiPersonnel.death")))
        }
      )

    return false
  }

}