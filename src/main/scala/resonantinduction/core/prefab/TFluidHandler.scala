package resonantinduction.core.prefab

import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids._

/**
 * Created by robert on 8/14/2014.
 */
trait TFluidHandler extends IFluidHandler
{
  var fluidTank : FluidTank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME)

  override def fill(from: ForgeDirection, resource: FluidStack, doFill: Boolean): Int = fluidTank.fill(resource, doFill)

  override def drain(from: ForgeDirection, resource: FluidStack, doDrain: Boolean): FluidStack =
  {
    if(resource != null && resource.isFluidEqual(fluidTank.getFluid))
    {
      return drain(from, resource.amount, doDrain)
    }
    return null
  }

  override def drain(from: ForgeDirection, maxDrain: Int, doDrain: Boolean): FluidStack = fluidTank.drain(maxDrain, doDrain)

  override def canFill(from: ForgeDirection, fluid: Fluid): Boolean = true

  override def canDrain(from: ForgeDirection, fluid: Fluid): Boolean = true

  override def getTankInfo(from: ForgeDirection): Array[FluidTankInfo] = Array[FluidTankInfo](fluidTank.getInfo)
}
