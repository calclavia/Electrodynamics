package mffs.security.module

import java.util.Set

import mffs.security.access.MFFSPermissions
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.util.ChatComponentText
import resonant.api.mffs.security.{IBiometricIdentifier, IInterdictionMatrix}
import resonant.lib.utility.LanguageUtility

import scala.collection.JavaConversions._

class ItemModuleConfiscate extends ItemModuleDefense
{
  override def onDefend(interdictionMatrix: IInterdictionMatrix, entityLiving: EntityLivingBase): Boolean =
  {
    if (entityLiving.isInstanceOf[EntityPlayer])
    {
      val player: EntityPlayer = entityLiving.asInstanceOf[EntityPlayer]
      val biometricIdentifier: IBiometricIdentifier = interdictionMatrix.getBiometricIdentifier
      if (biometricIdentifier != null && biometricIdentifier.hasPermission(player.getGameProfile, MFFSPermissions.bypassConfiscation))
      {
        return false
      }
    }
    val controlledStacks: Set[ItemStack] = interdictionMatrix.getFilteredItems
    var confiscationCount: Int = 0
    var inventory: IInventory = null

    if (entityLiving.isInstanceOf[EntityPlayer])
    {
      val biometricIdentifier: IBiometricIdentifier = interdictionMatrix.getBiometricIdentifier
      if (biometricIdentifier != null && biometricIdentifier.hasPermission((entityLiving.asInstanceOf[EntityPlayer]).getGameProfile, MFFSPermissions.defense))
      {
        return false
      }
      val player: EntityPlayer = entityLiving.asInstanceOf[EntityPlayer]
      inventory = player.inventory
    }
    else if (entityLiving.isInstanceOf[IInventory])
    {
      inventory = entityLiving.asInstanceOf[IInventory]
    }

    if (inventory != null)
    {
      var i: Int = 0

      while (i < inventory.getSizeInventory)
      {
        val checkStack: ItemStack = inventory.getStackInSlot(i)
        if (checkStack != null)
        {
          val foundItemMatch = controlledStacks filter (_ != null) exists (_.isItemEqual(checkStack))

          if ((interdictionMatrix.getFilterMode && foundItemMatch) || (!interdictionMatrix.getFilterMode && !foundItemMatch))
          {
            interdictionMatrix.mergeIntoInventory(inventory.getStackInSlot(i))
            inventory.setInventorySlotContents(i, null)
            confiscationCount += 1
          }
        }

        i += 1
      }

      if (confiscationCount > 0 && entityLiving.isInstanceOf[EntityPlayer])
      {
        (entityLiving.asInstanceOf[EntityPlayer]).addChatMessage(new ChatComponentText("[" + interdictionMatrix.getInventoryName + "] " + LanguageUtility.getLocal("message.moduleConfiscate.confiscate").replaceAll("%p", "" + confiscationCount)))
      }

      interdictionMatrix.requestFortron(confiscationCount, true)
    }
    return false
  }
}