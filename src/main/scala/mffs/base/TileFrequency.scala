package mffs.base

import java.util.{HashSet, Set}

import com.google.common.io.ByteArrayDataInput
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import resonant.api.blocks.IBlockFrequency
import resonant.api.mffs.IBiometricIdentifierLink
import resonant.api.mffs.card.ICoordLink
import resonant.api.mffs.security.IBiometricIdentifier
import universalelectricity.core.transform.vector.Vector3

import scala.collection.JavaConversions._

abstract class TileFrequency extends TileMFFSInventory with IBlockFrequency with IBiometricIdentifierLink
{
  private var frequency: Int = 0

  def initiate()
  {
    FrequencyGrid.instance.register(this)
    super.initiate()
  }

  override def invalidate()
  {
    FrequencyGrid.instance.unregister(this)
    super.invalidate()
  }

  override def onReceivePacket(packetID: Int, dataStream: ByteArrayDataInput)
  {
    super.onReceivePacket(packetID, dataStream)
    if (packetID == TilePacketType.FREQUENCY.ordinal)
    {
      this.setFrequency(dataStream.readInt)
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
    nbt.setInteger("frequency", this.getFrequency)
  }

  override def getFrequency: Int =
  {
    return this.frequency
  }

  override def setFrequency(frequency: Int)
  {
    this.frequency = frequency
  }

  /**
   * Gets the first linked security station, based on the card slots and frequency.
   *
   * @return
   */
  def getBiometricIdentifier: IBiometricIdentifier =
  {
    if (this.getBiometricIdentifiers.size > 0)
    {
      return this.getBiometricIdentifiers.toArray(0).asInstanceOf[IBiometricIdentifier]
    }
    return null
  }

  def getBiometricIdentifiers: Set[IBiometricIdentifier] =
  {
    val list: Set[IBiometricIdentifier] = new HashSet[IBiometricIdentifier]
    import scala.collection.JavaConversions._
    for (itemStack <- this.getCards)
    {
      if (itemStack != null && itemStack.getItem.isInstanceOf[ICoordLink])
      {
        val linkedPosition: Vector3 = (itemStack.getItem.asInstanceOf[ICoordLink]).getLink(itemStack)
        if (linkedPosition != null)
        {
          val tileEntity: TileEntity = linkedPosition.getTileEntity(this.worldObj)
          if (linkedPosition != null && tileEntity.isInstanceOf[IBiometricIdentifier])
          {
            list.add(tileEntity.asInstanceOf[IBiometricIdentifier])
          }
        }
      }
    }
    for (tileEntity <- FrequencyGrid.instance.get(this.getFrequency))
    {
      if (tileEntity.isInstanceOf[IBiometricIdentifier])
      {
        list.add(tileEntity.asInstanceOf[IBiometricIdentifier])
      }
    }
    return list
  }
}