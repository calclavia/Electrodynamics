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
    load = getNodes.map(n => n.getLoad).foldLeft(0D)(_ + _)
  }

  override def update(deltaTime: Double)
  {
    getNodes synchronized
    {
      getNodes.foreach(
        n =>
        {
          n._torque = 0
          n._angularVelocity = 0
        }
      )

      getNodes.filter(n => n.bufferTorque != 0 && n.bufferAngularVelocity != 0).foreach(n => recurse(deltaTime, n.bufferTorque, n.bufferAngularVelocity, Seq(n)))

      //UpdateTicker world enqueue
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
      val prev = passed(passed.size - 2)
      val ratio = curr.radius(prev) / prev.radius(curr)
      val invert = if (curr.inverseRotation(prev)) -1 else 1
      val addTorque = torque * ratio * invert
      val addVel = angularVelocity / ratio * invert
      curr._torque += addTorque
      curr._angularVelocity += addVel * deltaTime
      curr.connections.filter(!passed.contains(_)).foreach(c => recurse(deltaTime, addTorque, addVel, passed :+ c))
    }
    else
    {
      //Calculate energy loss
      val power = torque * angularVelocity
      val netEnergy = power - load * deltaTime
      val netTorque = netEnergy * (torque / power)
      val netVelocity = netEnergy * (angularVelocity / power)

      curr._torque += netTorque
      curr._angularVelocity += netVelocity * deltaTime
      curr.connections.filter(!passed.contains(_)).foreach(c => recurse(deltaTime, netTorque, netVelocity, passed :+ c))
    }
  }

  override def continueUpdate = getNodes.size > 0

  override def canUpdate = getNodes.size > 0
}
