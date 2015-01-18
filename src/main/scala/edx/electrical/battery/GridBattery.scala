package edx.electrical.battery

import java.util.{Arrays, LinkedHashSet, Set}

import resonant.lib.grid.core.Grid

import scala.collection.JavaConversions._

/** Basic grid designed to be used for creating a level look for batteries connected together
  * @author robert(Darkguardsman)
  */
class GridBattery extends Grid[TileBattery]
{
  var totalEnergy = 0d
  var totalCapacity = 0d

  nodeClass = classOf[TileBattery]

  /**
   * Causes the energy shared by all batteries to be distributed out to all linked batteries
   * @param exclusion - battery not to share with, used when batteries are removed from the network
   */
  def redistribute(exclusion: TileBattery*)
  {
    var lowestY: Int = 255
    var highestY: Int = 0
    totalEnergy = 0
    totalCapacity = 0
    for (connector <- this.getNodes)
    {
      totalEnergy += connector.energy.getEnergy
      totalCapacity += connector.energy.getEnergyCapacity
      lowestY = Math.min(connector.yCoord, lowestY)
      highestY = Math.max(connector.yCoord, highestY)
      connector.renderEnergyAmount = 0
    }

    var remainingRenderEnergy: Double = totalEnergy
    var y: Int = 0
    while (y >= 0 && y <= highestY && remainingRenderEnergy > 0)
    {
      val connectorsInlevel: Set[TileBattery] = new LinkedHashSet[TileBattery]

      for (connector <- this.getNodes)
      {
        if (connector.yCoord == y)
        {
          connectorsInlevel.add(connector)
        }
      }
      val levelSize: Int = connectorsInlevel.size
      var used: Double = 0

      for (connector <- connectorsInlevel)
      {
        val tryInject: Double = Math.min(remainingRenderEnergy / levelSize, connector.energy.getEnergyCapacity)
        connector.renderEnergyAmount_$eq(tryInject)
        used += tryInject
      }
      remainingRenderEnergy -= used
      y += 1
    }

    val percentageLoss: Double = 0
    val energyLoss: Double = percentageLoss * 100
    totalEnergy -= energyLoss
    val amountOfNodes: Int = this.getNodes.size - exclusion.length
    if (totalEnergy > 0 && amountOfNodes > 0)
    {
      var remainingEnergy: Double = totalEnergy
      val firstNode: TileBattery = nodes.head

      for (node <- this.getNodes)
      {
        if (node != firstNode && !Arrays.asList(exclusion).contains(node))
        {
          val percentage: Double = node.energy.getEnergyCapacity / totalCapacity
          val energyForBattery: Double = Math.max(totalEnergy * percentage, 0)
          node.energy.setEnergy(energyForBattery)
          remainingEnergy -= energyForBattery
        }
      }
      firstNode.energy.setEnergy(Math.max(remainingEnergy, 0))
    }
  }
}