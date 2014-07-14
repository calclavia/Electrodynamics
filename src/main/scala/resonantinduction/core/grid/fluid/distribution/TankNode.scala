package resonantinduction.core.grid.fluid.distribution

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.IFluidHandler
import resonant.lib.utility.WorldUtility
import resonantinduction.core.grid.MultipartNode
import universalelectricity.api.core.grid.INodeProvider
import universalelectricity.core.transform.vector.Vector3

class TankNode(parent: INodeProvider) extends MultipartNode[TankNode](parent) with IFluidHandler
{
  var maxFlowRate: Int = 20
  var maxPressure: Int = 100
  private var pressure: Int = 0

  var onChange: () => () = _

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

    if (this.worldObj != null && !this.worldObj.isRemote)
    {
      val previousConnections: Byte = renderSides
      connectedBlocks = new Array[AnyRef](6)
      renderSides = 0
      for (dir <- ForgeDirection.VALID_DIRECTIONS)
      {
        this.validateConnectionSide(new Vector3(this).add(dir).getTileEntity(worldObj), dir)
      }
      if (previousConnections != renderSides)
      {
        getNetwork.update
        getNetwork.reconstruct
        onChange.apply()
        sendRenderUpdate
      }
    }
  }

  /**
   * Checks to make sure the connection is valid to the tileEntity
   *
   * @param tileEntity - the tileEntity being checked
   * @param side       - side the connection is too
   */
  def validateConnectionSide(tileEntity: TileEntity, side: ForgeDirection)
  {
    if (!this.worldObj.isRemote)
    {
      if (tileEntity.isInstanceOf[IFluidDistributor])
      {
        this.getNetwork.merge((tileEntity.asInstanceOf[IFluidDistributor]).getNetwork)
        renderSides = WorldUtility.setEnableSide(renderSides, side, true)
        connectedBlocks(side.ordinal) = tileEntity
      }
    }
  }

  override def canConnect(from: ForgeDirection, source: AnyRef): Boolean =
  {
    return (source.isInstanceOf[TankNode]) && (connectionMap & (1 << from.ordinal)) != 0
  }

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