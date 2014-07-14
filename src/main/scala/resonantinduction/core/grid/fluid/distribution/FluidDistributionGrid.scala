package resonantinduction.core.grid.fluid.distribution

import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.{FluidStack, FluidTank, FluidTankInfo}
import universalelectricity.core.grid.{TickingGrid, UpdateTicker}

/**
 * Used for multiblock tanks to distribute fluid.
 *
 * @author DarkCow, Calclavia
 */
abstract class FluidDistributionGrid extends TickingGrid[IFluidDistributor]
{
  val tank = new FluidTank(0)
  var needsUpdate = false

  override def canUpdate: Boolean =
  {
    return needsUpdate && getNodes.size > 0
  }

  override def continueUpdate: Boolean =
  {
    return canUpdate
  }

  override def reconstruct()
  {
    tank.setCapacity(0)
    tank.setFluid(null)
    super.reconstruct()
    needsUpdate = true
    UpdateTicker.addUpdater(this)
  }

  override def reconstructNode(node: IFluidDistributor)
  {
    val connectorTank: FluidTank = node.getInternalTank

    if (connectorTank != null)
    {
      tank.setCapacity(tank.getCapacity + connectorTank.getCapacity)
      if (connectorTank.getFluid != null)
      {
        if (tank.getFluid == null)
        {
          tank.setFluid(connectorTank.getFluid.copy)
        }
        else if (tank.getFluid.isFluidEqual(connectorTank.getFluid))
        {
          tank.getFluid.amount += connectorTank.getFluidAmount
        }
        else if (tank.getFluid != null)
        {
          //TODO: Mix fluid
        }
      }
    }
  }

  def fill(source: IFluidDistributor, from: ForgeDirection, resource: FluidStack, doFill: Boolean): Int =
  {
    val fill: Int = this.getTank.fill(resource.copy, doFill)
    if (fill > 0)
    {
      needsUpdate = true
      UpdateTicker.addUpdater(this)
    }
    return fill
  }

  def drain(source: IFluidDistributor, from: ForgeDirection, resource: FluidStack, doDrain: Boolean): FluidStack =
  {
    if (resource != null && resource.isFluidEqual(getTank.getFluid))
    {
      val drain: FluidStack = getTank.drain(resource.amount, doDrain)
      needsUpdate = true
      UpdateTicker.addUpdater(this)
      return drain
    }
    return null
  }

  def drain(source: IFluidDistributor, from: ForgeDirection, resource: Int, doDrain: Boolean): FluidStack =
  {
    val drain: FluidStack = getTank.drain(resource, doDrain)
    needsUpdate = true
    UpdateTicker.addUpdater(this)
    return drain
  }

  def getTankInfo: Array[FluidTankInfo] =
  {
    return Array[FluidTankInfo](getTank.getInfo)
  }

  override def toString: String =
  {
    return super.toString + " Volume: " + this.tank.getFluidAmount
  }
}