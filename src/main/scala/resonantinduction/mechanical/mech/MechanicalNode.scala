package resonantinduction.mechanical.mech

import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.ForgeDirection
import resonant.api.grid.{INodeProvider, IUpdate}
import resonant.lib.grid.node.NodeConnector
import resonant.lib.transform.vector.IVectorWorld
import resonant.lib.utility.nbt.ISaveObj
import resonantinduction.core.interfaces.IMechanicalNode
import resonantinduction.core.prefab.node.TMultipartNode

import scala.collection.convert.wrapAll._

/**
 * Prefab node for the mechanical system used by almost ever mechanical object in Resonant Induction. Handles connections to other tiles, and shares power with them
 *
 * @author Calclavia, Darkguardsman
 */
class MechanicalNode(parent: INodeProvider) extends NodeConnector[MechanicalNode](parent) with TMultipartNode[MechanicalNode] with IMechanicalNode with ISaveObj with IVectorWorld with IUpdate
{
  /**
   * Marks that the rotation has changed and should be updated client side
   */
  var markRotationUpdate: Boolean = false
  /**
   * Makrs that the torque value has changed and should be updated client side
   */
  var markTorqueUpdate: Boolean = false
  /**
   * Allows the node to share its power with other nodes
   */
  var sharePower: Boolean = true
  var torque: Double = 0
  var prevTorque: Double = .0
  var prevAngularVelocity: Double = .0
  var angularVelocity: Double = 0
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
  protected var load: Double = 2
  private var power: Double = 0

  override def getRadius(dir: ForgeDirection, `with`: IMechanicalNode): Double = 0.5

  override def getAngularSpeed(side: ForgeDirection): Double = angularVelocity

  override def getForce(side: ForgeDirection): Double = torque

  override def inverseRotation(side: ForgeDirection): Boolean = false

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
        markRotationUpdate = true
      }
      if (Math.abs(prevTorque - torque) > 0.01f)
      {
        prevTorque = torque
        markTorqueUpdate = true
      }
      val torqueLoss: Double = Math.min(Math.abs(getTorque), (Math.abs(getTorque * getTorqueLoad) + getTorqueLoad / 10) * deltaTime)
      torque += (if (torque > 0) -torqueLoss else torqueLoss)
      val velocityLoss: Double = Math.min(Math.abs(getAngularSpeed), (Math.abs(getAngularSpeed * getAngularVelocityLoad) + getAngularVelocityLoad / 10) * deltaTime)
      angularVelocity += (if (angularVelocity > 0) -velocityLoss else velocityLoss)
      if (getEnergy <= 0)
      {
        angularVelocity = ({torque = 0; torque })
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

  override def canUpdate: Boolean = true

  override def continueUpdate: Boolean = true

  /**
   * Called when one revolution is made.
   */
  protected def revolve
  {
  }

  override def apply(source: AnyRef, torque: Double, angularVelocity: Double)
  {
    this.torque += torque
    this.angularVelocity += angularVelocity
  }

  private def getTorque: Double =
  {
    return if (angularVelocity != 0) torque else 0
  }

  private def getAngularSpeed: Double =
  {
    return if (torque != 0) angularVelocity else 0
  }

  /**
   * The energy percentage loss due to resistance in seconds.
   */
  def getTorqueLoad: Double =
  {
    return load
  }

  def getAngularVelocityLoad: Double =
  {
    return load
  }

  def getEnergy: Double =
  {
    return getTorque * getAngularSpeed
  }

  def getPower: Double =
  {
    return power
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

  /**
   * Can this node connect with another node?
   * @param other - Most likely a node, but it can also be another object
   * @param from - Direction of connection
   * @return True connection is allowed
   */
  override def canConnect[B <: MechanicalNode](other: B, from: ForgeDirection): Boolean =
  {
    if (canConnect(from))
    {
      if (other.isInstanceOf[INodeProvider])
      {
        return (other.asInstanceOf[INodeProvider]).getNode(classOf[MechanicalNode], from.getOpposite).isInstanceOf[MechanicalNode]
      }
      return other.isInstanceOf[MechanicalNode]
    }
    return false
  }

  override def isValidConnection(`object`: AnyRef): Boolean =
  {
    return true
  }
}