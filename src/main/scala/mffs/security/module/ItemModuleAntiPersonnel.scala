package mffs.security.module

import mffs.{ModularForceFieldSystem, Settings}
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ChatComponentText
import resonant.lib.utility.LanguageUtility

class ItemModuleAntiPersonnel extends ItemModuleDefense
{
  override def onDefend(interdictionMatrix: IInterdictionMatrix, entityLiving: EntityLivingBase): Boolean =
  {
    val hasPermission: Boolean = false

    if (!hasPermission && entityLiving.isInstanceOf[EntityPlayer])
    {
      val player: EntityPlayer = entityLiving.asInstanceOf[EntityPlayer]

      if (!player.capabilities.isCreativeMode && !player.isEntityInvulnerable)
      {
        var i: Int = 0
        while (i < player.inventory.getSizeInventory)
        {
          if (player.inventory.getStackInSlot(i) != null)
          {
            interdictionMatrix.mergeIntoInventory(player.inventory.getStackInSlot(i))
            player.inventory.setInventorySlotContents(i, null)
          }

          i += 1
        }

        player.setHealth(1)
        player.attackEntityFrom(ModularForceFieldSystem.damageFieldShock, 100)
        interdictionMatrix.requestFortron(Settings.interdictionMatrixMurderEnergy, false)
        player.addChatMessage(new ChatComponentText("[" + interdictionMatrix.getInventoryName + "] " + LanguageUtility.getLocal("message.moduleAntiPersonnel.death")))
      }
    }

    return false
  }

}