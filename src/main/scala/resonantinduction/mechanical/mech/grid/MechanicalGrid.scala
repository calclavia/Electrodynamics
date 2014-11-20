package resonantinduction.mechanical.mech.grid

import resonant.api.grid.IUpdate
import resonant.lib.grid.{GridNode, UpdateTicker}

import scala.collection.convert.wrapAll._
import scala.collection.mutable

/**
 * A grid that manages the mechanical objects
 * @author Calclavia
 */
class MechanicalGrid extends GridNode[NodeMechanical](classOf[NodeMechanical]) with IUpdate
{

  /**
   * A map marking out the relative spin directions of each node.
   * Updated upon recache
   */
  val spinMap = mutable.WeakHashMap.empty[NodeMechanical, Boolean]

  private var load = 0D

  /**
   * Rebuild the node list starting from the first node and recursively iterating through its connections.
   */
  override def reconstruct(first: NodeMechanical)
  {
    super.reconstruct(first)
    UpdateTicker.threaded.addUpdater(this)

    load = getNodes.map(n => n.getLoad).foldLeft(0D)(_ + _)
  }

  override protected def populateNode(node: NodeMechanical, prev: NodeMechanical)
  {
    super.populateNode(node, prev)

    //TODO: Check if gears are LOCKED (when two nodes obtain undesirable spins)
    val dir = if (prev != null) if (node.inverseRotation(prev)) !spinMap(prev) else spinMap(prev) else false
    spinMap += (node -> dir)

    //Set mechanical node's initial angle
    if (prev != null)
      node.prevAngle = (prev.prevAngle + prev.angleDisplacement) % (2 * Math.PI)
  }

  override def update(deltaTime: Double)
  {
    getNodes synchronized
    {
      getNodes.foreach(
        n =>
        {
          n.prevTorque = n.torque
          n.prevAngularVelocity = n.angularVelocity

          n._torque = 0
          n._angularVelocity = 0
        }
      )

      getNodes.filter(n => n.bufferTorque != 0 && n.bufferAngularVelocity != 0).foreach(n => recurse(deltaTime, Seq(n)))

      getNodes.foreach(
        n =>
        {
          if (n.prevTorque != n.torque)
            n.onTorqueChanged()

          if (n.prevAngularVelocity != n.angularVelocity)
            n.onVelocityChanged()
        }
      )
    }
  }

  def recurse(deltaTime: Double, passed: Seq[NodeMechanical])
  {
    val curr = passed(passed.size - 1)

    if (passed.size > 1)
    {
      val prev = passed(passed.size - 2)
      val ratio = curr.radius / prev.radius
      val invert = if (curr.inverseRotation(prev)) -1 else 1
      curr._torque += prev.torque * ratio * invert
      curr._angularVelocity += prev.angularVelocity / ratio * invert
    }
    else
    {
      curr._torque += curr.bufferTorque
      curr._angularVelocity += curr.bufferAngularVelocity * deltaTime
      curr.bufferTorque = 0
      curr.bufferAngularVelocity = 0
    }

    if (curr.power > 0)
      curr.connections.filter(!passed.contains(_)).foreach(c => recurse(deltaTime, passed :+ c))
  }

  override def continueUpdate = getNodes.size > 0

  override def canUpdate = getNodes.size > 0
}
