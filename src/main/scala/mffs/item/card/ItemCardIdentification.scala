package mffs.item.card

import java.util.List

import com.mojang.authlib.GameProfile
import mffs.security.access.MFFSPermissions
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import resonant.api.mffs.card.ICardIdentification
import resonant.lib.utility.LanguageUtility
import resonant.lib.utility.nbt.NBTUtility
import resonant.lib.wrapper.WrapList._

class ItemCardIdentification extends ItemCardPermission with ICardIdentification
{
  override def hitEntity(itemStack: ItemStack, entityLiving: EntityLivingBase, par3EntityLiving: EntityLivingBase): Boolean =
  {
    if (entityLiving.isInstanceOf[EntityPlayer])
    {
      this.setProfile(itemStack, entityLiving.asInstanceOf[EntityPlayer].getGameProfile)
    }
    return false
  }

  override def addInformation(itemStack: ItemStack, player: EntityPlayer, info: List[_], b: Boolean)
  {
    if (this.getProfile(itemStack) != null && !this.getProfile(itemStack).isEmpty)
    {
      info.add(LanguageUtility.getLocal("info.cardIdentification.username") + " " + this.getProfile(itemStack))
    }
    else
    {
      info.add(LanguageUtility.getLocal("info.cardIdentification.empty"))
    }

    var tooltip: String = ""
    var isFirst: Boolean = true
    for (permission <- MFFSPermissions.getPermissions)
    {
      if (this.hasPermission(itemStack, permission))
      {
        if (!isFirst)
        {
          tooltip += ", "
        }
        isFirst = false
        tooltip += LanguageUtility.getLocal("gui." + permission.name + ".name")
      }
    }
    if (tooltip != null && tooltip.length > 0)
    {
      info.addAll(LanguageUtility.splitStringPerWord(tooltip, 5))
    }
  }

  override def onItemRightClick(itemStack: ItemStack, par2World: World, entityPlayer: EntityPlayer): ItemStack =
  {
    this.setProfile(itemStack, entityPlayer.username)
    return itemStack
  }

  def setProfile(itemStack: ItemStack, profile: GameProfile)
  {
    NBTUtility.saveProfile(NBTUtility.getNBTTagCompound(itemStack), profile)
  }

  def getProfile(itemStack: ItemStack): GameProfile =
  {
    return NBTUtility.loadProfile(NBTUtility.getNBTTagCompound(itemStack))
  }

}