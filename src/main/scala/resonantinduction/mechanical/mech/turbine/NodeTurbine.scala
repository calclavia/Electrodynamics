package resonantinduction.mechanical.mech.turbine

import net.minecraftforge.common.util.ForgeDirection
import resonantinduction.core.interfaces.TNodeMechanical
import resonantinduction.mechanical.mech.grid.NodeMechanical

/**
 * Turbine's Mechanical node
 * Turbines always face forward and connect from behind.
 *
 * @author Calclavia, Darkguardsman
 */
class NodeTurbine(parent: TileTurbine) extends NodeMechanical(parent)
{

  /**
   * The mechanical load
   * @return Torque in Newton meters per second
   */
  override def getLoad = 100d

  /**
   * Moment of inertia = m * r * r
   * Where "m" is the mass and "r" is the radius of the object.
   */
  override def radius(other: TNodeMechanical) = parent.multiBlockRadius * parent.multiBlockRadius

  def turbine: TileTurbine =
  {
    return getParent.asInstanceOf[TileTurbine]
  }

  override def canConnect[B](other: B, from: ForgeDirection): Boolean =
  {
    return turbine.getMultiBlock.isPrimary && other.isInstanceOf[NodeMechanical] && !(other.isInstanceOf[NodeTurbine]) && from == turbine.getDirection
  }

  /**
  override def inverseRotation(dir: ForgeDirection): Boolean =
  {
    return dir == turbine.getDirection.getOpposite
  } */
}