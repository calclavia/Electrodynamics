package resonantinduction.core.grid.fluid.distribution

import java.util.HashMap

import net.minecraft.tileentity.TileEntity
import net.minecraftforge.fluids.FluidStack
import resonant.lib.utility.FluidUtility

import scala.collection.mutable

/** Network that handles connected tanks
  *
  * @author DarkGuardsman */
class TankNetwork extends FluidDistributionGrid
{
  needsUpdate = true

  override def update()
  {
    val networkTankFluid: FluidStack = tank.getFluid
    var lowestY: Int = 255
    var highestY: Int = 0

    if (getNodes.size > 0)
    {
      var totalFluid: Int = if (networkTankFluid != null) networkTankFluid.amount else 0
      val heightCount: HashMap[Integer, Integer] = new HashMap[_, _]
      val heightPriorityQueue = new mutable.PriorityQueue[IFluidDistributor]()(Ordering.by(
        (a: IFluidDistributor, b: IFluidDistributor) =>
        {
          if (networkTankFluid != null && networkTankFluid.getFluid.isGaseous) return 0
          val wa: TileEntity = a.asInstanceOf[TileEntity]
          val wb: TileEntity = b.asInstanceOf[TileEntity]

          if (wa.yCoord > wb.yCoord)
            a
          else
            b
        }
      ))

      import scala.collection.JavaConversions._
      for (connector <- this.getNodes)
      {
        if (connector.isInstanceOf[TileEntity])
        {
          val yCoord: Int = (connector.asInstanceOf[TileEntity]).yCoord
          if (yCoord < lowestY)
          {
            lowestY = yCoord
          }
          if (yCoord > highestY)
          {
            highestY = yCoord
          }
          heightPriorityQueue.add(connector)
          heightCount.put(yCoord, if (heightCount.containsKey(yCoord)) heightCount.get(yCoord) + 1 else 1)
        }
      }
      var didChange: Boolean = false
      while (!heightPriorityQueue.isEmpty)
      {
        val distributeNode: IFluidDistributor = heightPriorityQueue.poll
        val yCoord: Int = (distributeNode.asInstanceOf[TileEntity]).yCoord
        var connectorCount: Int = heightCount.get(yCoord)
        if (totalFluid <= 0)
        {
          distributeNode.getInternalTank.setFluid(null)
          distributeNode.onFluidChanged
          continue //todo: continue is not supported
        }
        val fluidPer: Int = totalFluid / connectorCount
        val deltaFluidAmount: Int = fluidPer - distributeNode.getInternalTank.getFluidAmount
        val current: Int = distributeNode.getInternalTank.getFluidAmount
        if (deltaFluidAmount > 0)
        {
          val filled: Int = distributeNode.getInternalTank.fill(FluidUtility.getStack(networkTankFluid, deltaFluidAmount), false)
          distributeNode.getInternalTank.fill(FluidUtility.getStack(networkTankFluid, deltaFluidAmount / 10), true)
          totalFluid -= current + filled
        }
        else
        {
          val drain: FluidStack = distributeNode.getInternalTank.drain(Math.abs(deltaFluidAmount), false)
          distributeNode.getInternalTank.drain(Math.abs(deltaFluidAmount / 10), true)
          if (drain != null) totalFluid -= current - drain.amount
        }
        if (deltaFluidAmount != 0) didChange = true
        if (connectorCount > 1) ({connectorCount -= 1; connectorCount + 1 })
        heightCount.put(yCoord, connectorCount)
        distributeNode.onFluidChanged
      }
      if (!didChange) needsUpdate = false
    }
  }
}