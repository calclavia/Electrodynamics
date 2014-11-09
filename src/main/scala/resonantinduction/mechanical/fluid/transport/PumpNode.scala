package resonantinduction.mechanical.fluid.transport

import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.IFluidHandler
import resonantinduction.core.prefab.node.NodePressure
import resonant.api.grid.INodeProvider

/**
 * A node for the pump
 * @author Calclavia
 */
class PumpNode(parent: INodeProvider) extends NodePressure(parent)
{
  def pump: TilePump = getParent.asInstanceOf[TilePump]

  override def pressure(dir: ForgeDirection): Int =
  {
    if (pump.mechanicalNode.getPower > 0)
    {
      if (dir == pump.getDirection)
      {
        return Math.max(Math.abs(pump.mechanicalNode.torque(ForgeDirection.UNKNOWN) / 8000d), 2).asInstanceOf[Int]
      }
      else if (dir == pump.getDirection.getOpposite)
      {
        return -Math.max(Math.abs(pump.mechanicalNode.torque(ForgeDirection.UNKNOWN) / 8000d), 2).asInstanceOf[Int]
      }
    }
    return 0
  }

  override def canConnect[B <: IFluidHandler](source: B, from: ForgeDirection): Boolean =
  {
    return super.canConnect(source, from) && (from == pump.getDirection || from == pump.getDirection.getOpposite)
  }
}
