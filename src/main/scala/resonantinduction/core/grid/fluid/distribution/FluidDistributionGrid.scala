package resonantinduction.core.grid.fluid.distribution

import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids._
import resonantinduction.core.grid.fluid.TileTankNode
import universalelectricity.core.grid.{TickingGrid, UpdateTicker}

/**
 * Used for multiblock tanks to distribute fluid.
 *
 * @author DarkCow, Calclavia
 */
abstract class FluidDistributionGrid extends TickingGrid[TankNode] with IFluidHandler
{
  val tank = new FluidTank(0)
  var needsUpdate = false

  override def canUpdate: Boolean =
  {
    return needsUpdate && getNodes().size > 0
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

  override def reconstructNode(node: TankNode)
  {
    val connectorTank: FluidTank = node.getParent.asInstanceOf[TileTankNode].getTank

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

  override def fill(from: ForgeDirection, resource: FluidStack, doFill: Boolean): Int =
  {
    val fill: Int = tank.fill(resource.copy, doFill)
    if (fill > 0)
    {
      needsUpdate = true
      UpdateTicker.addUpdater(this)
    }
    return fill
  }

  override def drain(from: ForgeDirection, resource: FluidStack, doDrain: Boolean): FluidStack =
  {
    if (resource != null && resource.isFluidEqual(tank.getFluid))
    {
      val drain: FluidStack = tank.drain(resource.amount, doDrain)
      needsUpdate = true
      UpdateTicker.addUpdater(this)
      return drain
    }
    return null
  }

  override def drain(from: ForgeDirection, resource: Int, doDrain: Boolean): FluidStack =
  {
    val drain: FluidStack = tank.drain(resource, doDrain)
    needsUpdate = true
    UpdateTicker.addUpdater(this)
    return drain
  }

  override def canFill(from: ForgeDirection, fluid: Fluid) = true

  override def canDrain(from: ForgeDirection, fluid: Fluid) = true

  override def getTankInfo(from: ForgeDirection) = Array[FluidTankInfo](tank.getInfo)

  override def toString: String =
  {
    return super.toString + " Volume: " + this.tank.getFluidAmount
  }
}