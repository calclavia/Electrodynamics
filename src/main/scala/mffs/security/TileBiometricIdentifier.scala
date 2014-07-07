package mffs.security

import java.util
import java.util.{Set => JSet}

import com.mojang.authlib.GameProfile
import mffs.base.TileFrequency
import mffs.security.access.AccessProfile
import mffs.{ModularForceFieldSystem, Settings}
import net.minecraft.item.ItemStack
import resonant.api.mffs.card.ICardIdentification
import resonant.api.mffs.security.IBiometricIdentifier
import resonant.lib.access.Permission
import scala.collection.JavaConversions._
import scala.collection.mutable

object TileBiometricIdentifier
{
  val SLOT_COPY = 12
}

class TileBiometricIdentifier extends TileFrequency with IBiometricIdentifier
{
  var accessProfile = new AccessProfile()

  /**
   * 2 slots: Card copying
   * 9 x 4 slots: Access Cards
   * Under access cards we have a permission selector
   */
  maxSlots = 2 + 9 * 4

  override def hasPermission(profile: GameProfile, permission: Permission): Boolean =
  {
    if (!isActive || ModularForceFieldSystem.proxy.isOp(profile) && Settings.allowOpOverride)
      return true

    return accessProfile.hasPermission(profile, permission)
  }

  /*
   override def onReceivePacket(packetID: Int, dataStream: ByteArrayDataInput)
   {
     super.onReceivePacket(packetID, dataStream)

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
         idCard.setProfile(this.getEditCard, dataStream.readUTF)
       }
     }
  }*/

  override def isItemValidForSlot(slotID: Int, itemStack: ItemStack): Boolean =
  {
    return itemStack.getItem.isInstanceOf[ICardIdentification]
  }

  override def markDirty()
  {
    rebuildAccess()
    super.markDirty()

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

  def rebuildAccess()
  {
    accessProfile = new AccessProfile()
    //TODO: Rebuild the access based on the cards.
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

  override def getBiometricIdentifiers: JSet[IBiometricIdentifier] =
  {
    //TODO: Fix this
    val set = new util.HashSet[IBiometricIdentifier] ()
    set.add(this)
    return set
  }
}