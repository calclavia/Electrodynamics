package mffs.base

import com.google.common.io.ByteArrayDataInput
import mffs.fortron.{FortronHelper, TransferMode}
import mffs.{MFFSHelper, ModularForceFieldSystem}
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.fluids._
import resonant.api.mffs.card.ICard
import resonant.api.mffs.fortron.IFortronFrequency
import universalelectricity.core.transform.vector.Vector3

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
      MFFSHelper.transferFortron(this, FrequencyGrid.instance.getFortronTiles(this.worldObj, new Vector3(this), 100, this.getFrequency), TransferMode.DRAIN, Integer.MAX_VALUE)
    }

    super.invalidate()
  }

  /**
   * Packets
   */
  override def getPacketData(packetID: Int): List[_] =
  {
    if (packetID == TilePacketType.FORTRON.ordinal)
    {
      val nbt: NBTTagCompound = new NBTTagCompound
      if (this.fortronTank.getFluid != null)
      {
        nbt.setTag("fortron", this.fortronTank.getFluid.writeToNBT(new NBTTagCompound))
      }

      val list = List()
      list.add(TilePacketType.FORTRON.ordinal)
      list.add(nbt)
      return list
    }

    return super.getPacketData(packetID)
  }

  override def onReceivePacket(packetID: Int, dataStream: ByteArrayDataInput)
  {
    super.onReceivePacket(packetID, dataStream)
    if (packetID == TilePacketType.FORTRON.ordinal)
    {
      val nbt: NBTTagCompound = PacketHandler.readNBTTagCompound(dataStream)
      if (nbt != null)
      {
        this.fortronTank.setFluid(FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("fortron")))
      }
    }
  }

  def sendFortronToClients(range: Int)
  {
    PacketHandler.sendPacketToClients(ModularForceFieldSystem.PACKET_TILE.getPacket(this, this.getPacketData(TilePacketType.FORTRON.ordinal).toArray), this.worldObj, new Vector3(this), range)
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
    if (resource.isFluidEqual(FortronHelper.FLUIDSTACK_FORTRON))
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
    return FortronHelper.getAmount(this.fortronTank)
  }

  override def setFortronEnergy(joules: Int)
  {
    this.fortronTank.setFluid(FortronHelper.getFortron(joules))
  }

  override def getFortronCapacity: Int =
  {
    return this.fortronTank.getCapacity
  }

  override def requestFortron(joules: Int, doUse: Boolean): Int =
  {
    return FortronHelper.getAmount(this.fortronTank.drain(joules, doUse))
  }

  override def provideFortron(joules: Int, doUse: Boolean): Int =
  {
    return this.fortronTank.fill(FortronHelper.getFortron(joules), doUse)
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