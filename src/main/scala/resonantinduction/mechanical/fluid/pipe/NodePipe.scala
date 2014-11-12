package resonantinduction.mechanical.fluid.pipe

import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.IFluidHandler
import resonant.api.grid.INodeProvider
import resonantinduction.core.prefab.node.{NodePressure, TMultipartNode}
import resonantinduction.core.prefab.part.connector.TColorable

/**
 * Pressure node for the pipe
 *
 * @author Calclavia, Darkguardsman
 */
class NodePipe(parent: PartPipe) extends NodePressure(parent) with TMultipartNode[IFluidHandler]
{
  def pipe: PartPipe = getParent.asInstanceOf[PartPipe]

  override def rebuild()
  {
    for (dir <- ForgeDirection.VALID_DIRECTIONS)
    {
      val tile = toVectorWorld.add(dir).getTileEntity(world)

      if (tile.isInstanceOf[IFluidHandler])
      {
        if (tile.isInstanceOf[INodeProvider])
        {
          val check = tile.asInstanceOf[INodeProvider].getNode(classOf[NodePressure], dir.getOpposite)

          if (check.isInstanceOf[NodePressure] && canConnect(check, dir) && check.asInstanceOf[NodePressure].canConnect(this, dir.getOpposite))
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
}