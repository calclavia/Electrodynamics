package resonantinduction.mechanical.mech.grid

import resonant.api.grid.IUpdate
import resonant.lib.grid.{GridNode, UpdateTicker}

import scala.collection.convert.wrapAll._
import scala.collection.mutable

/**
 * A grid that manages the mechanical objects
 * @author Calclavia
 */
class MechanicalGrid extends GridNode[MechanicalNode](classOf[MechanicalNode]) with IUpdate
{

  /**
   * A map marking out the relative spin directions of each node.
   * Updated upon recache
   */
  val spinMap = mutable.WeakHashMap.empty[MechanicalNode, Boolean]

  /**
   * The power of the mechanical grid
   * Unit: Watts or Joules per second
   */
  private var _power = 0D

  def power = _power

  /**
   * Rebuild the node list starting from the first node and recursively iterating through its connections.
   */
  override def reconstruct(first: MechanicalNode)
  {
    super.reconstruct(first)
    UpdateTicker.addUpdater(this)
  }

  override protected def populateNode(node: MechanicalNode, prev: MechanicalNode)
  {
    super.populateNode(node, prev)
    val dir = if (prev != null) (if (node.inverseRotation(prev)) !spinMap(prev) else spinMap(prev)) else false
    spinMap += (node -> dir)

    //Set mechanical node's initial angle
    if (prev != null)
      node.prevAngle = (prev.prevAngle + prev.angleDisplacement) % (2 * Math.PI)
  }

  override def update(deltaTime: Double)
  {
    //Find all nodes that are currently producing energy
    val inputs = getNodes.filter(n => n.bufferTorque != 0 && n.bufferAngle != 0)

    //Calculate the total input equivalent torque and angular velocity
    val input = inputs
      .map(
        n =>
        {
          val inversion = if (spinMap(n)) 1 else -1
          (n.bufferTorque * n.ratio * inversion, n.bufferAngle / deltaTime / n.ratio * inversion)
        })
      .foldLeft((0D, 0D))((b, a) => (a._1 + b._1, a._2 + b._2))

    var delta = (0D, 0D)

    if (input._1 != 0 && input._2 != 0)
    {
      //Calculate the total resistance of all nodes
      //TODO: Cache this
      val resistance = getNodes.view
        .map(n => (n.getTorqueLoad, n.getAngularVelocityLoad))
        .foldLeft((0D, 0D))((b, a) => (a._1 + b._1, a._2 + b._2))

      //Calculate the total change in torque and angular velocity
      delta = (input._1 - input._1 * resistance._1 / getNodes.size(), input._2 - input._2 * resistance._2 / getNodes.size())
    }

    //Calculate power
    _power = delta._1 * delta._2

    //Set torque and angular velocity of all nodes
    getNodes.foreach(n =>
    {
      val prevTorque = n.torque
      val prevAngularVelocity = n.angularVelocity

      val inversion = if (spinMap(n)) 1 else -1
      n.torque = delta._1 * n.ratio * inversion
      n.angularVelocity = delta._2 / n.ratio * inversion

      if (Math.abs(prevTorque - n.torque) >= 0.1)
        n.onTorqueChanged()

      if (Math.abs(prevAngularVelocity - n.angularVelocity) >= 0.1)
        n.onVelocityChanged()

    })

    //Clear buffers
    inputs.foreach(n =>
    {
      n.bufferTorque = 0
      n.bufferAngle = 0
    })
  }

  override def continueUpdate = getNodes.size > 0

  override def canUpdate = getNodes.size > 0
}
