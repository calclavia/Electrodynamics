package resonantinduction.core.prefab.node

import io.netty.buffer.ByteBuf
import net.minecraft.block.material.Material
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids._
import resonant.content.prefab.java.TileAdvanced
import resonant.lib.grid.node.TSpatialNodeProvider
import resonant.lib.network.ByteBufWrapper._
import resonant.lib.network.discriminator.PacketType
import resonant.lib.network.handle.{TPacketReceiver, TPacketSender}
import resonant.lib.prefab.fluid.NodeFluid

/**
 * A prefab class for tiles that use the fluid network.
 *
 * @author DarkGuardsman, Calclavia
 */
abstract class TileFluidProvider(material: Material) extends TileAdvanced(material) with TSpatialNodeProvider with IFluidHandler with TPacketReceiver with TPacketSender
{
  private var _fluidNode: NodeFluid = null
  protected var colorID: Int = 0
  protected var clientRenderMask = 0x3F

  def fluidNode = _fluidNode

  def fluidNode_=(newNode: NodeFluid)
  {
    _fluidNode = newNode
    fluidNode.onConnectionChanged = () => if (!world.isRemote) sendPacket(1)
    nodes.add(fluidNode)
  }

  override def write(buf: ByteBuf, id: Int)
  {
    super.write(buf, id)

    id match
    {
      case 0 =>
      {
        buf <<< colorID
        buf <<< fluidNode.connectedMask
        buf <<< fluidNode.getPrimaryTank
      }
      case 1 =>
      {
        buf <<< colorID
        buf <<< fluidNode.connectedMask
      }
    }
  }

  override def read(buf: ByteBuf, id: Int, packet: PacketType)
  {
    super.read(buf, id, packet)

    if (world.isRemote)
    {
      id match
      {
        case 0 =>
        {
          colorID = buf.readInt()
          clientRenderMask = buf.readInt()
          fluidNode.setPrimaryTank(buf.readTank())
        }
        case 1 =>
        {
          colorID = buf.readInt()
          clientRenderMask = buf.readInt()
        }
      }

      markRender()
    }
  }

  override def readFromNBT(nbt: NBTTagCompound)
  {
    super.readFromNBT(nbt)
    fluidNode.load(nbt)
    colorID = nbt.getInteger("colorID")
  }

  override def writeToNBT(nbt: NBTTagCompound)
  {
    super.writeToNBT(nbt)
    fluidNode.save(nbt)
    nbt.setInteger("colorID", colorID)
  }

  override def drain(from: ForgeDirection, resource: FluidStack, doDrain: Boolean): FluidStack = fluidNode.drain(from, resource, doDrain)

  override def drain(from: ForgeDirection, maxDrain: Int, doDrain: Boolean): FluidStack = fluidNode.drain(from, maxDrain, doDrain)

  override def canFill(from: ForgeDirection, fluid: Fluid): Boolean = fluidNode.canFill(from, fluid)

  override def canDrain(from: ForgeDirection, fluid: Fluid): Boolean = fluidNode.canDrain(from, fluid)

  override def fill(from: ForgeDirection, resource: FluidStack, doFill: Boolean): Int = fluidNode.fill(from, resource, doFill)

  override def getTankInfo(from: ForgeDirection): Array[FluidTankInfo] = fluidNode.getTankInfo(from)
}