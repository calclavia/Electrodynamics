package resonantinduction.core.grid.fluid.distribution

import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids._

/**
 * Wraps the fluid handler to forward it to a certain object.
 * @author Calclavia
 */
trait TFluidForwarder extends IFluidHandler
{
  def getForwardTank: IFluidHandler

  override def fill(from: ForgeDirection, resource: FluidStack, doFill: Boolean): Int =
  {
    return getForwardTank.fill(from, resource, doFill)
  }

  override def drain(from: ForgeDirection, resource: FluidStack, doDrain: Boolean): FluidStack =
  {
    return getForwardTank.drain(from, resource, doDrain)
  }

  override def drain(from: ForgeDirection, maxDrain: Int, doDrain: Boolean): FluidStack =
  {
    return getForwardTank.drain(from, maxDrain, doDrain)
  }

  override def canFill(from: ForgeDirection, fluid: Fluid): Boolean =
  {
    return getForwardTank.canFill(from, fluid)
  }

  override def canDrain(from: ForgeDirection, fluid: Fluid): Boolean =
  {
    return getForwardTank.canDrain(from, fluid)
  }

  override def getTankInfo(from: ForgeDirection): Array[FluidTankInfo] =
  {
    return getForwardTank.getTankInfo(from)
  }

}
