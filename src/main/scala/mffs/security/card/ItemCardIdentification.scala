package mffs.security.card

import java.util.List

import mffs.ModularForceFieldSystem
import mffs.item.gui.EnumGui
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import resonant.lib.access.scala.AccessUser
import resonant.lib.utility.LanguageUtility
import resonant.lib.utility.nbt.NBTUtility
import resonant.lib.wrapper.WrapList._

class ItemCardIdentification extends ItemCardAccess
{
  override def hitEntity(itemStack: ItemStack, entityLiving: EntityLivingBase, par3EntityLiving: EntityLivingBase): Boolean =
  {
    if (entityLiving.isInstanceOf[EntityPlayer])
    {
      val access = getAccess(itemStack)
      access.username = entityLiving.asInstanceOf[EntityPlayer].getGameProfile.getName
      setAccess(itemStack, access)
    }

    return false
  }

  override def addInformation(itemStack: ItemStack, player: EntityPlayer, info: List[_], b: Boolean)
  {
    if (getUsername(itemStack) != null)
    {
      info.add(LanguageUtility.getLocal("info.cardIdentification.username") + " " + this.getUsername(itemStack))
    }
    else
    {
      info.add(LanguageUtility.getLocal("info.cardIdentification.empty"))
    }

    val permString = LanguageUtility.getLocal("permission." + getAccess(itemStack).permissions.map(_.toString).mkString(","))
    info.addAll(LanguageUtility.splitStringPerWord(permString, 5))
  }

  override def onItemRightClick(itemStack: ItemStack, world: World, player: EntityPlayer): ItemStack =
  {
    if (!world.isRemote)
    {
      if (player.isSneaking)
      {
        val access = getAccess(itemStack)
        access.username = player.getGameProfile.getName
        setAccess(itemStack, access)
      }
      else
      {
        /**
         * Open item GUI
         */
        player.openGui(ModularForceFieldSystem, EnumGui.cardID.id, world, 0, 0, 0)
      }
    }

    return itemStack
  }

  override def setAccess(itemStack: ItemStack, access: AccessUser) = super.setAccess(itemStack, access)

  override def getAccess(itemStack: ItemStack): AccessUser =
  {
    val nbt = NBTUtility.getNBTTagCompound(itemStack)

    if (nbt != null)
    {
      val user = new AccessUser(nbt)
      return user
    }

    return null
  }
}