package resonantinduction.mechanical.fluid.pipe

import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.IFluidHandler
import resonant.lib.wrapper.BitmaskWrapper._
import resonantinduction.core.prefab.node.{NodePressure, TMultipartNode}
import resonantinduction.core.prefab.part.connector.TColorable
import resonant.api.grid.INodeProvider

/**
 * Pressure node for the pipe
 *
 * @author Calclavia, Darkguardsman
 */
class PipePressureNode(parent: PartPipe) extends NodePressure(parent) with TMultipartNode
{
  def pipe: PartPipe = getParent.asInstanceOf[PartPipe]

  override def reconstruct()
  {
    connections.clear()

    if (world != null)
    {
      val previousConnections = pipe.connectionMask
      pipe.connectionMask = 0

      for (dir <- ForgeDirection.VALID_DIRECTIONS)
      {
        val tile = position.add(dir).getTileEntity(world)

        if (tile.isInstanceOf[IFluidHandler])
        {
          if (tile.isInstanceOf[INodeProvider])
          {
            val check = tile.asInstanceOf[INodeProvider].getNode(classOf[NodePressure], dir.getOpposite)

            if (check.isInstanceOf[NodePressure] && canConnect(check,dir) && check.asInstanceOf[NodePressure].canConnect(this, dir.getOpposite))
            {
              pipe.connectionMask = pipe.connectionMask.openMask(dir)
              connections.put(check, dir)
            }
          }
          else if (canConnect(tile,dir))
          {
            pipe.connectionMask = pipe.connectionMask.openMask(dir)
            connections.put(tile, dir)
          }
        }
      }

      if (!world.isRemote && previousConnections != pipe.connectionMask)
      {
        pipe.sendConnectionUpdate
      }
    }
  }

  override def canConnect(source: AnyRef, from: ForgeDirection): Boolean =
  {
    if (!pipe.isBlockedOnSide(from))
    {
      if (source.isInstanceOf[PipePressureNode])
      {
        val otherNode = source.asInstanceOf[PipePressureNode]

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

  override def toString: String = getClass.getSimpleName + hashCode
}