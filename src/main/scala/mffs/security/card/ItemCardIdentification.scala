package mffs.security.card

import java.util.List

import io.netty.buffer.ByteBuf
import mffs.ModularForceFieldSystem
import mffs.item.gui.EnumGui
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import resonant.lib.access.scala.AccessUser
import resonant.lib.network.handle.TPacketReceiver
import resonant.lib.utility.LanguageUtility
import resonant.lib.utility.nbt.NBTUtility
import resonant.lib.wrapper.WrapList._

class ItemCardIdentification extends ItemCardAccess with TPacketReceiver
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
    val access = getAccess(itemStack)

    if (access != null)
    {
      info.add(LanguageUtility.getLocal("info.cardIdentification.username") + " " + access.username)
      val permString = LanguageUtility.getLocal("permission." + access.permissions.map(_.toString).mkString(","))
      info.addAll(LanguageUtility.splitStringPerWord(permString, 5))
    }
    else
    {
      info.add(LanguageUtility.getLocal("info.cardIdentification.empty"))
    }

  }

  override def onItemRightClick(itemStack: ItemStack, world: World, player: EntityPlayer): ItemStack =
  {
    if (!world.isRemote)
    {
      if (player.isSneaking)
      {
        var access = getAccess(itemStack)

        if (access != null)
          access.username = player.getGameProfile.getName
        else
          access = new AccessUser(player.getGameProfile.getName)

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

  override def read(buf: ByteBuf, player: EntityPlayer, extra: AnyRef*)
  {
    val itemStack = extra(0).asInstanceOf[ItemStack]
    var access = getAccess(itemStack)

    if(access != null)
    {
      access.username = player.getGameProfile.getName
    }
    else
    {
      access = new AccessUser(player)
    }
  }
}