package mffs.security.card

import java.util.List

import mffs.ModularForceFieldSystem
import mffs.item.gui.EnumGui

class ItemCardIdentification extends ItemCardAccess with IPacketReceiver
{
	override def hitEntity(Item: Item, entityLiving: EntityLivingBase, par3EntityLiving: EntityLivingBase): Boolean =
  {
    if (entityLiving.isInstanceOf[EntityPlayer])
    {
		val access = getAccess(Item)
      access.username = entityLiving.asInstanceOf[EntityPlayer].getGameProfile.getName
		setAccess(Item, access)
    }

    return false
  }

	override def addInformation(Item: Item, player: EntityPlayer, info: List[_], b: Boolean)
  {
	  val access = getAccess(Item)

    if (access != null)
    {
      info.add(LanguageUtility.getLocal("info.cardIdentification.username") + " " + access.username)
    }
    else
    {
      info.add(LanguageUtility.getLocal("info.cardIdentification.empty"))
    }

  }

	override def onItemRightClick(Item: Item, world: World, player: EntityPlayer): Item =
  {
    if (!world.isRemote)
    {
      if (player.isSneaking)
      {
		  var access = getAccess(Item)

		  if (access != null)
			  access.username = player.getGameProfile.getName
		  else {
			  access = new AccessUser(player.getGameProfile.getName)
		  }

		  setAccess(Item, access)
      }
      else
      {
        /**
         * Open item GUI
         */
        player.openGui(ModularForceFieldSystem, EnumGui.cardID.id, world, 0, 0, 0)
      }
    }

	  return Item
  }

  /**
   * Reads a packet
   * @param buf   - data encoded into the packet
   * @param player - player that is receiving the packet
   * @param packet - The packet instance that was sending this packet.
   */
  override def read(buf: ByteBuf, player: EntityPlayer, packet: PacketType)
  {
	  val Item = player.getCurrentEquippedItem
	  var access = getAccess(Item)

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

	  setAccess(Item, access)
  }

	override def getAccess(Item: Item): AccessUser =
  {
	  val nbt = NBTUtility.getNBTTagCompound(Item)

    if (nbt != null)
    {
      val user = new AccessUser(nbt)
      return user
    }

    return null
  }
}