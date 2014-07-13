package mffs.security.card

import java.util.List

import io.netty.buffer.ByteBuf
import mffs.ModularForceFieldSystem
import mffs.item.gui.EnumGui
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import resonant.lib.access.java.Permissions
import resonant.lib.access.scala.AccessUser
import resonant.lib.network.ByteBufWrapper.ByteBufWrapper
import resonant.lib.network.discriminator.PacketType
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

      val permString = LanguageUtility.getLocal(access.permissions.map(_.id).mkString(", "))
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

  /**
   * Reads a packet
   * @param buf   - data encoded into the packet
   * @param player - player that is receiving the packet
   * @param packet - The packet instance that was sending this packet.
   */
  override def read(buf: ByteBuf, player: EntityPlayer, packet: PacketType)
  {
    val itemStack = player.getCurrentEquippedItem
    var access = getAccess(itemStack)

    buf.readInt() match
    {
      case 0 =>
      {
        /**
         * Permission toggle packet
         */
        val perm = Permissions.find(buf.readString())

        if (access == null)
        {
          access = new AccessUser(player)
        }

        if (perm != null)
        {
          if (access.permissions.contains(perm))
            access.permissions -= perm
          else
            access.permissions += perm
        }
      }
      case 1 =>
      {
        /**
         * Username packet
         */
        if (access != null)
        {
          access.username = buf.readString()
        }
        else
        {
          access = new AccessUser(buf.readString())
        }
      }
    }

    setAccess(itemStack, access)
  }
}