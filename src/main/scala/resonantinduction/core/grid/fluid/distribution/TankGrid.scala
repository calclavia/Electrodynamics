package resonantinduction.core.grid.fluid.distribution

import net.minecraft.tileentity.TileEntity
import net.minecraftforge.fluids.FluidStack
import resonant.lib.utility.FluidUtility
import resonantinduction.core.grid.fluid.TileTankNode

import scala.collection.JavaConversions._
import scala.collection.mutable

/** Network that handles connected tanks
  *
  * @author DarkGuardsman */
class TankGrid extends FluidDistributionGrid
{
  needsUpdate = true

  override def update(deltaTime: Double)
  {
    val networkTankFluid = tank.getFluid

    if (getNodes.size > 0)
    {
      var totalFluid = if (networkTankFluid != null) networkTankFluid.amount else 0

      /**
       * Creates a priority queue with tanks in the bottom as highest priority.
       */
      val heightPriorityQueue = new mutable.PriorityQueue[TileTankNode]()(new Ordering[TileTankNode]
      {
        def compare(a: TileTankNode, b: TileTankNode): Int =
        {
          if (networkTankFluid != null && networkTankFluid.getFluid.isGaseous) return 0
          return b.asInstanceOf[TileEntity].yCoord - a.asInstanceOf[TileEntity].yCoord
        }
      })

      heightPriorityQueue ++= (getNodes() map (_.genericParent))

      var didChange = false

      while (!heightPriorityQueue.isEmpty)
      {
        val distributeNode = heightPriorityQueue.dequeue()
        val yCoord = distributeNode.yCoord
        val connectorCount = heightPriorityQueue count (_.yCoord == yCoord)

        if (totalFluid <= 0)
        {
          distributeNode.getTank.setFluid(null)
          distributeNode.onFluidChanged
        }
        else
        {
          val fluidPer: Int = totalFluid / connectorCount
          val deltaFluidAmount: Int = fluidPer - distributeNode.getTank.getFluidAmount
          val current: Int = distributeNode.getTank.getFluidAmount

          if (deltaFluidAmount > 0)
          {
            val filled: Int = distributeNode.getTank.fill(FluidUtility.getStack(networkTankFluid, deltaFluidAmount), false)
            distributeNode.getTank.fill(FluidUtility.getStack(networkTankFluid, deltaFluidAmount / 10), true)
            totalFluid -= current + filled
          }
          else
          {
            val drain: FluidStack = distributeNode.getTank.drain(Math.abs(deltaFluidAmount), false)
            distributeNode.getTank.drain(Math.abs(deltaFluidAmount / 10), true)
            if (drain != null) totalFluid -= current - drain.amount
          }

          if (deltaFluidAmount != 0) didChange = true
          distributeNode.onFluidChanged
        }
      }

      if (!didChange) needsUpdate = false
    }
  }
}