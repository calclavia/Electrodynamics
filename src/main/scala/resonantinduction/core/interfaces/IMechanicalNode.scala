package resonantinduction.core.interfaces

import net.minecraftforge.common.util.ForgeDirection
import resonant.api.grid.INode
import resonant.lib.transform.vector.IVectorWorld

/**
 * Applied to any node that will act as a mechanical object
 *
 * @author Darkguardsman
 */
abstract trait IMechanicalNode extends INode with IVectorWorld {
  /**
   * Gets the radius of the gear in meters. Used to calculate torque and gear ratio for connections.
   * Is not applied to direct face to face connections
   *
   * @param side - side of the machine
   * @return radius in meters of the rotation peace
   */
  def getRadius(side: ForgeDirection, `with`: IMechanicalNode): Double = 0.5

  /**
   * The Rotational speed of the object
   *
   * @param side - side of the machine
   * @return speed in meters per second
   */
  def getAngularSpeed(side: ForgeDirection): Double

  /**
   * Force applied from this side
   *
   * @param side - side of the machine
   * @return force
   */
  def getForce(side: ForgeDirection): Double

  /**
   * Does the direction flip on this side for rotation
   *
   * @param side - side of the machine
   * @return boolean, true = flipped, false = not
   */
  def inverseRotation(side: ForgeDirection): Boolean = false

  /**
   * Applies rotational force and velocity to this node increasing its current rotation value
   *
   * @param source          - should not be null
   * @param torque          - force at an angle
   * @param angularVelocity - speed of rotation
   */
  def apply(source: AnyRef, torque: Double, angularVelocity: Double)
}