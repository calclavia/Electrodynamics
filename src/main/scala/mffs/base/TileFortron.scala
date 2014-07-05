package mffs.base

import com.google.common.io.ByteArrayDataInput
import cpw.mods.fml.common.network.ByteBufUtils
import io.netty.buffer.ByteBuf
import mffs.ModularForceFieldSystem
import mffs.util.{FortronUtility, TransferMode}
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.fluids._
import resonant.api.mffs.card.ICard
import resonant.api.mffs.fortron.{FrequencyGridRegistry, IFortronFrequency}
import resonant.lib.network.PacketTile
import universalelectricity.core.transform.vector.Vector3

import scala.collection.convert.wrapAll._
import scala.collection.immutable

/**
 * A TileEntity that is powered by FortronHelper.
 *
 * @author Calclavia
 */
abstract class TileFortron extends TileFrequency with IFluidHandler with IFortronFrequency
{
  var markSendFortron = true
  protected var fortronTank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME)

  override def update()
  {
    super.update()

    if (!worldObj.isRemote && ticks % 60 == 0)
    {
      sendFortronToClients(25)
    }
  }

  override def invalidate()
  {
    if (this.markSendFortron)
    {
      FortronUtility.transferFortron(this, FrequencyGridRegistry.instance.getNodes(classOf[IFortronFrequency], worldObj, new Vector3(this), 100, this.getFrequency), TransferMode.DRAIN, Integer.MAX_VALUE)
    }

    super.invalidate()
  }

  /**
   * Packets
   */
  override def getPacketData(packetID: Int): List[AnyRef] =
  {
    if (packetID == TilePacketType.FORTRON.id)
    {
      val data = List[AnyRef]()
      data.add(TilePacketType.FORTRON.id : Integer)

      val nbt = new NBTTagCompound

      if (fortronTank.getFluid != null)
      {
        nbt.setTag("fortron", fortronTank.getFluid.writeToNBT(new NBTTagCompound))
      }

      data.add(nbt)
      return data
    }

    return super.getPacketData(packetID)
  }

  def onReceivePacket(packetID: Int, dataStream: ByteBuf)
  {
    super.onReceivePacket(packetID, dataStream)

    if (packetID == TilePacketType.FORTRON.id)
    {
      val nbt = ByteBufUtils.readTag(dataStream)

      if (nbt != null)
      {
        fortronTank.setFluid(FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("fortron")))
      }
    }
  }

  def sendFortronToClients(range: Int)
  {
    ModularForceFieldSystem.packetHandler.sendToAllAround(new PacketTile(this, getPacketData(TilePacketType.FORTRON.id).toArray), this.worldObj, new Vector3(this), range)
  }

  /**
   * NBT Methods
   */
  override def readFromNBT(nbt: NBTTagCompound)
  {
    super.readFromNBT(nbt)
    fortronTank.setFluid(FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("fortron")))
  }

  override def writeToNBT(nbt: NBTTagCompound)
  {
    super.writeToNBT(nbt)

    if (fortronTank.getFluid != null)
    {
      nbt.setTag("fortron", this.fortronTank.getFluid.writeToNBT(new NBTTagCompound))
    }
  }

  /**
   * Fluid Functions.
   */
  override def fill(from: Nothing, resource: FluidStack, doFill: Boolean): Int =
  {
    if (resource.isFluidEqual(FortronUtility.FLUIDSTACK_FORTRON))
    {
      return this.fortronTank.fill(resource, doFill)
    }
    return 0
  }

  override def drain(from: Nothing, resource: FluidStack, doDrain: Boolean): FluidStack =
  {
    if (resource == null || !resource.isFluidEqual(fortronTank.getFluid))
    {
      return null
    }
    return fortronTank.drain(resource.amount, doDrain)
  }

  override def drain(from: Nothing, maxDrain: Int, doDrain: Boolean): FluidStack =
  {
    return fortronTank.drain(maxDrain, doDrain)
  }

  override def canFill(from: Nothing, fluid: Fluid): Boolean =
  {
    return true
  }

  override def canDrain(from: Nothing, fluid: Fluid): Boolean =
  {
    return true
  }

  override def getTankInfo(from: Nothing): Array[FluidTankInfo] =
  {
    return Array[FluidTankInfo](this.fortronTank.getInfo)
  }

  override def getFortronEnergy: Int =
  {
    return FortronUtility.getAmount(this.fortronTank)
  }

  override def setFortronEnergy(joules: Int)
  {
    this.fortronTank.setFluid(FortronUtility.getFortron(joules))
  }

  override def getFortronCapacity: Int =
  {
    return this.fortronTank.getCapacity
  }

  override def requestFortron(joules: Int, doUse: Boolean): Int =
  {
    return FortronUtility.getAmount(this.fortronTank.drain(joules, doUse))
  }

  override def provideFortron(joules: Int, doUse: Boolean): Int =
  {
    return this.fortronTank.fill(FortronUtility.getFortron(joules), doUse)
  }

  /**
   * Gets the card that's in this machine.
   *
   * @return
   */
  override def getCard: ItemStack =
  {
    val itemStack: ItemStack = this.getStackInSlot(0)
    if (itemStack != null)
    {
      if (itemStack.getItem.isInstanceOf[ICard])
      {
        return itemStack
      }
    }
    return null
  }
}