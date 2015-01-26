package edx.core.prefab.node

import io.netty.buffer.ByteBuf
import net.minecraft.block.material.Material
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids._
import resonantengine.core.network.discriminator.PacketType
import resonantengine.lib.grid.core.TBlockNodeProvider
import resonantengine.lib.modcontent.block.ResonantTile
import resonantengine.lib.prefab.fluid.NodeFluid
import resonantengine.lib.wrapper.ByteBufWrapper._
import resonantengine.prefab.network.{TPacketReceiver, TPacketSender}

/**
 * A prefab class for tiles that use the fluid network.
 *
 * @author DarkGuardsman, Calclavia
 */
abstract class TileFluidProvider(material: Material) extends ResonantTile(material) with TBlockNodeProvider with IFluidHandler with TPacketReceiver with TPacketSender
{
  protected var colorID: Int = 0
  protected var clientRenderMask = 0x3F
  private var _fluidNode: NodeFluid = null

  override def start()
  {
    super.start()

    if (!world.isRemote)
    {
      fluidNode.onConnectionChanged()
    }
  }

  override def write(buf: ByteBuf, id: Int)
  {
    super.write(buf, id)

    id match
    {
      case 0 =>
      {
        buf <<< colorID
        buf <<< clientRenderMask
        buf <<< fluidNode.getPrimaryTank
      }
      case 1 =>
      {
        buf <<< fluidNode.getPrimaryTank
      }
    }
  }

  def fluidNode = _fluidNode

  def fluidNode_=(newNode: NodeFluid)
  {
    _fluidNode = newNode
    fluidNode.onConnectionChanged = () =>
    {
      clientRenderMask = fluidNode.connectedMask
      sendPacket(0)
    }
    fluidNode.onFluidChanged = () => if (!world.isRemote) sendPacket(1)
    nodes.add(fluidNode)
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
          fluidNode.setPrimaryTank(buf.readTank())
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