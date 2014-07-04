package mffs.base

import com.google.common.io.ByteArrayDataInput
import net.minecraft.nbt.NBTTagCompound
import resonant.api.blocks.IBlockFrequency
import resonant.api.mffs.IBiometricIdentifierLink
import resonant.api.mffs.card.ICoordLink
import resonant.api.mffs.fortron.FrequencyGridRegistry
import resonant.api.mffs.security.IBiometricIdentifier

abstract class TileFrequency extends TileMFFSInventory with IBlockFrequency with IBiometricIdentifierLink
{
  private var frequency = 0

  override def validate()
  {
    FrequencyGrid.instance.register(this)
    super.validate()
  }

  override def invalidate()
  {
    FrequencyGrid.instance.unregister(this)
    super.invalidate()
  }

  override def onReceivePacket(packetID: Int, dataStream: ByteArrayDataInput)
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
   * Gets the first linked biometric identifier, based on the card slots and frequency.
   */
  def getBiometricIdentifier: IBiometricIdentifier = if (getBiometricIdentifiers.size > 0) getBiometricIdentifiers.head else null

  def getBiometricIdentifiers: Set[IBiometricIdentifier] =
  {
    val cardLinks = getCards.view
            .filter(itemStack => itemStack != null && itemStack.getItem.isInstanceOf[ICoordLink])
            .map(_.getItem.asInstanceOf[ICoordLink].getLink(itemStack))
            .filter(_ != null)
            .map(_.getTileEntity)
            .filter(_.isInstanceOf[IBiometricIdentifier])
            .force

    val frequencyLinks = FrequencyGridRegistry.instance.getNodes(classOf[IBiometricIdentifier], getFrequency)

    return cardLinks ::: frequencyLinks
  }
}