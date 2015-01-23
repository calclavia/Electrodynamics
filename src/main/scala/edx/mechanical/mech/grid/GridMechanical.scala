package edx.mechanical.mech.grid

import resonant.api.IUpdate
import resonant.lib.grid.core.{GridNode, UpdateTicker}

import scala.collection.convert.wrapAll._

/**
 * A grid that manages the mechanical objects
 * @author Calclavia
 */
class GridMechanical extends GridNode[NodeMechanical] with IUpdate
{
  /**
   * Determines if this grid is locked (invalid opposite gear connections)
   */
  private var isLocked = false

  /**
   * The nodes that the grid is currently recusing through
   */
  private var allRecursed = Seq.empty[NodeMechanical]
  private var allDistributed = Seq.empty[NodeMechanical]

  nodeClass = classOf[NodeMechanical]

  /**
   * Rebuild the node list starting from the first node and recursively iterating through its connections.
   */
  override def reconstruct(first: NodeMechanical)
  {
    super.reconstruct(first)
    UpdateTicker.world.addUpdater(this)
    isLocked = false
  }

  override def deconstruct(first: NodeMechanical)
  {
    super.deconstruct(first)
    UpdateTicker.world.removeUpdater(this)
  }

  override def update(deltaTime: Double)
  {
    nodes synchronized
    {
      /**
       * Consider this as the moment of inertia: how difficult it is to spin this object.
       */
      nodes.foreach(
        n =>
        {
          n.torque = 0
          n.angularVelocity -= n.angularVelocity * deltaTime * n.friction
        }
      )

      //TODO: Add deceleration

      if (!isLocked)
      {
        getNodes.filter(n => n.bufferTorque != 0).foreach(
          n =>
          {
            allDistributed = Seq(n)
            recurse(Seq(n), deltaTime, n.bufferTorque, 0)
          }
        )
      }

      allDistributed = Seq.empty[NodeMechanical]

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
      }
    )
  }

  def calculateEquivalentInertia(passed: Seq[NodeMechanical]): Double =
  {
    val curr = passed.last
    allRecursed :+= curr

    /**
     * I1 + n^2 * I
     * where n is the acceleration ratio
     */
    var inertia = curr.inertia
    inertia += curr.connections.filterNot(allRecursed.contains).map(c => c.radius(curr) / curr.radius(c) * calculateEquivalentInertia(passed :+ c)).foldLeft(0d)(_ + _)
    return inertia
  }

  def recurse(passed: Seq[NodeMechanical], deltaTime: Double, torque: Double, angularVelocity: Double)
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
          if (!allDistributed.contains(c))
          {
            allDistributed :+= c
            recurse(passed :+ c, deltaTime, addTorque, addVel)
          }
          else
          {
            //Check for grid lock
            val sudoInvert = if (c.inverseRotation(curr) && curr.inverseNext(c)) -1 else 1

            if (Math.signum(c.angularVelocity) != sudoInvert * Math.signum(addVel))
            {
              isLocked = true
            }
          }
        }
      })
    }
    else
    {
      /**
       * This is the first node.
       * 1. Calculate equivalent moment of inertia of the mechanical system.
       * 2. Determine the angular acceleration:
       * T = I * a
       * a = T/I
       * where I = inertia and a = angular acceleration
       */
      val inertia = calculateEquivalentInertia(passed)
      val netTorque = torque
      val netAcceleration = torque / inertia

      curr.torque += netTorque
      curr.angularVelocity += netAcceleration * deltaTime
      curr.connections.foreach(c =>
      {
        allDistributed :+= c
        recurse(passed :+ c, deltaTime, netTorque, netAcceleration)
      })
    }
  }

  override def updatePeriod: Int = if (getNodes.size > 0) 50 else 0
}
