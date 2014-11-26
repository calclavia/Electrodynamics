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
  override def getLoad = 100 * parent.multiBlockRadius * parent.multiBlockRadius

  /**
   * Moment of inertia = m * r * r
   * Where "m" is the mass and "r" is the radius of the object.
   */
  override def radius(other: TNodeMechanical): Double =
  {
    val deltaPos = other.asInstanceOf[NodeMechanical].toVectorWorld - toVectorWorld

    if (deltaPos.normalize.toForgeDirection == parent.getDirection)
      return super.radius(other)

    return parent.multiBlockRadius
  }

  def turbine: TileTurbine = getParent.asInstanceOf[TileTurbine]

  override def canConnect[B <: NodeMechanical](other: B, from: ForgeDirection): Boolean =
  {
    return turbine.getMultiBlock.isPrimary && other.isInstanceOf[NodeMechanical] && !other.isInstanceOf[NodeTurbine] && canConnect(from)
  }

  override def canConnect(from: ForgeDirection) = from == turbine.getDirection
}