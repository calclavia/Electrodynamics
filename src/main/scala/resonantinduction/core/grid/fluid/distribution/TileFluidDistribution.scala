package resonantinduction.core.grid.fluid.distribution

import net.minecraft.block.material.Material
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.{Fluid, FluidStack, FluidTankInfo}
import resonantinduction.core.grid.fluid.TileFluidNode

/**
 * A prefab class for tiles that use the fluid network.
 *
 * @author DarkGuardsman
 */
abstract class TileFluidDistribution(material: Material) extends TileFluidNode(material) with IFluidDistributor
{
  override def start
  {
    super.start
    tankNode.reconstruct
  }

  override def invalidate
  {
    tankNode.deconstruct
    super.invalidate
  }

  def fill(from: ForgeDirection, resource: FluidStack, doFill: Boolean): Int =
  {
    return tankNode.fill(from, resource, doFill)
  }

  def drain(from: ForgeDirection, resource: FluidStack, doDrain: Boolean): FluidStack =
  {
    return tankNode.drain(from, resource, doDrain)
  }

  def drain(from: ForgeDirection, maxDrain: Int, doDrain: Boolean): FluidStack =
  {
    return tankNode.drain(from, maxDrain, doDrain)
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
    return Array[FluidTankInfo](getInternalTank.getInfo)
  }

  /**
   * Network used to link all parts together
   */
  protected var tankNode: TankNode = null
}