package mffs.security.module

import java.util.Set

import mffs.field.TileElectromagneticProjector
import mffs.security.MFFSPermissions
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ChatComponentText
import resonant.api.mffs.machine.IProjector
import universalelectricity.core.transform.vector.Vector3

class ItemModuleConfiscate extends ItemModuleDefense
{
  override def onProject(projector: IProjector, fields: Set[Vector3]): Boolean =
  {
    val proj = projector.asInstanceOf[TileElectromagneticProjector]
    val entities = getEntitiesInField(projector)

    entities.view
      .filter(_.isInstanceOf[EntityPlayer])
      .map(_.asInstanceOf[EntityPlayer])
      .filter(player => !proj.hasPermission(player.getGameProfile, MFFSPermissions.bypassConfiscation))
      .foreach(
        player =>
        {
          val filterItems = proj.getFilterItems
          //TODO: Support inventory entities
          val inventory = player.inventory

          val relevantSlots = (0 until inventory.getSizeInventory)
            .filter(
              i =>
              {
                val checkStack = inventory.getStackInSlot(i)
                checkStack != null && proj.isInvertedFilter != (filterItems exists (_ == checkStack.getItem))
              }
            )

          relevantSlots foreach (i =>
          {
            proj.mergeIntoInventory(inventory.getStackInSlot(i))
            inventory.setInventorySlotContents(i, null)
          })

          if (relevantSlots.size > 0)
          {
            player.addChatMessage(new ChatComponentText("[" + proj.getInventoryName + "] " + relevantSlots.size + " of your item(s) has been confiscated."))
            // LanguageUtility.getLocal("message.moduleConfiscate.confiscate")
          }
        }
      )

    return false
  }
}