package resonantinduction.core.grid.fluid

import io.netty.buffer.ByteBuf
import net.minecraft.block.material.Material
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.Packet
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids._
import resonant.content.prefab.java.TileAdvanced
import resonant.lib.network.ByteBufWrapper.ByteBufWrapper
import resonant.lib.network.discriminator.PacketType
import resonant.lib.network.handle.{IPacketIDReceiver, TPacketIDSender}
import resonant.lib.network.netty.PacketManager
import resonant.lib.utility.FluidUtility
import resonantinduction.core.ResonantInduction
import resonantinduction.core.grid.fluid.distribution.TankNode
import universalelectricity.api.core.grid.{INode, INodeProvider}
import universalelectricity.core.transform.vector.Vector3

/**
 * A prefab class for tiles that use the fluid network.
 *
 * @author DarkGuardsman
 */
object TileTankNode extends Enumeration
{
  final val PACKET_DESCRIPTION, PACKET_RENDER, PACKET_TANK = Value
}

class TileTankNode(material: Material) extends TileAdvanced(material) with INodeProvider with IFluidHandler with IPacketIDReceiver with TPacketIDSender
{
  protected var tank: FluidTank = new FluidTank(1000);
  protected var pressure = 0
  protected var colorID: Int = 0

  /**
   * Copy of the tank's content last time it updated
   */
  protected var prevStack: FluidStack = null

  /**
   * Bitmask that handles connections for the renderer
   */
  var renderSides: Byte = 0
  protected var markTankUpdate = false
  protected final val tankSize = 0

  var tankNode : TankNode = new TankNode(this)

  override def update()
  {
    super.update()

    if (markTankUpdate)
    {
      sendTankUpdate
      markTankUpdate = false
    }
  }

  def getTank() : FluidTank = tank

  def getFluid() : FluidStack = tank.getFluid

  def getFluidCapacity() : Int = tank.getCapacity

  override def readFromNBT(nbt: NBTTagCompound)
  {
    super.readFromNBT(nbt)
    colorID = nbt.getInteger("colorID")
    tank.readFromNBT(nbt.getCompoundTag("FluidTank"))
  }

  override def writeToNBT(nbt: NBTTagCompound)
  {
    super.writeToNBT(nbt)
    nbt.setInteger("colorID", colorID)
    nbt.setTag("FluidTank", tank.writeToNBT(new NBTTagCompound))
  }

  override def write(buf: ByteBuf, id: Int)
  {
    super.write(buf, id)

    if (id == TileTankNode.PACKET_DESCRIPTION.id)
    {
      buf <<< colorID
      buf <<< renderSides
      buf <<< tank

    }
    else if (id == TileTankNode.PACKET_RENDER.id)
    {
      buf <<< colorID
      buf <<< renderSides
    }
    else if (id == TileTankNode.PACKET_TANK.id)
    {
      buf <<< tank
      buf <<< pressure
    }
  }

  override def read(buf: ByteBuf, id: Int, player: EntityPlayer, packet: PacketType) : Boolean =
  {
    if (world.isRemote)
    {
      if (id == TileTankNode.PACKET_DESCRIPTION.id)
      {
        colorID = buf.readInt
        renderSides = buf.readByte
        tank = buf.readTank()
        return true
      }
      else if (id == TileTankNode.PACKET_RENDER.id)
      {
        colorID = buf.readInt
        renderSides = buf.readByte
        markRender
        return true
      }
      else if (id == TileTankNode.PACKET_TANK.id)
      {
        tank = buf.readTank()
        pressure = buf.readInt
        updateLight()
        return true
      }
    }
    return false
  }

  override def getDescriptionPacket: Packet = ResonantInduction.packetHandler.toMCPacket(PacketManager.request(this, TileTankNode.PACKET_DESCRIPTION.id))

  def sendRenderUpdate
  {
    if (!world.isRemote)
      ResonantInduction.packetHandler.sendToAll(PacketManager.request(this, TileTankNode.PACKET_RENDER.id))
  }

  def sendTankUpdate
  {
    if (!world.isRemote)
      ResonantInduction.packetHandler.sendToAllAround(PacketManager.request(this, TileTankNode.PACKET_TANK.id), world, new Vector3(this), 60)
  }

  def onFluidChanged()
  {
    if (!worldObj.isRemote)
    {
      if (!FluidUtility.matchExact(prevStack, tank.getFluid) || ticks == 0)
      {
        markTankUpdate = true
        prevStack = if (tank.getFluid != null) tank.getFluid.copy else null
      }
    }
  }

  override def getNode(nodeType: Class[_ <: INode], from: ForgeDirection): INode = if(nodeType.isInstanceOf[TankNode]) return tankNode else null

  override def drain(from: ForgeDirection, resource: FluidStack, doDrain: Boolean): FluidStack = tankNode.drain(from, resource, doDrain)

  override def drain(from: ForgeDirection, maxDrain: Int, doDrain: Boolean): FluidStack = tankNode.drain(from, maxDrain, doDrain)

  override def canFill(from: ForgeDirection, fluid: Fluid): Boolean = tankNode.canFill(from, fluid)

  override def canDrain(from: ForgeDirection, fluid: Fluid): Boolean = tankNode.canDrain(from, fluid)

  override def fill(from: ForgeDirection, resource: FluidStack, doFill: Boolean): Int = tankNode.fill(from, resource, doFill)

  override def getTankInfo(from: ForgeDirection): Array[FluidTankInfo] = tankNode.getTankInfo(from)


  override def initiate()
  {
    super.initiate()
    tankNode.reconstruct()
  }

  override def invalidate()
  {
    tankNode.deconstruct()
    super.invalidate()
  }
}