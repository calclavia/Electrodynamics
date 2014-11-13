package resonantinduction.mechanical.mech.grid

import net.minecraftforge.common.util.ForgeDirection
import resonant.api.grid.INodeProvider
import resonant.lib.grid.GridNode
import resonant.lib.grid.node.NodeGrid
import resonant.lib.transform.vector.IVectorWorld
import resonantinduction.core.interfaces.TMechanicalNode
import resonantinduction.core.prefab.node.TMultipartNode

import scala.beans.BeanProperty

/**
 * Prefab node for the mechanical system used by almost ever mechanical object in Resonant Induction. Handles connections to other tiles, and shares power with them
 *
 * @author Calclavia, Darkguardsman
 */
class NodeMechanical(parent: INodeProvider) extends NodeGrid[NodeMechanical](parent) with TMultipartNode[NodeMechanical] with TMechanicalNode with IVectorWorld
{
  var torque = 0D
  var angularVelocity = 0D

  /**
   * Buffer values used by the grid to transfer mechanical energy.
   */
  protected[grid] var bufferTorque = 0D

  /**
   * The mechanical load
   */
  var load = 10D

  /**
   * Angle calculations
   */
  protected var prevTime = 0L
  var prevAngle = 0D

  /**
   * The amount of angle in radians displaced. This is used to align the gear teeth.
   */
  var angleDisplacement = 0D

  /**
   * Events
   */
  @BeanProperty
  var onTorqueChanged: () => Unit = () => ()
  @BeanProperty
  var onVelocityChanged: () => Unit = () => ()

  /**
   * An arbitrary angle value computed based on velocity
   * @return The angle in radians
   */
  def angle: Double =
  {
    val deltaTime = (System.currentTimeMillis() - prevTime) / 1000D
    prevTime = System.currentTimeMillis()
    prevAngle = (prevAngle + deltaTime * angularVelocity) % (2 * Math.PI)
    return prevAngle
  }

  @deprecated
  protected def revolve()
  {

  }

  //Moment of inertia = m * r ^ 2
  def momentOfInertia = 1d

  /**
   * The mechanical load
   * @return Torque in Newton meters per second
   */
  def getLoad = load

  override def rotate(torque: Double)
  {
    bufferTorque += torque
  }

  /**
   * The percentage of angular velocity loss every second
   */
  def getAngularVelocityLoad: Double = getLoad

  //TODO: Create new grids automatically?
  def power: Double = torque * angularVelocity

  def getMechanicalGrid: MechanicalGrid = super.grid.asInstanceOf[MechanicalGrid]

  override def newGrid: GridNode[NodeMechanical] = new MechanicalGrid

  override def isValidConnection(other: AnyRef): Boolean = other.isInstanceOf[NodeMechanical]
}