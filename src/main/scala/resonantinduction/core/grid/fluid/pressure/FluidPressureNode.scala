package resonantinduction.core.grid.fluid.pressure

import java.util.{HashMap, Iterator}

import codechicken.multipart.TMultiPart
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.{FluidStack, FluidTank, IFluidHandler}
import resonantinduction.core.grid.MultipartNode
import universalelectricity.api.core.grid.INodeProvider
import universalelectricity.core.grid.Node
import universalelectricity.core.transform.vector.Vector3

class FluidPressureNode(parent: INodeProvider) extends MultipartNode[FluidPressureNode](parent)
{
  var maxFlowRate: Int = 20
  var maxPressure: Int = 100
  protected var connectionMap: Byte = Byte.parseByte("111111", 2)
  private var pressure: Int = 0

  def setConnection(connectionMap: Byte): FluidPressureNode =
  {
    this.connectionMap = connectionMap
    return this
  }

  def update(deltaTime: Double)
  {
    if (!world.isRemote)
    {
      updatePressure
      distribute
    }
  }

  protected def updatePressure
  {
    var totalPressure: Int = 0
    var findCount: Int = 0
    var minPressure: Int = 0
    var maxPressure: Int = 0
    val it: Iterator[Map.Entry[AnyRef, ForgeDirection]] = new HashMap[_, _](connections).entrySet.iterator
    while (it.hasNext)
    {
      val entry: Map.Entry[AnyRef, ForgeDirection] = it.next
      val obj: AnyRef = entry.getKey
      if (obj.isInstanceOf[FluidPressureNode])
      {
        val pressure: Int = (obj.asInstanceOf[FluidPressureNode]).getPressure(entry.getValue.getOpposite)
        minPressure = Math.min(pressure, minPressure)
        maxPressure = Math.max(pressure, maxPressure)
        totalPressure += pressure
        findCount += 1
      }
    }
    if (findCount == 0)
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
      setPressure(Math.max(minPressure, Math.min(maxPressure, totalPressure / findCount + Integer.signum(totalPressure))))
    }
  }

  def distribute
  {
    val it: Iterator[Map.Entry[AnyRef, ForgeDirection]] = new HashMap[_, _](connections).entrySet.iterator
    while (it.hasNext)
    {
      val entry: Map.Entry[_, ForgeDirection] = it.next
      val obj: AnyRef = entry.getKey
      val dir: ForgeDirection = entry.getValue

      if (obj.isInstanceOf[FluidPressureNode])
      {
        val otherNode: FluidPressureNode = obj.asInstanceOf[FluidPressureNode]
        val pressureA: Int = getPressure(dir)
        val pressureB: Int = otherNode.getPressure(dir.getOpposite)
        if (pressureA >= pressureB)
        {
          val tankA: FluidTank = parent.getPressureTank
          if (tankA != null)
          {
            val fluidA: FluidStack = tankA.getFluid
            if (fluidA != null)
            {
              val amountA: Int = fluidA.amount
              if (amountA > 0)
              {
                val tankB: FluidTank = otherNode.parent.getPressureTank
                if (tankB != null)
                {
                  val amountB: Int = tankB.getFluidAmount
                  var quantity: Int = Math.max(if (pressureA > pressureB) (pressureA - pressureB) * getMaxFlowRate else 0, Math.min((amountA - amountB) / 2, getMaxFlowRate))
                  quantity = Math.min(Math.min(quantity, tankB.getCapacity - amountB), amountA)
                  if (quantity > 0)
                  {
                    val drainStack: FluidStack = parent.drain(dir.getOpposite, quantity, false)
                    if (drainStack != null && drainStack.amount > 0)
                    {
                      parent.drain(dir.getOpposite, otherNode.parent.fill(dir, drainStack, true), true)
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
        val sourceTank: FluidTank = parent.getPressureTank
        val transferAmount: Int = (Math.max(pressure, tankPressure) - Math.min(pressure, tankPressure)) * getMaxFlowRate
        if (pressure > tankPressure)
        {
          if (sourceTank.getFluidAmount > 0 && transferAmount > 0)
          {
            val drainStack: FluidStack = parent.drain(dir.getOpposite, transferAmount, false)
            parent.drain(dir.getOpposite, fluidHandler.fill(dir.getOpposite, drainStack, true), true)
          }
        }
        else if (pressure < tankPressure)
        {
          if (transferAmount > 0)
          {
            val drainStack: FluidStack = fluidHandler.drain(dir.getOpposite, transferAmount, false)
            if (drainStack != null)
            {
              fluidHandler.drain(dir.getOpposite, parent.fill(dir.getOpposite, drainStack, true), true)
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
    connections.clear
    for (dir <- ForgeDirection.VALID_DIRECTIONS)
    {
      val tile: TileEntity = position.add(dir).getTileEntity(world)
      if (tile.isInstanceOf[Nothing])
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