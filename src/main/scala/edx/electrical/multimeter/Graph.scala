package edx.electrical.multimeter

import net.minecraft.nbt.NBTTagCompound
import resonantengine.api.misc.ISave
import resonantengine.lib.collection.EvictingList
import resonantengine.lib.wrapper.NBTWrapper._

import scala.collection.JavaConversions._

/**
 * Graph for the multimeter
 *
 * @author Calclavia
 */
class Graph[V](val name: String, val maxPoints: Int = 0)(implicit n: Numeric[V]) extends ISave
{
  /**
   * Each point represents a tick.
   */
  protected var points = new EvictingList[V](maxPoints)
  /**
   * Queue for the next update to insert into the graph.
   */
  protected var queue: V = default
  private var peak: V = default

  def getPeak: V = peak

  def head: V = apply(0)

  def apply(x: Int = 0): V = if (points.size > x) points.get(x) else default

  def getDouble(x: Int = 0) = n.toDouble(this(x))

  def queue(value: V)
  {
    queue = value
  }

  def doneQueue = this += queue

  def +=(y: V)
  {
    points.add(y)
    peak = default
    for (point <- points) if (n.gt(point, n.zero)) peak = y
  }

  def default: V = n.zero

  def load(nbt: NBTTagCompound)
  {
    points.clear()
    val array = nbt.getArray[Double]("DataPoints")
    points.addAll(array.map(_.asInstanceOf[V]).toList)
  }

  def save(nbt: NBTTagCompound)
  {
    nbt.setArray("DataPoints", points.map(n.toDouble).toArray)
  }

  def getAverage: Double = if (points.size > 0) n.toDouble(points.foldLeft(n.zero)((b, a) => n.plus(b, a))) / points.size else 0
}