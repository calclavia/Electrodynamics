package resonantinduction.mechanical.mech.grid

import resonant.api.grid.IUpdate
import resonant.lib.grid.{GridNode, UpdateTicker}

import scala.collection.convert.wrapAll._

/**
 * A grid that manages the mechanical objects
 * @author Calclavia
 */
class MechanicalGrid extends GridNode[NodeMechanical](classOf[NodeMechanical]) with IUpdate
{
  private var load = 0D

  /**
   * Rebuild the node list starting from the first node and recursively iterating through its connections.
   */
  override def reconstruct(first: NodeMechanical)
  {
    super.reconstruct(first)
    UpdateTicker.world.addUpdater(this)
  }

  override def update(deltaTime: Double)
  {
    getNodes synchronized
    {
      load = 0

      getNodes.foreach(
        n =>
        {
          n.torque = 0
          n.angularVelocity = 0
          load += n.getLoad
        }
      )

      getNodes.filter(n => n.bufferTorque != 0 && n.bufferAngularVelocity != 0).foreach(n => recurse(deltaTime, n.bufferTorque, n.bufferAngularVelocity, Seq(n)))

      //      UpdateTicker.world.enqueue(resetNodes)
      resetNodes()
    }
  }

  def resetNodes()
  {
    getNodes.foreach(
      n =>
      {
        if (n.prevTorque != n.torque)
        {
          n.prevTorque = n.torque
          n.onTorqueChanged()
        }

        if (n.prevAngularVelocity != n.angularVelocity)
        {
          n.prevAngularVelocity = n.angularVelocity
          n.onVelocityChanged()
        }

        n.bufferTorque = 0
        n.bufferAngularVelocity = 0
      }
    )
  }

  def recurse(deltaTime: Double, torque: Double, angularVelocity: Double, passed: Seq[NodeMechanical])
  {
    val curr = passed.last

    if (passed.size > 1)
    {
      //Pass energy to every single node
      val prev = passed(passed.size - 2)
      val ratio = curr.radius(prev) / prev.radius(curr)
      val invert = if (curr.inverseRotation(prev)) -1 else 1
      val addTorque = torque * ratio * invert
      val addVel = angularVelocity / ratio * invert
      curr.torque += addTorque
      curr.angularVelocity += addVel * deltaTime
      curr.connections.filter(!passed.contains(_)).foreach(c => recurse(deltaTime, addTorque, addVel, passed :+ c))
    }
    else
    {
      //Calculate energy loss
      val power = torque * angularVelocity
      val netEnergy = Math.max(power - load * deltaTime, 0)
      val netTorque = netEnergy * (torque / power)
      val netVelocity = netEnergy * (angularVelocity / power)

      curr.torque += netTorque
      curr.angularVelocity += netVelocity * deltaTime
      curr.connections.filter(!passed.contains(_)).foreach(c => recurse(deltaTime, netTorque, netVelocity, passed :+ c))
    }
  }

  override def continueUpdate = getNodes.size > 0

  override def canUpdate = getNodes.size > 0
}
