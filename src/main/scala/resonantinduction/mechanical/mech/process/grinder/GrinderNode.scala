package resonantinduction.mechanical.mech.process.grinder

import net.minecraftforge.common.util.ForgeDirection
import resonantinduction.mechanical.mech.grid.MechanicalNode

/** Node just for the grinder
  *
  * @author Darkguardsman */
class GrinderNode(parent: TileGrindingWheel) extends MechanicalNode(parent: TileGrindingWheel)
{
  def grider: TileGrindingWheel =
  {
    return getParent.asInstanceOf[TileGrindingWheel]
  }

  override def canConnect[B](other: B, from: ForgeDirection): Boolean =
  {
    if (grider.getDirection == ForgeDirection.UP || grider.getDirection == ForgeDirection.DOWN)
    {
      return grider.getDirection == from || grider.getDirection.getOpposite == from
    }
    return grider.getDirection != from && grider.getDirection.getOpposite != from
  }


  override def inverseRotation(dir: ForgeDirection): Boolean =
  {
    return !(dir.offsetX > 0 || dir.offsetZ < 0 || dir.offsetY < 0)
  }
}