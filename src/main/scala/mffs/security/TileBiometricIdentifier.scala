package mffs.security

import com.google.common.io.ByteArrayDataInput
import com.mojang.authlib.GameProfile
import mffs.base.TileFrequency
import mffs.item.card.ItemCardFrequency
import mffs.security.access.AccessProfile
import mffs.{ModularForceFieldSystem, Settings}
import net.minecraft.item.ItemStack
import resonant.api.mffs.card.ICardIdentification
import resonant.api.mffs.security.IBiometricIdentifier
import resonant.lib.access.Permission

class TileBiometricIdentifier extends TileFrequency with IBiometricIdentifier
{
  val SLOT_COPY = 12
  var access = new AccessProfile()

  def isAccessGranted(profile: GameProfile, permission: Permission): Boolean =
  {
    if (!isActive || ModularForceFieldSystem.proxy.isOp(profile) && Settings.OP_OVERRIDE)
      return true

    return access.hasPermission(profile, permission)
  }

  override def onReceivePacket(packetID: Int, dataStream: ByteArrayDataInput)
  {
    super.onReceivePacket(packetID, dataStream)
    /*
    if (packetID == TilePacketType.TOGGLE_MODE.ordinal)
    {
      if (this.getEditCard != null)
      {
        val idCard: ICardIdentification = this.getEditCard.getItem.asInstanceOf[ICardIdentification]
        val id: Int = dataStream.readInt
        val permission = Permission.getPermission(id)
        if (permission != null)
        {
          if (!idCard.hasPermission(this.getEditCard, permission))
          {
            idCard.addPermission(this.getEditCard, permission)
          }
          else
          {
            idCard.removePermission(this.getEditCard, permission)
          }
        }
        else
        {
          ModularForceFieldSystem.LOGGER.severe("Error handling security station permission packet: " + id + " - " + permission)
        }
      }
    }
    else if (packetID == TilePacketType.STRING.ordinal)
    {
      if (this.getEditCard != null)
      {
        val idCard: ICardIdentification = this.getEditCard.getItem.asInstanceOf[ICardIdentification]
        idCard.setUsername(this.getEditCard, dataStream.readUTF)
      }
    }*/
  }

  override def isItemValidForSlot(slotID: Int, itemStack: ItemStack): Boolean =
  {
    if (slotID == 0)
    {
      return itemStack.getItem.isInstanceOf[ItemCardFrequency]
    }
    else
    {
      return itemStack.getItem.isInstanceOf[ICardIdentification]
    }
  }

  def getOwner: String =
  {
    val itemStack: ItemStack = this.getStackInSlot(2)
    if (itemStack != null)
    {
      if (itemStack.getItem.isInstanceOf[ICardIdentification])
      {
        return (itemStack.getItem.asInstanceOf[ICardIdentification]).getUsername(itemStack)
      }
    }
    return null
  }

  def onInventoryChanged
  {
    super.onInventoryChanged
    /*
    if (this.getEditCard != null && this.getStackInSlot(SLOT_COPY) != null && this.getStackInSlot(SLOT_COPY).getItem.isInstanceOf[ICardIdentification])
    {
      val masterCard: ICardIdentification = (this.getEditCard.getItem.asInstanceOf[ICardIdentification])
      val copyCard: ICardIdentification = (this.getStackInSlot(SLOT_COPY).getItem.asInstanceOf[ICardIdentification])
      for (permission <- MFFSPermissions.getPermissions)
      {
        if (masterCard.hasPermission(this.getEditCard, permission))
        {
          copyCard.addPermission(this.getStackInSlot(SLOT_COPY), permission)
        }
        else
        {
          copyCard.removePermission(this.getStackInSlot(SLOT_COPY), permission)
        }
      }
    }*/
  }

  override def getSizeInventory: Int =
  {
    return 13
  }

  override def getInventoryStackLimit: Int =
  {
    return 1
  }

  /**
   * Gets the current card that is being edited.
   **/
  def getManipulatingCard: ItemStack =
  {
    if (this.getStackInSlot(1) != null)
    {
      if (this.getStackInSlot(1).getItem.isInstanceOf[ICardIdentification])
      {
        return this.getStackInSlot(1)
      }
    }
    return null
  }

  /**
   *
   * @return
   */
  def getActiveCards: Set[ItemStack] =
  {
    return null;
  }

  override def setActive(flag: Boolean)
  {
    if (this.getOwner != null || !flag)
    {
      super.setActive(flag)
    }
  }

  override def getBiometricIdentifiers: Set[IBiometricIdentifier] = Set(this)
}