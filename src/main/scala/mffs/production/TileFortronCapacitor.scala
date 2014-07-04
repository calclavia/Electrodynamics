package mffs.production

import java.util.{ArrayList, HashSet, Set}

import com.google.common.io.ByteArrayDataInput
import mffs.base.TileModuleAcceptor
import mffs.util.{MFFSUtility, TransferMode}
import mffs.ModularForceFieldSystem
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import resonant.api.blocks.IBlockFrequency
import resonant.api.mffs.card.{ICardInfinite, ICard, ICoordLink}
import resonant.api.mffs.fortron.{IFortronCapacitor, IFortronFrequency, IFortronStorage}
import resonant.api.mffs.modules.IModule
import universalelectricity.core.transform.vector.Vector3

import scala.collection.JavaConversions._

class TileFortronCapacitor extends TileModuleAcceptor with IFortronStorage with IFortronCapacitor
{
  private var transferMode: TransferMode = TransferMode.EQUALIZE

  capacityBase = 700
  capacityBoost = 10
  startModuleIndex = 2

  override def update()
  {
    super.update()
    this.consumeCost()

    if (this.isActive && this.ticks % 10 == 0)
    {
      var machines: Set[IFortronFrequency] = new HashSet[IFortronFrequency]

      for (itemStack <- this.getCards)
      {
        if (itemStack != null)
        {
          if (itemStack.getItem.isInstanceOf[ICardInfinite])
          {
            this.setFortronEnergy(this.getFortronCapacity)
          }
          else if (itemStack.getItem.isInstanceOf[ICoordLink])
          {
            val linkPosition: Vector3 = (itemStack.getItem.asInstanceOf[ICoordLink]).getLink(itemStack)
            if (linkPosition != null && linkPosition.getTileEntity(this.worldObj).isInstanceOf[IFortronFrequency])
            {
              machines.add(this)
              machines.add(linkPosition.getTileEntity(this.worldObj).asInstanceOf[IFortronFrequency])
            }
          }
        }
      }
      if (machines.size < 1)
      {
        machines = this.getLinkedDevices
      }
      MFFSUtility.transferFortron(this, machines, this.transferMode, getTransmissionRate * 10)
    }
  }

  override def getAmplifier: Float =
  {
    return 0.001f
  }

  /**
   * Packet Methods
   */
  override def getPacketData(packetID: Int): ArrayList[_] =
  {
    val data: ArrayList[_] = super.getPacketData(packetID)
    if (packetID == TilePacketType.DESCRIPTION.ordinal)
    {
      data.add(this.transferMode.ordinal)
    }
    return data
  }

  override def onReceivePacket(packetID: Int, dataStream: ByteArrayDataInput)
  {
    super.onReceivePacket(packetID, dataStream)
    if (packetID == TilePacketType.DESCRIPTION.ordinal)
    {
      this.transferMode = TransferMode.values(dataStream.readInt)
    }
    else if (packetID == TilePacketType.TOGGLE_MODE.ordinal)
    {
      this.transferMode = this.transferMode.toggle
    }
  }

  override def getSizeInventory: Int =
  {
    return 5
  }

  override def readFromNBT(nbt: NBTTagCompound)
  {
    super.readFromNBT(nbt)
    this.transferMode = TransferMode.values(nbt.getInteger("transferMode"))
  }

  override def writeToNBT(nbttagcompound: NBTTagCompound)
  {
    super.writeToNBT(nbttagcompound)
    nbttagcompound.setInteger("transferMode", this.transferMode.ordinal)
  }

  def getLinkedDevices: Set[IFortronFrequency] =
  {
    val fortronBlocks: Set[IFortronFrequency] = new HashSet[IFortronFrequency]
    val frequencyBlocks: Set[IBlockFrequency] = FrequencyGrid.instance.get(this.worldObj, new Vector3(this), this.getTransmissionRange, this.getFrequency)
    import scala.collection.JavaConversions._
    for (frequencyBlock <- frequencyBlocks)
    {
      if (frequencyBlock.isInstanceOf[IFortronFrequency])
      {
        fortronBlocks.add(frequencyBlock.asInstanceOf[IFortronFrequency])
      }
    }
    return fortronBlocks
  }

  override def isItemValidForSlot(slotID: Int, itemStack: ItemStack): Boolean =
  {
    if (slotID == 0 || slotID == 1)
    {
      return itemStack.getItem.isInstanceOf[ICard]
    }
    else
    {
      return itemStack.getItem.isInstanceOf[IModule]
    }
  }

  override def getCards: Set[ItemStack] =
  {
    val cards: Set[ItemStack] = new HashSet[ItemStack]
    cards.add(super.getCard)
    cards.add(this.getStackInSlot(1))
    return cards
  }

  def getTransferMode: TransferMode =
  {
    return this.transferMode
  }

  def getTransmissionRange: Int =
  {
    return 15 + this.getModuleCount(ModularForceFieldSystem.itemModuleScale)
  }

  def getTransmissionRate: Int =
  {
    return 250 + 50 * this.getModuleCount(ModularForceFieldSystem.itemModuleSpeed)
  }

}