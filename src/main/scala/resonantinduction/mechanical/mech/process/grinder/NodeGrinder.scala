package resonantinduction.mechanical.mech.process.grinder

import net.minecraftforge.common.util.ForgeDirection
import resonantinduction.core.interfaces.TNodeMechanical
import resonantinduction.mechanical.mech.grid.NodeMechanical

/**
 * @author Calclavia
 */
class NodeGrinder(parent: TileGrindingWheel) extends NodeMechanical(parent: TileGrindingWheel)
{
  override def canConnect[B <: NodeMechanical](other: B, from: ForgeDirection): Boolean =
  {
    if (parent.getDirection == ForgeDirection.UP || parent.getDirection == ForgeDirection.DOWN)
    {
      return parent.getDirection == from || parent.getDirection.getOpposite == from
    }
    return parent.getDirection != from && parent.getDirection.getOpposite != from
  }

  override def inverseRotation(other: TNodeMechanical) = (toVector3 - other.asInstanceOf[NodeMechanical].toVector3).toArray.sum > 0
}