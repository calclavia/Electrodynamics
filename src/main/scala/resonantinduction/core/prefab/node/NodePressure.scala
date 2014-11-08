package resonantinduction.core.prefab.node

import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.{FluidContainerRegistry, IFluidHandler}
import resonant.api.grid.{INodeProvider, IUpdate}
import resonant.lib.grid.UpdateTicker
import resonant.lib.prefab.fluid.NodeFluid

import scala.collection.convert.wrapAll._

/**
 * A node for fluid that moves based on pressure
 *
 * @param parent Parent(TileEntity or Multipart) that contains this node
 * @param volume Amount of fluid in liters
 *
 * @author Calclavia
 */
class NodePressure(parent: INodeProvider, volume: Int = FluidContainerRegistry.BUCKET_VOLUME) extends NodeFluid(parent, volume) with IUpdate
{
  var maxFlowRate = 20
  var maxPressure = 100
  private var _pressure: Int = 0

  UpdateTicker.addUpdater(this)

  def update(deltaTime: Double)
  {
    println(world)
    if (!world.isRemote)
    {
      updatePressure()
      distribute(deltaTime)
    }
  }

  def distribute(deltaTime: Double)
  {
    val flowRate = (maxFlowRate * deltaTime).toInt

    directionMap.foreach
    {
      case (handler: IFluidHandler, dir: ForgeDirection) =>
      {
        if (handler.isInstanceOf[NodePressure])
        {
          //"A" is this node. "B" is the other node
          //It's another pressure node
          val otherNode = handler.asInstanceOf[NodePressure]
          val pressureA = pressure(dir)
          val pressureB = otherNode.pressure(dir.getOpposite)

          //High pressure to low
          if (pressureA >= pressureB)
          {
            val tankA = getPrimaryTank

            if (tankA != null)
            {
              val fluidA = tankA.getFluid

              if (fluidA != null)
              {
                val amountA = fluidA.amount

                if (amountA > 0)
                {
                  val tankB = otherNode.getPrimaryTank

                  if (tankB != null)
                  {
                    doDistribute(dir, this, otherNode, flowRate)
                  }
                }
              }
            }
          }
        }
        else
        {
          //It's a fluid handler.
          val pressure = this.pressure(dir)
          val tankPressure = 0
          val sourceTank = getPrimaryTank
          val transferAmount = (Math.max(pressure, tankPressure) - Math.min(pressure, tankPressure)) * flowRate

          if (pressure > tankPressure)
          {
            if (sourceTank.getFluidAmount > 0 && transferAmount > 0)
            {
              val drainStack = drain(dir.getOpposite, transferAmount, false)
              drain(dir.getOpposite, handler.fill(dir.getOpposite, drainStack, true), true)
            }
          }
          else if (pressure < tankPressure)
          {
            if (transferAmount > 0)
            {
              val drainStack = handler.drain(dir.getOpposite, transferAmount, false)
              if (drainStack != null)
              {
                handler.drain(dir.getOpposite, fill(dir.getOpposite, drainStack, true), true)
              }
            }
          }
        }
      }
    }
  }

  protected def doDistribute(dir: ForgeDirection, nodeA: NodePressure, nodeB: NodePressure, flowRate: Int)
  {
    val tankA = nodeA.getPrimaryTank
    val tankB = nodeB.getPrimaryTank
    val pressureA = nodeA.pressure(dir)
    val pressureB = nodeB.pressure(dir.getOpposite)
    val amountA = tankA.getFluidAmount
    val amountB = tankB.getFluidAmount

    var quantity = Math.max(if (pressureA > pressureB) (pressureA - pressureB) * flowRate else Math.min((amountA - amountB) / 2, flowRate), Math.min((amountA - amountB) / 2, flowRate))
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

  protected def updatePressure()
  {
    var totalPressure = 0
    val connectionSize = connections.size
    var minPressure = 0
    var maxPressure = 0

    directionMap.foreach
    {
      case (handler: IFluidHandler, dir: ForgeDirection) =>
      {
        if (handler.isInstanceOf[NodePressure])
        {
          val node = handler.asInstanceOf[NodePressure]
          val pressure = node.pressure(dir.getOpposite)
          minPressure = Math.min(pressure, minPressure)
          maxPressure = Math.max(pressure, maxPressure)
          totalPressure += pressure
        }
      }
    }

    if (connectionSize == 0)
    {
      pressure = 0
    }
    else
    {
      if (minPressure < 0)
        minPressure += 1
      if (maxPressure > 0)
        maxPressure -= 1

      pressure = Math.max(minPressure, Math.min(maxPressure, totalPressure / connectionSize + Integer.signum(totalPressure)))
    }
  }

  def pressure: Int = _pressure

  def pressure(direction: ForgeDirection): Int = _pressure

  def pressure_=(pressure: Int)
  {
    this._pressure = pressure
  }

  def canUpdate = !isInvalid

  def continueUpdate = !isInvalid
}