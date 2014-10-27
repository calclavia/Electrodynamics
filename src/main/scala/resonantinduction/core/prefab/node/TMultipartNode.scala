package resonantinduction.core.prefab.node

import codechicken.multipart.TMultiPart
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import resonant.api.grid.INodeProvider
import resonant.lib.grid.node.NodeConnector

/**
 * A trait that allows nodes to works with Forge Multipart. This trait MUST be mixed in.
 * @author Calclavia
 */
trait TMultipartNode extends NodeConnector
{
  override def world: World =
  {
    if (parent != null)
    {
      if (parent.isInstanceOf[TMultiPart])
        return parent.asInstanceOf[TMultiPart].world
      else if (parent.isInstanceOf[TileEntity])
        return parent.asInstanceOf[TileEntity].getWorldObj
    }
    return null
  }

  override def x: Double =
  {
    if (parent != null)
    {
      if (parent.isInstanceOf[TMultiPart])
        return (parent.asInstanceOf[TMultiPart]).x
      else if (parent.isInstanceOf[TileEntity])
        return (parent.asInstanceOf[TileEntity]).xCoord
    }
    return 0
  }

  override def y: Double =
  {
    if (parent != null)
    {
      if (parent.isInstanceOf[TMultiPart])
        return (parent.asInstanceOf[TMultiPart]).y
      else if (parent.isInstanceOf[TileEntity])
        return (parent.asInstanceOf[TileEntity]).yCoord
    }
    return 0
  }

  override def z: Double =
  {
    if (parent != null)
    {
      if (parent.isInstanceOf[TMultiPart])
        return (parent.asInstanceOf[TMultiPart]).z
      else if (parent.isInstanceOf[TileEntity])
        return (parent.asInstanceOf[TileEntity]).zCoord
    }
    return 0
  }
}
