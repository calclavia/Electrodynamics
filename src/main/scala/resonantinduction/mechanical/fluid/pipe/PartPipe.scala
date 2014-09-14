package resonantinduction.mechanical.fluid.pipe

import codechicken.lib.data.MCDataInput
import codechicken.lib.render.CCRenderState
import codechicken.lib.vec.Vector3
import codechicken.multipart.{TNormalOcclusion, TSlottedPart}
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids._
import resonant.lib.`type`.EvictingList
import resonantinduction.core.prefab.part.connector.{PartFramedNode, TColorable, TMaterial}
import resonantinduction.mechanical.Mechanical
import resonantinduction.mechanical.fluid.pipe.PipeMaterials.PipeMaterial

/**
 * Fluid transport pipe
 *
 * @author Calclavia
 */
class PartPipe extends PartFramedNode with TMaterial[PipeMaterial] with TColorable with TSlottedPart with TNormalOcclusion with IFluidHandler
{
  protected final val tank: FluidTank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME)

  /**
   * Computes the average fluid for client to render.
   */
  private val averageTankData = new EvictingList[Integer](20)
  private var markPacket: Boolean = true

  setNode(new PipePressureNode(this))

  def preparePlacement(meta: Int)
  {
    setMaterial(meta)
  }

  def setMaterial(i: Int)
  {
    material = PipeMaterials(i).asInstanceOf[PipeMaterial]
  }

  def getMaterialID: Int = material.id

  override def update()
  {
    super.update()

    averageTankData.add(tank.getFluidAmount)

    if (!world.isRemote && markPacket)
    {
      sendFluidUpdate
      markPacket = false
    }
  }

  /**
   * Sends fluid level to the client to be used in the renderer
   */
  def sendFluidUpdate
  {
    val nbt = new NBTTagCompound
    var averageAmount: Int = 0
    if (averageTankData.size > 0)
    {
      for (i <- 0 until averageTankData.size)
      {
        {
          averageAmount += averageTankData.get(i)
        }
      }

      averageAmount /= averageTankData.size
    }
    val tempTank: FluidTank = if (tank.getFluid != null) new FluidTank(tank.getFluid.getFluid, averageAmount, tank.getCapacity) else new FluidTank(tank.getCapacity)
    tempTank.writeToNBT(nbt)
    tile.getWriteStream(this).writeByte(3).writeInt(tank.getCapacity).writeNBTTagCompound(nbt)
  }

  override def read(packet: MCDataInput, packetID: Int)
  {
    if (packetID == 3)
    {
      tank.setCapacity(packet.readInt)
      tank.readFromNBT(packet.readNBTTagCompound)
    }
    else
    {
      super.read(packet, packetID)
    }
  }

  @SideOnly(Side.CLIENT)
  override def renderDynamic(pos: Vector3, frame: Float, pass: Int)
  {
    RenderPipe.INSTANCE.render(this, pos.x, pos.y, pos.z, frame)
  }

  def getItem: ItemStack =
  {
    return new ItemStack(Mechanical.itemPipe, 1, getMaterialID)
  }

  def fill(from: ForgeDirection, resource: FluidStack, doFill: Boolean): Int =
  {
    if (!world.isRemote)
    {
      if (doFill)
      {
        markPacket = true
      }
      return tank.fill(resource, doFill)
    }
    return 0
  }

  def drain(from: ForgeDirection, resource: FluidStack, doDrain: Boolean): FluidStack =
  {
    return drain(from, resource.amount, doDrain)
  }

  def drain(from: ForgeDirection, maxDrain: Int, doDrain: Boolean): FluidStack =
  {
    if (!world.isRemote)
    {
      if (doDrain)
      {
        markPacket = true
      }
      return tank.drain(maxDrain, doDrain)
    }
    return null
  }

  def canFill(from: ForgeDirection, fluid: Fluid): Boolean =
  {
    return true
  }

  def canDrain(from: ForgeDirection, fluid: Fluid): Boolean =
  {
    return true
  }

  def getTankInfo(from: ForgeDirection): Array[FluidTankInfo] =
  {
    return Array[FluidTankInfo](tank.getInfo)
  }

  override def drawBreaking(renderBlocks: RenderBlocks)
  {
    CCRenderState.reset()
  }

  override def save(nbt: NBTTagCompound)
  {
    super.save(nbt)
    tank.writeToNBT(nbt)
  }

  override def load(nbt: NBTTagCompound)
  {
    super.load(nbt)
    tank.readFromNBT(nbt)
  }

  override def getSlotMask: Int =
  {
    return 0
  }

}