package edx.mechanical.fluid.transport

import edx.core.prefab.node.NodeFluidPressure
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.IFluidHandler
import resonantengine.api.graph.INodeProvider

/**
 * A node for the pump
 * @author Calclavia
 */
class NodePump(parent: INodeProvider) extends NodeFluidPressure(parent)
{
  override def pressure(dir: ForgeDirection): Int =
  {
    if (pump.mechanicalNode.power > 0)
    {
      if (dir == pump.getDirection)
      {
        return Math.max(Math.log(Math.abs(pump.mechanicalNode.torque) + 1) * 3, 2).toInt
      }

      return -Math.max(Math.log(Math.abs(pump.mechanicalNode.torque) + 1) * 3, 2).toInt
    }

    return 0
  }

  def pump: TilePump = getParent.asInstanceOf[TilePump]

  override def canConnect[B <: IFluidHandler](source: B, from: ForgeDirection): Boolean =
  {
    return super.canConnect(source, from) && (from == pump.getDirection || from == pump.getDirection.getOpposite)
  }
}
