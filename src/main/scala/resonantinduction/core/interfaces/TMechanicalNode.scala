package resonantinduction.core.interfaces

import net.minecraftforge.common.util.ForgeDirection
import resonant.api.grid.INode
import resonant.lib.transform.vector.IVectorWorld

/**
 * Applied to any node that will act as a mechanical object
 *
 * @author Darkguardsman, Calclavia
 */
trait TMechanicalNode extends INode with IVectorWorld
{
  /**
   * Gets the radius of the gear in meters. Used to calculate torque and gear ratio for connections.
   * Is not applied to direct face to face connections
   *
   * @param side - side of the machine
   * @return radius in meters of the rotation peace
   */
  def getRadius(side: ForgeDirection, from: TMechanicalNode): Double = 0.5

  /**
   * The mechanical ratio. The higher the ratio, the more torque but less angular velocity.
   * @return A double greater than zero
   */
  def ratio = 1D

  /**
   * Gets the angular velocity of the mechanical device from a specific side
   *
   * @param from - The side of the mechanical device
   * @return Angular velocity in meters per second
   */
  def angularVelocity(from: ForgeDirection): Double

  /**
   * Gets the torque of the mechanical device from a specific side
   *
   * @param from - The side of the mechanical device
   * @return force
   */
  def torque(from: ForgeDirection): Double

  /**
   * Does the direction flip on this side for rotation
   *
   * @param from - The side of the mechanical device
   * @return boolean, true = flipped, false = not
   */
  def inverseRotation(from: ForgeDirection): Boolean = false

  /**
   * Applies rotational force and velocity to this node increasing its current rotation value
   *
   * @param source          - The source object that is applying this force
   * @param torque          - force at an angle
   * @param angularVelocity - speed of rotation
   */
  def rotate(source: AnyRef, torque: Double, angularVelocity: Double)
}