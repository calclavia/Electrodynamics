package mffs.base

import com.mojang.authlib.GameProfile
import io.netty.buffer.ByteBuf
import mffs.Reference
import mffs.security.access.MFFSPermissions
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ChatComponentText
import resonant.api.blocks.IBlockFrequency
import resonant.api.mffs.IBiometricIdentifierLink
import resonant.api.mffs.card.ICoordLink
import resonant.api.mffs.fortron.FrequencyGridRegistry
import resonant.api.mffs.security.IBiometricIdentifier
import resonant.lib.access.Permission
import universalelectricity.core.transform.vector.Vector3
import scala.collection.convert.wrapAll._
abstract class TileFrequency extends TileMFFSInventory with IBlockFrequency with IBiometricIdentifierLink
{
  private var frequency = 0

  override def validate()
  {
    FrequencyGridRegistry.instance.add(this)
    super.validate()
  }

  override def invalidate()
  {
    FrequencyGridRegistry.instance.remove(this)
    super.invalidate()
  }

  def onReceivePacket(packetID: Int, dataStream: ByteBuf)
  {
    super.onReceivePacket(packetID, dataStream)

    if (packetID == TilePacketType.FREQUENCY.id)
    {
      setFrequency(dataStream.readInt)
    }
  }

  override def readFromNBT(nbt: NBTTagCompound)
  {
    super.readFromNBT(nbt)
    this.setFrequency(nbt.getInteger("frequency"))
  }

  override def writeToNBT(nbt: NBTTagCompound)
  {
    super.writeToNBT(nbt)
    nbt.setInteger("frequency", getFrequency)
  }

  override def getFrequency = frequency

  override def setFrequency(frequency: Int)
  {
    this.frequency = frequency
  }

  /**
   * Permissions
   */
  override protected def configure(player: EntityPlayer, side: Int, hit: Vector3): Boolean =
  {
    if (!isAccessGranted(player.getGameProfile, MFFSPermissions.configure))
    {
      player.addChatMessage(new ChatComponentText("[" + Reference.NAME + "]" + " Access denied!"))
      return false
    }

    return super.configure(player, side, hit)
  }

  //TODO: Check the "isActive" to see if it's really needed.
  def isAccessGranted(profile: GameProfile, permission: Permission): Boolean = isActive && getBiometricIdentifiers.forall(_.hasPermission(profile, permission))

  def isAccessGranted(profile: GameProfile, permissions: Permission*): Boolean = permissions.forall(isAccessGranted(profile, _))

  /**
   * Gets the first linked biometric identifier, based on the card slots and frequency.
   */
  def getBiometricIdentifier: IBiometricIdentifier = if (getBiometricIdentifiers.size > 0) getBiometricIdentifiers.head else null

  def getBiometricIdentifiers: Set[IBiometricIdentifier] =
  {
    val cardLinks = (getCards.view
            .filter(itemStack => itemStack != null && itemStack.getItem.isInstanceOf[ICoordLink])
            .map(itemStack => itemStack.getItem.asInstanceOf[ICoordLink].getLink(itemStack))
            .filter(_ != null)
            .map(_.getTileEntity)
            .filter(_.isInstanceOf[IBiometricIdentifier])
            .map(_.asInstanceOf[IBiometricIdentifier]))
            .force.toSet

    val frequencyLinks = FrequencyGridRegistry.instance.getNodes(classOf[IBiometricIdentifier], getFrequency).toSet

    return frequencyLinks ++ cardLinks
  }
}