package resonantinduction.core.prefab.node

import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.{FluidStack, IFluidHandler}
import resonant.api.grid.{IUpdate, INodeProvider}

/**
 * Created by robert on 8/15/2014.
 */
class NodePressure(parent : INodeProvider, buckets : Int) extends NodeTank(parent, buckets) with IUpdate {

  def this(parent: INodeProvider)
  {
    this(parent, 1)
  }

  def update(deltaTime: Double) {
    if (!world.isRemote) {
      updatePressure
      if (getFluid != null) {
        import scala.collection.JavaConversions._
        for (entry <- connections.entrySet) {
          if (entry.getKey.isInstanceOf[INodeProvider] && (entry.getKey.asInstanceOf[INodeProvider]).getNode(classOf[NodePressure], entry.getValue.getOpposite).isInstanceOf[NodePressure]) {
            val node: NodePressure = (entry.getKey.asInstanceOf[INodeProvider]).getNode(classOf[NodePressure], entry.getValue.getOpposite).asInstanceOf[NodePressure]
            if (node.getPressure(entry.getValue.getOpposite) <= getPressure(entry.getValue)) {
            }
          }
          else if (entry.getKey.isInstanceOf[INodeProvider] && (entry.getKey.asInstanceOf[INodeProvider]).getNode(classOf[NodeTank], entry.getValue.getOpposite).isInstanceOf[NodeTank]) {
            val node: NodeTank = (entry.getKey.asInstanceOf[INodeProvider]).getNode(classOf[NodeTank], entry.getValue.getOpposite).asInstanceOf[NodeTank]
            if (node.canFill(entry.getValue.getOpposite, getFluid.getFluid)) {
              val stack: FluidStack = drain(Integer.MAX_VALUE, false)
              val drained: Int = node.fill(stack, true)
              drain(drained, true)
            }
          }
          else if (entry.getKey.isInstanceOf[IFluidHandler]) {
            if ((entry.getKey.asInstanceOf[IFluidHandler]).canFill(entry.getValue.getOpposite, getFluid.getFluid)) {
              val stack: FluidStack = drain(Integer.MAX_VALUE, false)
              val drained: Int = (entry.getKey.asInstanceOf[IFluidHandler]).fill(entry.getValue.getOpposite, stack, true)
              drain(drained, true)
            }
          }
        }
      }
    }
  }

  def canUpdate: Boolean = {
    return true
  }

  def continueUpdate: Boolean = {
    return true
  }

  protected def updatePressure {
    var totalPressure: Int = 0
    val connectionSize: Int = connections.size
    var minPressure: Int = 0
    var maxPressure: Int = 0
    import scala.collection.JavaConversions._
    for (entry <- connections.entrySet) {
      if (entry.getKey.isInstanceOf[INodeProvider] && (entry.getKey.asInstanceOf[INodeProvider]).getNode(classOf[NodePressure], entry.getValue.getOpposite).isInstanceOf[NodePressure]) {
        val node: NodePressure = (entry.getKey.asInstanceOf[INodeProvider]).getNode(classOf[NodePressure], entry.getValue.getOpposite).asInstanceOf[NodePressure]
        val pressure: Int = node.getPressure(entry.getValue.getOpposite)
        minPressure = Math.min(pressure, minPressure)
        maxPressure = Math.max(pressure, maxPressure)
        totalPressure += pressure
      }
    }
    if (connectionSize == 0) {
      setPressure(0)
    }
    else {
      if (minPressure < 0) {
        minPressure += 1
      }
      if (maxPressure > 0) {
        maxPressure -= 1
      }
      setPressure(Math.max(minPressure, Math.min(maxPressure, totalPressure / connectionSize + Integer.signum(totalPressure))))
    }
  }

  def getPressure(direction: ForgeDirection): Int = {
    return pressure
  }

  def setPressure(pressure: Int) {
    this.pressure = pressure
  }

  private var pressure: Int = 0
}