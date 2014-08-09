package resonantinduction.core.grid.fluid

import io.netty.buffer.ByteBuf
import net.minecraft.block.material.Material
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.Packet
import net.minecraftforge.fluids.{FluidStack, FluidTank, IFluidHandler}
import resonant.content.spatial.block.SpatialTile
import resonant.lib.network.ByteBufWrapper.ByteBufWrapper
import resonant.lib.network.discriminator.PacketType
import resonant.lib.network.handle.{IPacketIDReceiver, TPacketIDSender}
import resonant.lib.network.netty.PacketManager
import resonant.lib.utility.FluidUtility
import resonantinduction.core.ResonantInduction
import universalelectricity.api.core.grid.INodeProvider
import universalelectricity.core.transform.vector.Vector3

/**
 * A prefab class for tiles that use the fluid network.
 *
 * @author DarkGuardsman
 */
object TileFluidNodeProvider extends Enumeration
{
  final val PACKET_DESCRIPTION, PACKET_RENDER, PACKET_TANK = Value
}

abstract class TileFluidNodeProvider(material: Material) extends SpatialTile(material) with INodeProvider with IFluidHandler with IPacketIDReceiver with TPacketIDSender
{
  protected var tank: FluidTank
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

  override def update()
  {
    super.update()

    if (markTankUpdate)
    {
      sendTankUpdate
      markTankUpdate = false
    }
  }

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

    if (id == TileFluidNodeProvider.PACKET_DESCRIPTION.id)
    {
      buf <<< colorID
      buf <<< renderSides
      buf <<< tank

    }
    else if (id == TileFluidNodeProvider.PACKET_RENDER.id)
    {
      buf <<< colorID
      buf <<< renderSides
    }
    else if (id == TileFluidNodeProvider.PACKET_TANK.id)
    {
      buf <<< tank
      buf <<< pressure
    }
  }

  def read(buf: ByteBuf, id: Int, player: EntityPlayer, packet: PacketType)
  {
    if (world.isRemote)
    {
      if (id == TileFluidNodeProvider.PACKET_DESCRIPTION.id)
      {
        colorID = buf.readInt
        renderSides = buf.readByte
        tank = buf.readTank()
      }
      else if (id == TileFluidNodeProvider.PACKET_RENDER.id)
      {
        colorID = buf.readInt
        renderSides = buf.readByte
        markRender
      }
      else if (id == TileFluidNodeProvider.PACKET_TANK.id)
      {
        tank = buf.readTank()
        pressure = buf.readInt
        updateLight()
      }
    }
  }

  override def getDescriptionPacket: Packet = ResonantInduction.packetHandler.toMCPacket(PacketManager.request(this, TileFluidNodeProvider.PACKET_DESCRIPTION.id))

  def sendRenderUpdate
  {
    if (!world.isRemote)
      ResonantInduction.packetHandler.sendToAll(PacketManager.request(this, TileFluidNodeProvider.PACKET_RENDER.id))
  }

  def sendTankUpdate
  {
    if (!world.isRemote)
      ResonantInduction.packetHandler.sendToAllAround(PacketManager.request(this, TileFluidNodeProvider.PACKET_TANK.id), world, new Vector3(this), 60)
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
}