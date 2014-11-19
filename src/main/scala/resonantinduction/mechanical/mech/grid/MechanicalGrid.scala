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
          n._torque = 0
          n._angularVelocity = 0
        }
      )

      getNodes.filter(n => n.bufferTorque != 0 && n.bufferAngularVelocity != 0).foreach(n => recurse(Seq(n)))

      /*
      //Find all nodes that are currently producing energy
      val inputs = getNodes.filter(n => n.bufferTorque != 0)

      //Calculate the total input equivalent torque
      val inputTorque = inputs
                        .map(n => n.bufferTorque * (if (spinMap(n)) 1 else -1))
                        .foldLeft(0D)(_ + _)

      val deltaTorque = if (inputTorque != 0) Math.max(Math.abs(inputTorque) - load * deltaTime, 0) * inputTorque / Math.abs(inputTorque) else 0


      //Set torque and angular velocity of all nodes
      getNodes.foreach(
        n =>
        {
          val prevTorque = n.torque
          val prevAngularVelocity = n.angularVelocity

          val inversion = if (spinMap(n)) 1 else -1
          n._torque = deltaTorque * inversion
          val angularAcceleration = deltaTorque / n.radius
          n._angularVelocity = angularAcceleration * deltaTime * inversion

          if (Math.abs(n.torque - prevTorque) > 0)
            n.onTorqueChanged()

          if (Math.abs(n.angularVelocity - prevAngularVelocity) > 0)
            n.onVelocityChanged()

          //Clear buffers
          n.bufferTorque = n.bufferDefaultTorque
        })
        */
    }
  }

  def recurse(passed: Seq[NodeMechanical])
  {
    val curr = passed(passed.size - 1)

    if (passed.size > 1)
    {
      val prev = passed(passed.size - 2)
      val ratio = curr.radius / prev.radius
      val invert = if (curr.inverseRotation(prev)) 1 else -1
      curr._torque += passed(0).torque * ratio * invert
      curr._angularVelocity += passed(0).angularVelocity / ratio * invert
    }
    else
    {
      curr._torque += curr.bufferTorque
      curr._angularVelocity += curr.bufferAngularVelocity
      curr.bufferTorque = 0
      curr.bufferAngularVelocity = 0
    }

    if (curr.power > 0)
      curr.connections.foreach(c => recurse(passed :+ c))
  }

  override def continueUpdate = getNodes.size > 0

  override def canUpdate = getNodes.size > 0
}
