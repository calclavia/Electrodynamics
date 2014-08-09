package resonantinduction.core.grid.fluid.pressure

import net.minecraft.block.material.Material
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.{FluidContainerRegistry, FluidStack, FluidTank, FluidTankInfo}
import resonantinduction.core.grid.fluid.TileTankNode
import universalelectricity.api.core.grid.INode

/**
 * A prefab class for tiles that use the fluid network.
 *
 * @author DarkGuardsman
 */
abstract class TilePressureNode(material: Material) extends TileTankNode(material)
{
  protected val pressureNode: FluidPressureNode
  tank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME)

  override def start
  {
    super.start
    pressureNode.reconstruct
  }

  override def invalidate
  {
    pressureNode.deconstruct
    super.invalidate
  }

  override def fill(from: ForgeDirection, resource: FluidStack, doFill: Boolean): Int =
  {
    val fill: Int = tank.fill(resource, doFill)
    onFluidChanged
    return fill
  }

  override def drain(from: ForgeDirection, resource: FluidStack, doDrain: Boolean): FluidStack =
  {
    return drain(from, resource.amount, doDrain)
  }

  override def drain(from: ForgeDirection, maxDrain: Int, doDrain: Boolean): FluidStack =
  {
    val drain = tank.drain(maxDrain, doDrain)
    onFluidChanged
    return drain
  }

  override def getTankInfo(from: ForgeDirection): Array[FluidTankInfo] =
  {
    return Array[FluidTankInfo](tank.getInfo)
  }

  def getSubID: Int =
  {
    return this.colorID
  }

  def setSubID(id: Int)
  {
    this.colorID = id
  }

  def getPressureTank: FluidTank =
  {
    return tank
  }

  override def getNode(nodeType: Class[_ <: INode], from: ForgeDirection): INode =
  {
    if (nodeType.isAssignableFrom(pressureNode.getClass))
      return pressureNode

    return null
  }
}