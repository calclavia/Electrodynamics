package mffs.security

import java.util.{HashSet, Set}

import com.google.common.io.ByteArrayDataInput
import mffs.base.TileFrequency
import mffs.item.card.ItemCardFrequency
import mffs.security.access.MFFSPermissions
import mffs.{ModularForceFieldSystem, Settings}
import net.minecraft.item.ItemStack
import resonant.api.mffs.card.ICardIdentification
import resonant.api.mffs.security.IBiometricIdentifier

class TileBiometricIdentifier extends TileFrequency with IBiometricIdentifier
{
  val SLOT_COPY = 12

  def isAccessGranted(username: String, permission: MFFSPermissions): Boolean =
  {
    if (!isActive)
    {
      return true
    }
    if (ModularForceFieldSystem.proxy.isOp(username) && Settings.OP_OVERRIDE)
    {
      return true
    }
    {
      var i: Int = 0

      while (i < this.getSizeInventory())
      {
        val itemStack: ItemStack = this.getStackInSlot(i)
        if (itemStack != null && itemStack.getItem.isInstanceOf[ICardIdentification])
        {
          if (username.equalsIgnoreCase((itemStack.getItem.asInstanceOf[ICardIdentification]).getUsername(itemStack)))
          {
            if ((itemStack.getItem.asInstanceOf[ICardIdentification]).hasPermission(itemStack, permission))
            {
              return true
            }
          }
        }

        i += 1
      }
    }
    return username.equalsIgnoreCase(this.getOwner)
  }

  override def onReceivePacket(packetID: Int, dataStream: ByteArrayDataInput)
  {
    super.onReceivePacket(packetID, dataStream)
    if (packetID == TilePacketType.TOGGLE_MODE.ordinal)
    {
      if (this.getManipulatingCard != null)
      {
        val idCard: ICardIdentification = this.getManipulatingCard.getItem.asInstanceOf[ICardIdentification]
        val id: Int = dataStream.readInt
        val permission: MFFSPermissions = MFFSPermissions.getPermission(id)
        if (permission != null)
        {
          if (!idCard.hasPermission(this.getManipulatingCard, permission))
          {
            idCard.addPermission(this.getManipulatingCard, permission)
          }
          else
          {
            idCard.removePermission(this.getManipulatingCard, permission)
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
      if (this.getManipulatingCard != null)
      {
        val idCard: ICardIdentification = this.getManipulatingCard.getItem.asInstanceOf[ICardIdentification]
        idCard.setUsername(this.getManipulatingCard, dataStream.readUTF)
      }
    }
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
    if (this.getManipulatingCard != null && this.getStackInSlot(SLOT_COPY) != null && this.getStackInSlot(SLOT_COPY).getItem.isInstanceOf[ICardIdentification])
    {
      val masterCard: ICardIdentification = (this.getManipulatingCard.getItem.asInstanceOf[ICardIdentification])
      val copyCard: ICardIdentification = (this.getStackInSlot(SLOT_COPY).getItem.asInstanceOf[ICardIdentification])
      for (permission <- MFFSPermissions.getPermissions)
      {
        if (masterCard.hasPermission(this.getManipulatingCard, permission))
        {
          copyCard.addPermission(this.getStackInSlot(SLOT_COPY), permission)
        }
        else
        {
          copyCard.removePermission(this.getStackInSlot(SLOT_COPY), permission)
        }
      }
    }
  }

  override def getSizeInventory: Int =
  {
    return 13
  }

  override def getInventoryStackLimit: Int =
  {
    return 1
  }

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

  override def setActive(flag: Boolean)
  {
    if (this.getOwner != null || !flag)
    {
      super.setActive(flag)
    }
  }

  override def getBiometricIdentifiers: Set[IBiometricIdentifier] =
  {
    val set: Set[IBiometricIdentifier] = new HashSet[IBiometricIdentifier]
    set.add(this)
    return set
  }
}