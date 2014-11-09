package resonantinduction.mechanical.mech.grid

import net.minecraftforge.common.util.ForgeDirection
import resonant.api.grid.INodeProvider
import resonant.lib.grid.GridNode
import resonant.lib.grid.node.NodeGrid
import resonant.lib.transform.vector.IVectorWorld
import resonantinduction.core.interfaces.TMechanicalNode
import resonantinduction.core.prefab.node.TMultipartNode

/**
 * Prefab node for the mechanical system used by almost ever mechanical object in Resonant Induction. Handles connections to other tiles, and shares power with them
 *
 * @author Calclavia, Darkguardsman
 */
class MechanicalNode(parent: INodeProvider) extends NodeGrid[MechanicalNode](parent) with TMultipartNode[MechanicalNode] with TMechanicalNode with IVectorWorld
{
  /**
   * Allows the node to share its power with other nodes
   */
  var torque = 0D
  var angularVelocity = 0D

  protected[grid] var bufferTorque = 0D
  protected[grid] var bufferAngle = 0D

  var load = 0.2

  /**
   * Angle calculations
   */
  private var prevTime = 0L
  private var prevAngle = 0D
  protected var teethDisplacement = 0D

  /**
   * Events
   */
  var onTorqueChanged: () => Unit = () => ()
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
    return prevAngle + (if (getMechanicalGrid != null && getMechanicalGrid.spinMap(this)) teethDisplacement else 0D)
  }

  @deprecated
  protected def revolve()
  {

  }

  @deprecated
  override def getRadius(dir: ForgeDirection, `with`: TMechanicalNode): Double = 0.5

  override def rotate(from: AnyRef, torque: Double, angle: Double)
  {
    bufferTorque += torque
    bufferAngle += angle
  }

  /**
   * The percentage of torque loss every second
   */
  def getTorqueLoad: Double = load

  /**
   * The percentage of angular velocity loss every second
   */
  def getAngularVelocityLoad: Double = load

  def getPower: Double = getMechanicalGrid.power

  def getMechanicalGrid: MechanicalGrid = super.getGrid.asInstanceOf[MechanicalGrid]

  override def newGrid: GridNode[MechanicalNode] = new MechanicalGrid

  override def isValidConnection(other: AnyRef): Boolean = other.isInstanceOf[MechanicalNode]
}