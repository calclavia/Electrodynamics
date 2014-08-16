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
    return if (parent.isInstanceOf[TMultiPart]) (parent.asInstanceOf[TMultiPart]).world else if (parent.isInstanceOf[TileEntity]) (parent.asInstanceOf[TileEntity]).getWorldObj else null
  }

  override def x: Double =
  {
    return if (parent.isInstanceOf[TMultiPart]) (parent.asInstanceOf[TMultiPart]).x else if (parent.isInstanceOf[TileEntity]) (parent.asInstanceOf[TileEntity]).xCoord else 0
  }

  override def y: Double =
  {
    return if (parent.isInstanceOf[TMultiPart]) (parent.asInstanceOf[TMultiPart]).y else if (parent.isInstanceOf[TileEntity]) (parent.asInstanceOf[TileEntity]).yCoord else 0
  }

  override def z: Double =
  {
    return if (parent.isInstanceOf[TMultiPart]) (parent.asInstanceOf[TMultiPart]).z else if (parent.isInstanceOf[TileEntity]) (parent.asInstanceOf[TileEntity]).zCoord else 0
  }
}
