package resonantinduction.core.prefab.node

import cpw.mods.fml.common.network.ByteBufUtils
import io.netty.buffer.ByteBuf
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids._
import resonant.api.grid.{INode, INodeProvider}
import resonant.content.prefab.java.TileAdvanced
import resonant.lib.network.discriminator.{PacketTile, PacketType}
import resonant.lib.network.handle.TPacketIDReceiver

/**
 * A prefab class for tiles that use the fluid network.
 *
 * @author DarkGuardsman
 */
class TileTankNode(material: Material) extends TileAdvanced(material) with INodeProvider with IFluidHandler with TPacketIDReceiver
{
  val PACKET_DESCRIPTION = 0
  val PACKET_RENDER = 1
  var renderSides: Byte = 0
  var tankNode: NodeTank = new NodeTank(this, 16)
  protected var colorID: Int = 0

  def getFluid: FluidStack = getTank.getFluid

  def getFluidCapacity: Int = getTank.getCapacity

  def getTank: IFluidTank = tankNode

  override def start()
  {
    super.start()
    tankNode.reconstruct()
  }

  override def invalidate()
  {
    tankNode.deconstruct()
    super.invalidate()
  }

  override def onWorldJoin()
  {
    tankNode.reconstruct()
  }

  override def onNeighborChanged(block: Block)
  {
    tankNode.reconstruct()
  }

  override def onWorldSeparate()
  {
    tankNode.deconstruct()
  }

  override def readFromNBT(nbt: NBTTagCompound)
  {
    super.readFromNBT(nbt)
    tankNode.load(nbt)
    colorID = nbt.getInteger("colorID")
  }

  override def writeToNBT(nbt: NBTTagCompound)
  {
    super.writeToNBT(nbt)
    tankNode.save(nbt)
    nbt.setInteger("colorID", colorID)
  }

  def sendRenderUpdate
  {
    if (!world.isRemote)
    {
      val packet: PacketTile = new PacketTile(this);
      packet <<< PACKET_RENDER
      packet <<< colorID
      packet <<< renderSides
      sendPacket(packet)
    }
  }

  override def getDescPacket(): PacketTile =
  {
    val packet: PacketTile = new PacketTile(this);
    packet <<< PACKET_DESCRIPTION
    packet <<< colorID
    packet <<< renderSides
    val tag: NBTTagCompound = new NBTTagCompound()
    tankNode.save(tag)
    packet <<< tag
    sendPacket(packet)
    return packet
  }

  override def read(buf: ByteBuf, id: Int, player: EntityPlayer, t: PacketType): Boolean =
  {
    //TODO handle fluid node's packet code
    if (id == PACKET_DESCRIPTION)
    {
      colorID = buf.readInt()
      renderSides = buf.readByte()
      tankNode.load(ByteBufUtils.readTag(buf))
    }
    else if (id == PACKET_RENDER)
    {
      colorID = buf.readInt()
      renderSides = buf.readByte()
    }

    return false
  }

  override def getNode[N <: INode](nodeType: Class[_ <: N], from: ForgeDirection): N = (if (nodeType.isInstanceOf[NodeTank]) tankNode else null).asInstanceOf[N]

  override def drain(from: ForgeDirection, resource: FluidStack, doDrain: Boolean): FluidStack = tankNode.drain(from, resource, doDrain)

  override def drain(from: ForgeDirection, maxDrain: Int, doDrain: Boolean): FluidStack = tankNode.drain(from, maxDrain, doDrain)

  override def canFill(from: ForgeDirection, fluid: Fluid): Boolean = tankNode.canFill(from, fluid)

  override def canDrain(from: ForgeDirection, fluid: Fluid): Boolean = tankNode.canDrain(from, fluid)

  override def fill(from: ForgeDirection, resource: FluidStack, doFill: Boolean): Int = tankNode.fill(from, resource, doFill)

  override def getTankInfo(from: ForgeDirection): Array[FluidTankInfo] = tankNode.getTankInfo(from)

  def setCapacity(capacity: Int)
  {
    tankNode.setCapacity(capacity)
  }

  def getFluidAmount: Int = tankNode.getFluidAmount
}