package mffs.production

import io.netty.buffer.ByteBuf
import mffs.ModularForceFieldSystem
import mffs.base.{TileModuleAcceptor, TilePacketType}
import mffs.util.TransferMode.TransferMode
import mffs.util.{FortronUtility, TransferMode}
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import resonant.api.mffs.card.{ICard, ICardInfinite, ICoordLink}
import resonant.api.mffs.fortron.{FrequencyGridRegistry, IFortronCapacitor, IFortronFrequency, IFortronStorage}
import resonant.api.mffs.modules.IModule
import universalelectricity.core.transform.vector.Vector3

import scala.collection.JavaConversions._
import scala.collection.mutable
import java.util.{Set => JSet}

class TileFortronCapacitor extends TileModuleAcceptor with IFortronStorage with IFortronCapacitor
{
  private var transferMode = TransferMode.EQUALIZE
  private val tickRate = 10

  capacityBase = 700
  capacityBoost = 10
  startModuleIndex = 2

  override def update()
  {
    super.update()
    this.consumeCost()

    if (this.isActive && this.ticks % tickRate == 0)
    {
      var machines = mutable.Set.empty[IFortronFrequency]

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
            if (linkPosition != null && linkPosition.getTileEntity(world).isInstanceOf[IFortronFrequency])
            {
              machines.add(this)
              machines.add(linkPosition.getTileEntity(world).asInstanceOf[IFortronFrequency])
            }
          }
        }
      }
      if (machines.size < 1)
      {
        machines = getLinkedDevices
      }

      FortronUtility.transferFortron(this, machines, this.transferMode, getTransmissionRate * tickRate)
    }
  }

  override def getAmplifier: Float =  0.001f

  /**
   * Packet Methods
   */
  override def getPacketData(packetID: Int): List[AnyRef] =
  {
    if (packetID == TilePacketType.DESCRIPTION.id)
    {
      return super.getPacketData(packetID) :+ (transferMode.id: Integer)
    }

    return super.getPacketData(packetID)
  }

  override def onReceivePacket(packetID: Int, dataStream: ByteBuf)
  {
    super.onReceivePacket(packetID, dataStream)

    if (packetID == TilePacketType.DESCRIPTION.id)
    {
      transferMode = TransferMode(dataStream.readInt)
    }
    else if (packetID == TilePacketType.TOGGLE_MODE.id)
    {
      transferMode = this.transferMode.toggle
    }
  }

  override def getSizeInventory: Int =
  {
    return 5
  }

  override def readFromNBT(nbt: NBTTagCompound)
  {
    super.readFromNBT(nbt)
    this.transferMode = TransferMode(nbt.getInteger("transferMode"))
  }

  override def writeToNBT(nbttagcompound: NBTTagCompound)
  {
    super.writeToNBT(nbttagcompound)
    nbttagcompound.setInteger("transferMode", this.transferMode.id)
  }

 override def getLinkedDevices: JSet[IFortronFrequency] =
  {
    return FrequencyGridRegistry.instance.getNodes(classOf[IFortronFrequency], world, new Vector3(this), getTransmissionRange, getFrequency)
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

  override def getCards: Set[ItemStack] = Set[ItemStack](super.getCard, getStackInSlot(1))

  def getTransferMode: TransferMode = transferMode

  def getTransmissionRange: Int = 15 + getModuleCount(ModularForceFieldSystem.Items.moduleScale)

  def getTransmissionRate: Int = 250 + 50 * getModuleCount(ModularForceFieldSystem.Items.moduleSpeed)

}