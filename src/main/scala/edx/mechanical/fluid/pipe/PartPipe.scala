package edx.mechanical.fluid.pipe

import java.lang.{Iterable => JIterable}

import codechicken.lib.data.{MCDataInput, MCDataOutput}
import codechicken.lib.raytracer.IndexedCuboid6
import codechicken.lib.render.CCRenderState
import codechicken.lib.vec.Vector3
import cpw.mods.fml.relauncher.{Side, SideOnly}
import edx.core.prefab.part.CuboidShapes
import edx.core.prefab.part.connector.{PartFramedNode, TColorable, TMaterial}
import edx.mechanical.MechanicalContent
import edx.mechanical.fluid.pipe.PipeMaterials.PipeMaterial
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids._
import resonantengine.lib.collection.EvictingList
import resonantengine.lib.wrapper.BitmaskWrapper._

import scala.collection.convert.wrapAll._
import scala.collection.mutable

/**
 * Fluid transport pipe
 *
 * @author Calclavia
 */
class PartPipe extends PartFramedNode with TMaterial[PipeMaterial] with TColorable with IFluidHandler
{
  override lazy val node = new NodePipe(this)

  /**
   * Computes the average fluid for client to render.
   */
  private val averageTankData = new EvictingList[Integer](20)
  private var markPacket = true

  material = PipeMaterials.ceramic
  node.onConnectionChanged = () => sendPacket(0)
  node.onFluidChanged = () => markPacket = true

  override def getSubParts: JIterable[IndexedCuboid6] =
  {
    val sideCuboids = CuboidShapes.thickSegment
    val list = mutable.Set.empty[IndexedCuboid6]
    list += CuboidShapes.thickCenter
    list ++= ForgeDirection.VALID_DIRECTIONS.filter(s => clientRenderMask.mask(s) || s == testingSide).map(s => sideCuboids(s.ordinal()))
    return list
  }

  def preparePlacement(meta: Int)
  {
    setMaterial(meta)
  }

  def setMaterial(i: Int)
  {
    material = PipeMaterials(i).asInstanceOf[PipeMaterial]
  }

  override def update()
  {
    super.update()

    averageTankData.add(node.getFluidAmount)

    if (!world.isRemote && markPacket)
    {
      sendPacket(3)
      markPacket = false
    }
  }

  /**
   * Packet Methods
   */
  override def write(packet: MCDataOutput, id: Int)
  {
    super[PartFramedNode].write(packet, id)
    super[TMaterial].write(packet, id)
    super[TColorable].write(packet, id)

    if (id == 3)
    {
      //node Packet
      val nbt = new NBTTagCompound
      val averageAmount = averageTankData.reduce(_ + _) / averageTankData.size
      val tempTank = node.getTank //if (node.getFluid != null) new FluidTank(node.getFluid.getFluid, averageAmount, node.getCapacity) else new FluidTank(node.getCapacity)
      tempTank.writeToNBT(nbt)
      packet.writeInt(node.getCapacity).writeNBTTagCompound(nbt)
    }
  }

  override def read(packet: MCDataInput, packetID: Int)
  {
    super[PartFramedNode].read(packet, packetID)
    super[TMaterial].read(packet, packetID)
    super[TColorable].read(packet, packetID)

    if (packetID == 3 && world.isRemote)
    {
      node.setPrimaryTank(new FluidTank(packet.readInt))
      node.getTank.readFromNBT(packet.readNBTTagCompound)
    }
  }

  /**
   * NBT Methods
   */
  override def load(nbt: NBTTagCompound)
  {
    super[PartFramedNode].load(nbt)
    super[TMaterial].load(nbt)
    super[TColorable].load(nbt)

    node.load(nbt)
  }

  override def save(nbt: NBTTagCompound)
  {
    super[PartFramedNode].save(nbt)
    super[TMaterial].save(nbt)
    super[TColorable].save(nbt)

    node.save(nbt)
  }

  @SideOnly(Side.CLIENT)
  override def renderDynamic(pos: Vector3, frame: Float, pass: Int)
  {
    RenderPipe.render(this, pos.x, pos.y, pos.z, frame)
  }

  def getItem: ItemStack = new ItemStack(MechanicalContent.itemPipe, 1, getMaterialID)

  def getMaterialID: Int = material.id

  override def fill(from: ForgeDirection, resource: FluidStack, doFill: Boolean): Int =
  {
    if (!world.isRemote)
    {
      if (doFill)
        markPacket = true

      return node.fill(resource, doFill)
    }
    return 0
  }

  override def drain(from: ForgeDirection, resource: FluidStack, doDrain: Boolean): FluidStack =
  {
    return drain(from, resource.amount, doDrain)
  }

  override def drain(from: ForgeDirection, maxDrain: Int, doDrain: Boolean): FluidStack =
  {
    if (!world.isRemote)
    {
      if (doDrain)
        markPacket = true

      return node.drain(maxDrain, doDrain)
    }
    return null
  }

  override def canFill(from: ForgeDirection, fluid: Fluid): Boolean = true

  override def canDrain(from: ForgeDirection, fluid: Fluid): Boolean = true

  override def getTankInfo(from: ForgeDirection): Array[FluidTankInfo] = Array[FluidTankInfo](node.getInfo)

  override def drawBreaking(renderBlocks: RenderBlocks)
  {
    CCRenderState.reset()
  }
}