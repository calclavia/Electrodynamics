package edx.mechanical.fluid.pipe

import edx.core.prefab.node.{NodeFluidPressure, TMultipartNode}
import edx.core.prefab.part.connector.TColorable
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.IFluidHandler
import resonantengine.api.graph.INodeProvider

/**
 * Pressure node for the pipe
 *
 * @author Calclavia, Darkguardsman
 */
class NodePipe(parent: PartPipe) extends NodeFluidPressure(parent) with TMultipartNode[IFluidHandler]
{
  override def rebuild()
  {
    for (dir <- ForgeDirection.VALID_DIRECTIONS)
    {
      val tile = position.add(dir).getTileEntity(world)

      if (tile.isInstanceOf[IFluidHandler])
      {
        if (tile.isInstanceOf[INodeProvider])
        {
          val check = tile.asInstanceOf[INodeProvider].getNode(classOf[NodeFluidPressure], dir.getOpposite)

          if (check.isInstanceOf[NodeFluidPressure] && canConnect(check, dir) && check.asInstanceOf[NodeFluidPressure].canConnect(this, dir.getOpposite))
          {
            connect(check, dir)
          }
        }
        else if (tile.isInstanceOf[IFluidHandler] && canConnect(tile.asInstanceOf[IFluidHandler], dir))
        {
          connect(tile.asInstanceOf[IFluidHandler], dir)
        }
      }
    }
  }

  override def canConnect[B <: IFluidHandler](source: B, from: ForgeDirection): Boolean =
  {
    if (!pipe.isBlockedOnSide(from))
    {
      if (source.isInstanceOf[NodePipe])
      {
        val otherNode = source.asInstanceOf[NodePipe]

        val otherPipe = otherNode.pipe

        if (!otherPipe.isBlockedOnSide(from.getOpposite) && pipe.material == otherPipe.material)
        {
          return pipe.getColor == otherPipe.getColor || (pipe.getColor == TColorable.defaultColor || otherPipe.getColor == TColorable.defaultColor)
        }

        return false
      }

      return super.canConnect(source, from) || source.isInstanceOf[IFluidHandler]
    }

    return false
  }

  def pipe: PartPipe = getParent.asInstanceOf[PartPipe]
}