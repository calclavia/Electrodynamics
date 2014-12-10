package resonantinduction.mechanical.mech.grid

import resonant.api.IUpdate
import resonant.lib.grid.{GridNode, UpdateTicker}

import scala.collection.convert.wrapAll._

/**
 * A grid that manages the mechanical objects
 * @author Calclavia
 */
class MechanicalGrid extends GridNode[NodeMechanical](classOf[NodeMechanical]) with IUpdate
{
  /**
   * The energy loss of this grid
   */
  private var load = 0D

  /**
   * Determines if this grid is locked (invalid opposite gear connections)
   */
  private var isLocked = false

  /**
   * The nodes that the grid is currently recusing through
   */
  private var passed = Seq.empty[NodeMechanical]

  /**
   * Rebuild the node list starting from the first node and recursively iterating through its connections.
   */
  override def reconstruct(first: NodeMechanical)
  {
    super.reconstruct(first)
    UpdateTicker.world.addUpdater(this)
    isLocked = false
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

      if (!isLocked)
      {
        getNodes.filter(n => n.bufferTorque != 0 && n.bufferAngularVelocity != 0).foreach(
          n =>
          {
            passed = Seq(n)
            recurse(deltaTime, n.bufferTorque, n.bufferAngularVelocity)
          }
        )
      }

      passed = Seq.empty[NodeMechanical]

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

  def recurse(deltaTime: Double, torque: Double, angularVelocity: Double)
  {
    val curr = passed.last

    if (passed.size > 1)
    {
      //Pass energy to every single node
      val prev = passed(passed.size - 2)
      val ratio = curr.radius(prev) / prev.radius(curr)
      val invert = if (curr.inverseRotation(prev) && prev.inverseNext(curr)) -1 else 1
      val addTorque = torque * ratio * invert
      val addVel = angularVelocity / ratio * invert
      curr.torque += addTorque
      curr.angularVelocity += addVel * deltaTime

      curr.connections.foreach(c =>
      {
        if (c != prev)
        {
          if (!passed.contains(c))
          {
            passed :+= c
            recurse(deltaTime, addTorque, addVel)
          }
          else
          {
            //Check for grid lock
            val sudoInvert = if (c.inverseRotation(curr) && curr.inverseNext(c)) -1 else 1

            if (Math.signum(c.angularVelocity) != sudoInvert * Math.signum(addVel))
            {
              isLocked = false
            }
          }
        }
      })
    }
    else
    {
      //This is the first node.
      //Calculate energy loss
      val power = torque * angularVelocity
      val netEnergy = Math.max(power - load * deltaTime, 0)
      val netTorque = netEnergy * (torque / power)
      val netVelocity = netEnergy * (angularVelocity / power)

      curr.torque += netTorque
      curr.angularVelocity += netVelocity * deltaTime
      curr.connections.foreach(c =>
      {
        passed :+= c
        recurse(deltaTime, netTorque, netVelocity)
      })
    }
  }

  override def continueUpdate = getNodes.size > 0 && !dead

  override def canUpdate = getNodes.size > 0
}
