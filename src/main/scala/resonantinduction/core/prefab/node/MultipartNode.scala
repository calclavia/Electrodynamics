package resonantinduction.core.prefab.node

import codechicken.multipart.TMultiPart
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import universalelectricity.api.core.grid.INodeProvider
import universalelectricity.core.grid.node.NodeConnector

/**
 * A node that works with Forge Multipart
 * @author Calclavia
 */
abstract class MultipartNode(parent: INodeProvider) extends NodeConnector(parent)
{
  override def world: World =
  {
    if(parent != null) {
      if (parent.isInstanceOf[TMultiPart])
        return parent.asInstanceOf[TMultiPart].world
      else if (parent.isInstanceOf[TileEntity])
        return parent.asInstanceOf[TileEntity].getWorldObj
    }
    return null
  }

  override def x: Double =
  {
    if(parent != null) {
      if (parent.isInstanceOf[TMultiPart])
        return (parent.asInstanceOf[TMultiPart]).x
      else if (parent.isInstanceOf[TileEntity])
        return (parent.asInstanceOf[TileEntity]).xCoord
    }
    return 0
  }

  override def y: Double =
  {
    if(parent != null) {
      if (parent.isInstanceOf[TMultiPart])
        return (parent.asInstanceOf[TMultiPart]).y
      else if (parent.isInstanceOf[TileEntity])
        return (parent.asInstanceOf[TileEntity]).yCoord
    }
    return 0
  }

  override def z: Double =
  {
    if(parent != null) {
      if (parent.isInstanceOf[TMultiPart])
        return (parent.asInstanceOf[TMultiPart]).z
      else if (parent.isInstanceOf[TileEntity])
        return (parent.asInstanceOf[TileEntity]).zCoord
    }
    return 0
  }
}
