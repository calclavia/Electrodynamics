package resonantinduction.archaic.fluid.gutter

import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.{Fluid, FluidContainerRegistry, FluidStack}
import resonantinduction.core.prefab.node.{NodePressure, TileFluidProvider}

/**
 * A node for fluid that follows pressure and gravity.
 * @author Calclavia
 */
class NodePressureGravity(parent: TileFluidProvider, volume: Int = FluidContainerRegistry.BUCKET_VOLUME) extends NodePressure(parent, volume)
{
  override protected def doDistribute(dir: ForgeDirection, nodeA: NodePressure, nodeB: NodePressure, flowRate: Int)
  {
    if (dir == ForgeDirection.DOWN)
    {
      val tankA = nodeA.getPrimaryTank
      val tankB = nodeB.getPrimaryTank
      val pressureA = nodeA.pressure(dir)
      val pressureB = nodeB.pressure(dir.getOpposite)
      val amountA = tankA.getFluidAmount
      val amountB = tankB.getFluidAmount

      var quantity = Math.max(if (pressureA > pressureB) (pressureA - pressureB) * flowRate else amountA, amountA)
      quantity = Math.min(Math.min(quantity, tankB.getCapacity - amountB), amountA)

      if (quantity > 0)
      {
        val drainStack = drain(dir.getOpposite, quantity, false)
        if (drainStack != null && drainStack.amount > 0)
        {
          drain(dir.getOpposite, nodeB.fill(dir, drainStack, true), true)
        }
      }

    }
    else if (dir != ForgeDirection.UP)
    {
      super.doDistribute(dir, nodeA, nodeB, flowRate)
    }
  }

  override def canFill(from: ForgeDirection, fluid: Fluid): Boolean =
  {
    return from != ForgeDirection.UP && !fluid.isGaseous
  }

  override def canDrain(from: ForgeDirection, fluid: Fluid): Boolean =
  {
    return from != ForgeDirection.UP && !fluid.isGaseous
  }

  override def fill(from: ForgeDirection, resource: FluidStack, doFill: Boolean): Int =
  {
    if (!resource.getFluid.isGaseous)
    {
      return super.fill(from, resource, doFill)
    }
    return 0
  }
}