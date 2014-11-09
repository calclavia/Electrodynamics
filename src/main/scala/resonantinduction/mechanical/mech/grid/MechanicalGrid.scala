package resonantinduction.mechanical.mech.grid

import resonant.api.grid.IUpdate
import resonant.lib.grid.{GridNode, UpdateTicker}
import resonantinduction.core.interfaces.TMechanicalNode

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
  private val spinMap = mutable.WeakHashMap.empty[MechanicalNode, Boolean]

  /**
   * The power of the mechanical grid
   * Unit: Watts or Joules per second
   */
  private var _power = 0D

  def power = _power

  override def reconstruct()
  {
    //Populate spin map
    spinMap.clear()
    populateSpinMap(getNodes.head)

    UpdateTicker.addUpdater(this)
  }

  private def populateSpinMap(node: MechanicalNode, inverse: Boolean = false)
  {
    spinMap += (node -> inverse)
    node.connections.foreach(n => populateSpinMap(n, !inverse))
  }

  override def update(deltaTime: Double)
  {

  }

  /**
   * Propogates the buffer from this specific device
   */
  private def propogate()
  {

  }

  override def continueUpdate = getNodes.size > 0

  override def canUpdate = getNodes.size > 0
}
