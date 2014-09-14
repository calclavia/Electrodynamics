package resonantinduction.mechanical.fluid.pipe

import codechicken.multipart.{TNormalOcclusion, TSlottedPart}
import net.minecraftforge.fluids._
import resonantinduction.core.prefab.part.{PartFramedNode, TColorable, TMaterial}

/**
 * Fluid transport pipe
 *
 * @author Calclavia,
 */
class PartPipe extends PartFramedNode with TMaterial[EnumPipeMaterial] with TColorable with TSlottedPart with TNormalOcclusion with IFluidHandler
{
  setNode(new PipePressureNode(this))

  def setMaterial(i: Int)
  {
    material = EnumPipeMaterial.values(i)
  }

  def getMaterialID: Int = material.ordinal


  override def update
  {
    super.update
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
    val nbt: NBTTagCompound = new NBTTagCompound
    var averageAmount: Int = 0
    if (averageTankData.size > 0)
    {
      {
        var i: Int = 0
        while (i < averageTankData.size)
        {
          {
            averageAmount += averageTankData.get(i)
          }
          (
          {
            i += 1;
            i - 1
          })
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

  @SideOnly(Side.CLIENT) override def renderDynamic(pos: Vector3, frame: Float, pass: Int)
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
    CCRenderState.reset
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

  override def getOcclusionBoxes: Set[Cuboid6] =
  {
    return null
  }

  override def getSlotMask: Int =
  {
    return 0
  }

  protected final val tank: FluidTank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME)

  /**
   * Computes the average fluid for client to render.
   */
  private var averageTankData: EvictingList[Integer] = new EvictingList[Integer](20)
  private var markPacket: Boolean = true
}