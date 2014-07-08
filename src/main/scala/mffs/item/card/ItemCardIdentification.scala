package mffs.item.card

import java.util.List

import com.mojang.authlib.GameProfile
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import resonant.api.mffs.card.ICardIdentification
import resonant.lib.utility.LanguageUtility
import resonant.lib.utility.nbt.NBTUtility
import resonant.lib.wrapper.WrapList._

import scala.collection.convert.wrapAll._

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
    if (this.getProfile(itemStack) != null)
    {
      info.add(LanguageUtility.getLocal("info.cardIdentification.username") + " " + this.getProfile(itemStack))
    }
    else
    {
      info.add(LanguageUtility.getLocal("info.cardIdentification.empty"))
    }

    val permString = LanguageUtility.getLocal("permission." + getPermissions(itemStack).map(_.toString).mkString(","))
    info.addAll(LanguageUtility.splitStringPerWord(permString, 5))
  }

  override def onItemRightClick(itemStack: ItemStack, par2World: World, entityPlayer: EntityPlayer): ItemStack =
  {
    setProfile(itemStack, entityPlayer.getGameProfile)
    return itemStack
  }

  def setProfile(itemStack: ItemStack, profile: GameProfile) = NBTUtility.saveProfile(NBTUtility.getNBTTagCompound(itemStack), profile)

  def getProfile(itemStack: ItemStack): GameProfile = NBTUtility.loadProfile(NBTUtility.getNBTTagCompound(itemStack))
}