package resonantinduction.core.grid.fluid

import cpw.mods.fml.common.network.ByteBufUtils
import io.netty.buffer.ByteBuf
import net.minecraft.block.material.Material
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids._
import resonant.content.prefab.java.TileAdvanced
import resonant.lib.network.discriminator.{PacketTile, PacketType}
import resonant.lib.network.handle.{IPacketIDReceiver, TPacketIDSender}
import resonantinduction.core.prefab.node.NodeTank
import universalelectricity.api.core.grid.{INode, INodeProvider}

/**
 * A prefab class for tiles that use the fluid network.
 *
 * @author DarkGuardsman
 */
class TileTankNode(material: Material) extends TileAdvanced(material) with INodeProvider with IFluidHandler with IPacketIDReceiver with TPacketIDSender
{
  val PACKET_DESCRIPTION = 0
  val PACKET_RENDER = 1
  protected var colorID: Int = 0
  var renderSides: Byte = 0

  var tankNode : NodeTank = new NodeTank(this)

  def getTank() : IFluidTank = tankNode

  def getFluid() : FluidStack = getTank().getFluid

  def getFluidCapacity() : Int = getTank().getCapacity

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
      val  packet: PacketTile = new PacketTile(this);
      packet <<< PACKET_RENDER
      packet <<< colorID
      packet <<< renderSides
      sendPacket(packet)
    }
  }

  override def getDescPacket() : PacketTile =
  {
      val  packet: PacketTile = new PacketTile(this);
      packet <<< PACKET_DESCRIPTION
      packet <<< colorID
      packet <<< renderSides
      val tag : NBTTagCompound = new NBTTagCompound()
      tankNode.save(tag)
      packet <<< tag
      sendPacket(packet)
      return packet;
  }

  override def read(buf: ByteBuf, id: Int, player: EntityPlayer, t: PacketType): Boolean =
  {
    if(id == PACKET_DESCRIPTION)
    {
      colorID = buf.readInt()
      renderSides = buf.readByte()
      tankNode.load(ByteBufUtils.readTag(buf))
    }
    else if(id == PACKET_RENDER)
    {
      colorID = buf.readInt()
      renderSides = buf.readByte()
    }
    return tankNode.read(buf, id, player, t);
  }

  override def getNode(nodeType: Class[_ <: INode], from: ForgeDirection): INode = if(nodeType.isInstanceOf[NodeTank]) return tankNode else null

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