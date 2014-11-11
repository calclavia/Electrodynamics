package resonantinduction.mechanical.fluid.transport

import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.IFluidHandler
import resonant.api.grid.INodeProvider
import resonantinduction.core.prefab.node.NodePressure

/**
 * A node for the pump
 * @author Calclavia
 */
class PumpNode(parent: INodeProvider) extends NodePressure(parent)
{
  def pump: TilePump = getParent.asInstanceOf[TilePump]

  override def pressure(dir: ForgeDirection): Int =
  {
    if (dir == pump.getDirection)
    {
      return Math.max(Math.abs(pump.mechanicalNode.torque / 8000d), 2) toInt
    }

    return -Math.max(Math.abs(pump.mechanicalNode.torque / 8000d), 2).toInt
  }

  override def canConnect[B <: IFluidHandler](source: B, from: ForgeDirection): Boolean =
  {
    return super.canConnect(source, from) && (from == pump.getDirection || from == pump.getDirection.getOpposite)
  }
}
