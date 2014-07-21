package resonantinduction.core.grid.fluid.distribution

import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.IFluidHandler
import resonant.lib.utility.WorldUtility
import resonantinduction.core.grid.MultipartNode
import universalelectricity.api.core.grid.INodeProvider

class TankNode(parent: INodeProvider) extends MultipartNode[TankNode](parent) with IFluidHandler with TFluidForwarder
{
  var maxFlowRate: Int = 20
  var maxPressure: Int = 100

  //TODO: Do we actually call this?
  private var pressure: Int = 0
  var connectedSides: Byte = 0

  var onChange: () => Unit = null

  def genericParent = parent.asInstanceOf[TFluidDistributor]

  def getMaxFlowRate: Int =
  {
    return maxFlowRate
  }

  def setPressure(newPressure: Int)
  {
    if (newPressure > 0)
    {
      pressure = Math.min(maxPressure, newPressure)
    }
    else
    {
      pressure = Math.max(-maxPressure, newPressure)
    }
  }

  def getPressure(dir: ForgeDirection): Int =
  {
    return pressure
  }

  /**
   * Recache the connections. This is the default connection implementation.
   */
  override def doRecache
  {
    connections.clear

    if (world != null && !world.isRemote)
    {
      val previousSides = connectedSides
      connectedSides = 0

      for (dir <- ForgeDirection.VALID_DIRECTIONS)
      {
        val tile = (position + dir).getTileEntity

        if (tile.isInstanceOf[TFluidDistributor])
        {
          connections.put(tile.asInstanceOf[TFluidDistributor].getNode(classOf[TankNode], dir.getOpposite), dir)
          connectedSides = WorldUtility.setEnableSide(connectedSides, dir, true)
        }
      }

      if (previousSides != connectedSides)
      {
        //TODO: Check and fix
        getGrid.reconstruct()
        onChange.apply()
      }
    }
  }

  override def canConnect(from: ForgeDirection, source: AnyRef): Boolean =
  {
    //TODO: Check this
    return source.isInstanceOf[TankNode]
  }

  override def getForwardTank = getGrid.asInstanceOf[TankGrid]

  override protected def newGrid() = new TankGrid

  override def load(nbt: NBTTagCompound)
  {
    super.load(nbt)
    pressure = nbt.getInteger("pressure")
  }

  override def save(nbt: NBTTagCompound)
  {
    super.save(nbt)
    nbt.setInteger("pressure", pressure)
  }
}