package resonantinduction.mechanical.fluid.transport

import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.IFluidHandler
import resonant.api.tile.INodeProvider
import resonantinduction.core.prefab.node.NodeFluidPressure

/**
 * A node for the pump
 * @author Calclavia
 */
class PumpNode(parent: INodeProvider) extends NodeFluidPressure(parent)
{
  def pump: TilePump = getParent.asInstanceOf[TilePump]

  override def pressure(dir: ForgeDirection): Int =
  {
    if (pump.mechanicalNode.power > 0)
    {
      if (dir == pump.getDirection)
      {
        return Math.max(Math.log(Math.abs(pump.mechanicalNode.torque) + 1), 2).toInt
      }

      return -Math.max(Math.log(Math.abs(pump.mechanicalNode.torque) + 1), 2).toInt
    }

    return 0
  }

  override def canConnect[B <: IFluidHandler](source: B, from: ForgeDirection): Boolean =
  {
    return super.canConnect(source, from) && (from == pump.getDirection || from == pump.getDirection.getOpposite)
  }
}
