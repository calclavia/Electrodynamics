package edx.electrical.multimeter

import net.minecraft.nbt.{NBTTagCompound, NBTTagList}
import resonant.lib.grid.core.Grid
import resonant.lib.transform.vector.Vector3
import resonant.lib.utility.LanguageUtility
import resonant.lib.utility.science.UnitDisplay

import scala.collection.convert.wrapAll._
import scala.collection.mutable.ArrayBuffer

class GridMultimeter extends Grid[PartMultimeter]
{
  final val displayInformation = new ArrayBuffer[String]
  /**
   * The available graphs to be handled.
   */
  final val graphs = new ArrayBuffer[Graph[_]]
  final val energyGraph = new Graph[Double]("energy", maxData)
  final val powerGraph = new Graph[Double]("power", maxData)
  final val voltageGraph = new Graph[Double]("voltage", maxData)
  final val torqueGraph = new Graph[Double]("torque", maxData)
  final val angularVelocityGraph = new Graph[Double]("speed", maxData)
  final val fluidGraph = new Graph[Int]("fluid", maxData)
  final val thermalGraph = new Graph[Int]("temperature", maxData)
  final val pressureGraph = new Graph[Int]("pressure", maxData)
  /**
   * Maximum data points a graph can store.
   */
  private val maxData: Int = 1
  /**
   * The absolute center of the multimeter screens.
   */
  var center: Vector3 = new Vector3
  /**
   * The relative bound sizes.
   */
  var upperBound: Vector3 = new Vector3
  var lowerBound: Vector3 = new Vector3
  /**
   * The overall size of the multimeter
   */
  var size: Vector3 = new Vector3
  /**
   * If the screen is not a perfect rectangle, don't render.
   */
  var isEnabled: Boolean = true
  var primaryMultimeter: PartMultimeter = null
  private var doUpdate: Boolean = false

  nodeClass = classOf[PartMultimeter]

  graphs += energyGraph
  graphs += powerGraph
  graphs += voltageGraph
  graphs += torqueGraph
  graphs += angularVelocityGraph
  graphs += fluidGraph
  graphs += thermalGraph
  graphs += pressureGraph

  def getDisplay(graphID: Int): String =
  {
    val graph = graphs(graphID)
    var graphValue: String = ""
    if (graph == energyGraph) graphValue = new UnitDisplay(UnitDisplay.Unit.JOULES, energyGraph()).toString
    if (graph == powerGraph) graphValue = new UnitDisplay(UnitDisplay.Unit.WATT, powerGraph()).toString
    if (graph == voltageGraph) graphValue = new UnitDisplay(UnitDisplay.Unit.VOLTAGE, voltageGraph()).toString
    if (graph == torqueGraph) graphValue = new UnitDisplay(UnitDisplay.Unit.NEWTON_METER, torqueGraph(), true).toString
    if (graph == angularVelocityGraph) graphValue = UnitDisplay.roundDecimals(angularVelocityGraph()) + " rad/s"
    if (graph == fluidGraph) graphValue = new UnitDisplay(UnitDisplay.Unit.LITER, fluidGraph()).toString
    if (graph == thermalGraph) graphValue = UnitDisplay.roundDecimals(thermalGraph()) + " K"
    if (graph == pressureGraph) graphValue = UnitDisplay.roundDecimals(pressureGraph()) + " Pa"
    return getLocalized(graph) + ": " + graphValue
  }

  def getLocalized(graph: Graph[_]): String =
  {
    return LanguageUtility.getLocal("tooltip.graph." + graph.name)
  }

  def isPrimary(check: PartMultimeter): Boolean =
  {
    return primaryMultimeter == check
  }

  def markUpdate
  {
    doUpdate = true
  }

  override def isValidNode(node: AnyRef): Boolean =
  {
    return node.isInstanceOf[PartMultimeter] && node.asInstanceOf[PartMultimeter].world != null && node.asInstanceOf[PartMultimeter].tile != null
  }

  override def reconstruct()
  {
    if (getNodes.size > 0)
    {
      primaryMultimeter = null
      upperBound = null
      lowerBound = null

      nodes.foreach(node =>
      {
        node.setGrid(this)
        if (primaryMultimeter == null) primaryMultimeter = node
        if (upperBound == null)
        {
          upperBound = node.getPosition.add(1)
        }
        if (lowerBound == null)
        {
          lowerBound = node.getPosition
        }
        upperBound = upperBound.max(node.getPosition.add(1))
        lowerBound = lowerBound.min(node.getPosition)
      })

      center = upperBound.midPoint(lowerBound)
      upperBound -= center
      lowerBound -= center
      size = new Vector3(Math.abs(upperBound.x) + Math.abs(lowerBound.x), Math.abs(upperBound.y) + Math.abs(lowerBound.y), Math.abs(upperBound.z) + Math.abs(lowerBound.z))
      val area: Double = (if (size.x != 0) size.x else 1) * (if (size.y != 0) size.y else 1) * (if (size.z != 0) size.z else 1)
      isEnabled = area == getNodes.size

      getNodes foreach (c =>
      {
        c.updateDesc()
        c.updateGraph()
      })

      doUpdate = true
    }
  }

  def load(nbt: NBTTagCompound)
  {
    val nbtList: NBTTagList = nbt.getTagList("graphs", 0)

    for (i <- 0 until nbtList.tagCount)
    {
      val nbtCompound: NBTTagCompound = nbtList.getCompoundTagAt(i)
      graphs.get(i).load(nbtCompound)
    }
  }

  def save: NBTTagCompound =
  {
    val nbt = new NBTTagCompound
    val data = new NBTTagList
    graphs.foreach(
      g =>
      {
        val tag = new NBTTagCompound
        g.save(tag)
        data.appendTag(tag)
      }
    )
    nbt.setTag("graphs", data)
    return nbt
  }
}