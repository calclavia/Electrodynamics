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
import resonant.lib.grid.node.TSpatialNodeProvider
import resonant.lib.network.discriminator.{PacketTile, PacketType}
import resonant.lib.network.handle.TPacketIDReceiver
import resonant.lib.prefab.fluid.NodeFluid

/**
 * A prefab class for tiles that use the fluid network.
 *
 * @author DarkGuardsman, Calclavia
 */
abstract class TileFluidProvider(material: Material) extends TileAdvanced(material) with TSpatialNodeProvider with IFluidHandler with TPacketIDReceiver
{
  val descriptionPacket = 0
  val renderPacket = 1

  protected val fluidNode: NodeFluid
  protected var colorID: Int = 0
  protected var clientRenderMask = 0x00

  nodes.add(fluidNode)

  def getFluid: FluidStack = getTank.getFluid

  def getFluidCapacity: Int = getTank.getCapacity

  def getTank: IFluidTank = fluidNode

  override def readFromNBT(nbt: NBTTagCompound)
  {
    super.readFromNBT(nbt)
    colorID = nbt.getInteger("colorID")
  }

  override def writeToNBT(nbt: NBTTagCompound)
  {
    super.writeToNBT(nbt)
    nbt.setInteger("colorID", colorID)
  }

  def sendRenderUpdate
  {
    if (!world.isRemote)
    {
      val packet: PacketTile = new PacketTile(this);
      packet <<< renderPacket
      packet <<< colorID
      packet <<< fluidNode.connectedBitmask
      sendPacket(packet)
    }
  }

  override def getDescPacket(): PacketTile =
  {
    val packet: PacketTile = new PacketTile(this);
    packet <<< descriptionPacket
    packet <<< colorID
    packet <<< fluidNode.connectedBitmask
    val tag: NBTTagCompound = new NBTTagCompound()
    fluidNode.save(tag)
    packet <<< tag
    sendPacket(packet)
    return packet
  }

  override def read(buf: ByteBuf, id: Int, player: EntityPlayer, t: PacketType): Boolean =
  {
    if (id == descriptionPacket)
    {
      colorID = buf.readInt()
      clientRenderMask = buf.readByte()
      fluidNode.load(ByteBufUtils.readTag(buf))
    }
    else if (id == renderPacket)
    {
      colorID = buf.readInt()
      clientRenderMask = buf.readByte()
    }

    return false
  }

  override def drain(from: ForgeDirection, resource: FluidStack, doDrain: Boolean): FluidStack = fluidNode.drain(from, resource, doDrain)

  override def drain(from: ForgeDirection, maxDrain: Int, doDrain: Boolean): FluidStack = fluidNode.drain(from, maxDrain, doDrain)

  override def canFill(from: ForgeDirection, fluid: Fluid): Boolean = fluidNode.canFill(from, fluid)

  override def canDrain(from: ForgeDirection, fluid: Fluid): Boolean = fluidNode.canDrain(from, fluid)

  override def fill(from: ForgeDirection, resource: FluidStack, doFill: Boolean): Int = fluidNode.fill(from, resource, doFill)

  override def getTankInfo(from: ForgeDirection): Array[FluidTankInfo] = fluidNode.getTankInfo(from)

  def setCapacity(capacity: Int)
  {
    fluidNode.setCapacity(capacity)
  }

  def getFluidAmount: Int = fluidNode.getFluidAmount
}