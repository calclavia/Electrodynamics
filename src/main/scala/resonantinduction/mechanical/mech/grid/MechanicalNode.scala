package resonantinduction.mechanical.mech.grid

import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.ForgeDirection
import resonant.api.grid.{INodeProvider, IUpdate}
import resonant.lib.grid.GridNode
import resonant.lib.grid.node.NodeGrid
import resonant.lib.transform.vector.IVectorWorld
import resonant.lib.utility.nbt.ISaveObj
import resonantinduction.core.interfaces.TMechanicalNode
import resonantinduction.core.prefab.node.TMultipartNode

/**
 * Prefab node for the mechanical system used by almost ever mechanical object in Resonant Induction. Handles connections to other tiles, and shares power with them
 *
 * @author Calclavia, Darkguardsman
 */
class MechanicalNode(parent: INodeProvider) extends NodeGrid[MechanicalNode](parent) with TMultipartNode[MechanicalNode] with TMechanicalNode with ISaveObj with IVectorWorld
{
  /**
   * Allows the node to share its power with other nodes
   */
  var torque: Double = 0
  var prevTorque: Double = .0
  var prevAngularVelocity: Double = .0
  var angularVelocity: Double = 0

  protected[grid] var bufferTorque = 0D
  protected[grid] var bufferAngle = 0D

  /**
   * Current angle of rotation, mainly used for rendering
   */
  var renderAngle: Double = 0
  /**
   * Angle of rotation of last update
   */
  var prev_angle: Double = 0
  var acceleration: Float = 2f
  protected var maxDeltaAngle: Double = Math.toRadians(120)
  protected var load = 0.2

  /**
   * Events
   */
  var onStateChanged: () => Unit = () => ()

  override def getRadius(dir: ForgeDirection, `with`: TMechanicalNode): Double = 0.5

  override def angularVelocity(side: ForgeDirection): Double = angularVelocity

  override def torque(side: ForgeDirection): Double = torque

  override def inverseRotation(side: ForgeDirection): Boolean = false

  /*
override def update(deltaTime: Double)
{
  if (angularVelocity >= 0)
  {
    renderAngle += Math.min(angularVelocity, this.maxDeltaAngle) * deltaTime
  }
  else
  {
    renderAngle += Math.max(angularVelocity, -this.maxDeltaAngle) * deltaTime
  }
  if (renderAngle >= Math.PI * 2)
  {
    revolve
    renderAngle = renderAngle % (Math.PI * 2)
  }
  if (world != null && !world.isRemote)
  {
    val acceleration: Double = this.acceleration * deltaTime
    if (Math.abs(prevAngularVelocity - angularVelocity) > 0.01f)
    {
      prevAngularVelocity = angularVelocity
      onStateChanged()
    }
    if (Math.abs(prevTorque - torque) > 0.01f)
    {
      prevTorque = torque
      onStateChanged()
    }
    val torqueLoss: Double = Math.min(Math.abs(getTorque), (Math.abs(getTorque * getTorqueLoad) + getTorqueLoad / 10) * deltaTime)
    torque += (if (torque > 0) -torqueLoss else torqueLoss)
    val velocityLoss: Double = Math.min(Math.abs(getAngularSpeed), (Math.abs(getAngularSpeed * getAngularVelocityLoad) + getAngularVelocityLoad / 10) * deltaTime)
    angularVelocity += (if (angularVelocity > 0) -velocityLoss else velocityLoss)
    if (getEnergy <= 0)
    {
      angularVelocity = ({torque = 0; torque})
    }
    power = getEnergy / deltaTime

    if (sharePower)
    {
      directionMap.foreach
      {
        case (adjacentMech: MechanicalNode, dir: ForgeDirection) =>
        {
          if (adjacentMech != null)
          {
            val ratio: Double = adjacentMech.getRadius(dir.getOpposite, this) / getRadius(dir, adjacentMech)
            val inverseRotation: Boolean = this.inverseRotation(dir) && adjacentMech.inverseRotation(dir.getOpposite)
            val inversion: Int = if (inverseRotation) -1 else 1
            val targetTorque: Double = inversion * adjacentMech.getTorque / ratio
            val applyTorque: Double = targetTorque * acceleration
            if (Math.abs(torque + applyTorque) < Math.abs(targetTorque))
            {
              torque += applyTorque
            }
            else if (Math.abs(torque - applyTorque) > Math.abs(targetTorque))
            {
              torque -= applyTorque
            }
            val targetVelocity: Double = inversion * adjacentMech.getAngularSpeed * ratio
            val applyVelocity: Double = targetVelocity * acceleration
            if (Math.abs(angularVelocity + applyVelocity) < Math.abs(targetVelocity))
            {
              angularVelocity += applyVelocity
            }
            else if (Math.abs(angularVelocity - applyVelocity) > Math.abs(targetVelocity))
            {
              angularVelocity -= applyVelocity
            }
          }
        }
      }
    }
  }

  prev_angle = renderAngle

  }
*/

  /**
   * Called when one revolution is made.
   */
  protected def revolve
  {
  }

  override def rotate(from: AnyRef, torque: Double, angle: Double)
  {
    bufferTorque += torque
    bufferAngle += angle
  }

  private def getTorque: Double = if (angularVelocity != 0) torque else 0

  private def getAngularSpeed: Double = if (torque != 0) angularVelocity else 0

  /**
   * The percentage of torque loss every second
   */
  def getTorqueLoad: Double = load

  /**
   * The percentage of angular velocity loss every second
   */
  def getAngularVelocityLoad: Double = load

  def getEnergy: Double =
  {
    return getTorque * getAngularSpeed
  }

  def getPower: Double =
  {
    return 0//getMechanicalGrid.power
  }

  def load(nbt: NBTTagCompound)
  {
    torque = nbt.getDouble("torque")
    angularVelocity = nbt.getDouble("angularVelocity")
  }

  def save(nbt: NBTTagCompound)
  {
    nbt.setDouble("torque", torque)
    nbt.setDouble("angularVelocity", angularVelocity)
  }

  def getMechanicalGrid: MechanicalGrid = super.getGrid.asInstanceOf[MechanicalGrid]

  override def newGrid: GridNode[MechanicalNode] = new MechanicalGrid

  override def isValidConnection(other: AnyRef): Boolean =
  {
    return other.isInstanceOf[MechanicalNode]
  }
}