package resonantinduction.core.grid.fluid.pressure

import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.{FluidStack, FluidTank, IFluidHandler}
import resonantinduction.core.grid.MultipartNode
import resonantinduction.core.grid.fluid.distribution.TFluidDistributor
import universalelectricity.api.core.grid.INodeProvider

import scala.collection.convert.wrapAll._

class FluidPressureNode(parent: INodeProvider) extends MultipartNode[AnyRef](parent)
{
  var maxFlowRate: Int = 20
  var maxPressure: Int = 100
  protected var connectionMap: Byte = java.lang.Byte.parseByte("111111", 2)
  private var pressure = 0

  def genericParent = parent.asInstanceOf[TFluidDistributor]

  def setConnection(connectionMap: Byte): FluidPressureNode =
  {
    this.connectionMap = connectionMap
    return this
  }

  override def update(deltaTime: Double)
  {
    if (!world.isRemote)
    {
      updatePressure
      distribute
    }
  }

  protected def updatePressure()
  {
    var totalPressure = 0
    val connectionSize = connections.size
    var minPressure = 0
    var maxPressure = 0

    connections
    .filterKeys(_.isInstanceOf[FluidPressureNode])
    .map({ case (k: AnyRef, v: ForgeDirection) => (k.asInstanceOf[FluidPressureNode], v) })
    .foreach
    {
      case (node: FluidPressureNode, dir: ForgeDirection) =>
      {
        val pressure = node.getPressure(dir.getOpposite)
        minPressure = Math.min(pressure, minPressure)
        maxPressure = Math.max(pressure, maxPressure)
        totalPressure += pressure
      }
    }

    if (connectionSize == 0)
    {
      setPressure(0)
    }
    else
    {
      if (minPressure < 0)
      {
        minPressure += 1
      }
      if (maxPressure > 0)
      {
        maxPressure -= 1
      }

      setPressure(Math.max(minPressure, Math.min(maxPressure, totalPressure / connectionSize + Integer.signum(totalPressure))))
    }
  }

  def distribute
  {
    connections.foreach
    {
      case (obj: AnyRef, dir: ForgeDirection) =>
      {
        if (obj.isInstanceOf[FluidPressureNode])
        {
          val otherNode: FluidPressureNode = obj.asInstanceOf[FluidPressureNode]
          val pressureA = getPressure(dir)
          val pressureB = otherNode.getPressure(dir.getOpposite)

          /**
           * High pressure from this node flows to low pressure nodes.
           */
          if (pressureA >= pressureB)
          {
            val tankA = genericParent.getTank
            if (tankA != null)
            {
              val fluidA: FluidStack = tankA.getFluid
              if (fluidA != null)
              {
                val amountA: Int = fluidA.amount
                if (amountA > 0)
                {
                  val tankB: FluidTank = otherNode.genericParent.getTank
                  if (tankB != null)
                  {
                    val amountB: Int = tankB.getFluidAmount
                    var quantity: Int = Math.max(if (pressureA > pressureB) (pressureA - pressureB) * getMaxFlowRate else 0, Math.min((amountA - amountB) / 2, getMaxFlowRate))
                    quantity = Math.min(Math.min(quantity, tankB.getCapacity - amountB), amountA)
                    if (quantity > 0)
                    {
                      val drainStack: FluidStack = genericParent.drain(dir.getOpposite, quantity, false)
                      if (drainStack != null && drainStack.amount > 0)
                      {
                        genericParent.drain(dir.getOpposite, otherNode.genericParent.fill(dir, drainStack, true), true)
                      }
                    }
                  }
                }
              }
            }
          }
        }
        else if (obj.isInstanceOf[IFluidHandler])
        {
          val fluidHandler: IFluidHandler = obj.asInstanceOf[IFluidHandler]
          val pressure: Int = getPressure(dir)
          val tankPressure: Int = if (fluidHandler.isInstanceOf[INodeProvider]) (fluidHandler.asInstanceOf[INodeProvider]).getNode(classOf[FluidPressureNode], dir.getOpposite).getPressure(dir.getOpposite) else 0
          val sourceTank = genericParent.getTank
          val transferAmount: Int = (Math.max(pressure, tankPressure) - Math.min(pressure, tankPressure)) * getMaxFlowRate
          if (pressure > tankPressure)
          {
            if (sourceTank.getFluidAmount > 0 && transferAmount > 0)
            {
              val drainStack: FluidStack = genericParent.drain(dir.getOpposite, transferAmount, false)
              genericParent.drain(dir.getOpposite, fluidHandler.fill(dir.getOpposite, drainStack, true), true)
            }
          }
          else if (pressure < tankPressure)
          {
            if (transferAmount > 0)
            {
              val drainStack: FluidStack = fluidHandler.drain(dir.getOpposite, transferAmount, false)
              if (drainStack != null)
              {
                fluidHandler.drain(dir.getOpposite, genericParent.fill(dir.getOpposite, drainStack, true), true)
              }
            }
          }
        }
      }
    }
  }

  def getMaxFlowRate: Int =
  {
    return maxFlowRate
  }

  def setPressure(newPressure: Int)
  {
    if (newPressure > 0)
    {
      pressure = Math.min(maxPressure, newPressure)
    }
    else
    {
      pressure = Math.max(-maxPressure, newPressure)
    }
  }

  def getPressure(dir: ForgeDirection): Int =
  {
    return pressure
  }

  /**
   * Recache the connections. This is the default connection implementation.
   */
  override def doRecache
  {
    connections.clear()

    for (dir <- ForgeDirection.VALID_DIRECTIONS)
    {
      val tile = (position + dir).getTileEntity

      if (tile.isInstanceOf[INodeProvider])
      {
        val check: FluidPressureNode = (tile.asInstanceOf[INodeProvider]).getNode(classOf[FluidPressureNode], dir.getOpposite)
        if (check != null && canConnect(dir, check) && check.canConnect(dir.getOpposite, this))
        {
          connections.put(check, dir)
        }
      }
    }
  }

  override def canConnect(from: ForgeDirection, source: AnyRef): Boolean =
  {
    return (source.isInstanceOf[FluidPressureNode]) && (connectionMap & (1 << from.ordinal)) != 0
  }

  override def load(nbt: NBTTagCompound)
  {
    super.load(nbt)
    pressure = nbt.getInteger("pressure")
  }

  override def save(nbt: NBTTagCompound)
  {
    super.save(nbt)
    nbt.setInteger("pressure", pressure)
  }
}