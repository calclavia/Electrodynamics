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
    UpdateTicker.addUpdater(this)

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
      //Find all nodes that are currently producing energy
      val inputs = getNodes.filter(n => n.bufferTorque != 0)

      //Calculate the total input equivalent torque
      val inputTorque = inputs
        .map(n => n.bufferTorque * (if (spinMap(n)) 1 else -1))
        .foldLeft(0D)(_ + _)

      val deltaTorque = if (inputTorque != 0) Math.max(Math.abs(inputTorque) - load * deltaTime, 0) * inputTorque / Math.abs(inputTorque) else 0

      //Set torque and angular velocity of all nodes
      getNodes.foreach(n =>
      {
        val prevTorque = n.torque
        val prevAngularVelocity = n.angularVelocity

        val inversion = if (spinMap(n)) 1 else -1
        n.torque = deltaTorque * n.ratio * inversion
        val angularAcceleration = deltaTorque / n.momentOfInertia
        n.angularVelocity = angularAcceleration / n.ratio * deltaTime * inversion

        if (Math.abs(prevTorque - n.torque) >= 0.1)
          n.onTorqueChanged()

        if (Math.abs(prevAngularVelocity - n.angularVelocity) >= 0.01)
          n.onVelocityChanged()

        //Clear buffers
        n.bufferTorque = 0
      })
    }
  }

  override def continueUpdate = getNodes.size > 0

  override def canUpdate = getNodes.size > 0
}
