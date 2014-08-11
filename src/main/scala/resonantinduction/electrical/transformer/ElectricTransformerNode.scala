package resonantinduction.electrical.transformer

import net.minecraft.tileentity.TileEntity
import net.minecraftforge.common.util.ForgeDirection
import universalelectricity.api.core.grid.INodeProvider
import universalelectricity.api.core.grid.electric.IEnergyNode
import universalelectricity.core.grid.node.ElectricNode
import universalelectricity.core.transform.vector.VectorWorld

/**
 * Created by robert on 8/11/2014.
 */
class ElectricTransformerNode(parent: INodeProvider) extends ElectricNode(parent: INodeProvider)
{
  var connectionDirection : ForgeDirection = ForgeDirection.NORTH
  var input : Boolean = true;
  var otherNode : ElectricTransformerNode = null
  var step : Int = 2

  //Default constructor
  setResistance(0)

  def this(parent: INodeProvider, side: ForgeDirection, in : Boolean) =
  {
    this(parent)    
    connectionDirection = side
    input = in
  }

  override def getVoltage: Double =
  {
    if(!input)
    {
      return otherNode.getVoltage * step
    }
    return voltage
  }

  override def canConnect(from: ForgeDirection, source: AnyRef): Boolean =
  {
    return source.isInstanceOf[INodeProvider] && from == connectionDirection
  }

  override def addEnergy(wattage: Double, doAdd: Boolean): Double =
  {
    if(input)
    {
      return otherNode.sendEnergy(wattage, doAdd)
    }
    return 0
  }

  def sendEnergy(wattage: Double, doAdd: Boolean): Double =
  {
    val tile : TileEntity = new VectorWorld(parent.asInstanceOf[TileEntity]).add(connectionDirection).getTileEntity
    if(tile.isInstanceOf[INodeProvider] && tile.asInstanceOf[INodeProvider].getNode(Class[IEnergyNode], connectionDirection.getOpposite).isInstanceOf[IEnergyNode])
    {
      val node :IEnergyNode = tile.asInstanceOf[INodeProvider].getNode(Class[IEnergyNode], connectionDirection.getOpposite).asInstanceOf[IEnergyNode]
      return node.addEnergy(wattage, doAdd)
    }

    return 0
  }


  override def removeEnergy(wattage: Double, doRemove: Boolean) : Double =
  {
    return 0
  }
}
