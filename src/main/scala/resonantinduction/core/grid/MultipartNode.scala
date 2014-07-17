package resonantinduction.core.grid

import codechicken.multipart.TMultiPart
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import universalelectricity.api.core.grid.INodeProvider
import universalelectricity.core.grid.Node
import universalelectricity.core.transform.vector.VectorWorld

/**
 * A node that works with Forge Multipart
 * @author Calclavia
 */
abstract class MultipartNode[N <: Node[N]](parent: INodeProvider) extends Node[N](parent)
{
  override def world: World =
  {
    return if (parent.isInstanceOf[TMultiPart]) (parent.asInstanceOf[TMultiPart]).world else if (parent.isInstanceOf[TileEntity]) (parent.asInstanceOf[TileEntity]).getWorldObj else null
  }

  override def position: VectorWorld =
  {
    return if (parent.isInstanceOf[TMultiPart]) new VectorWorld(parent.asInstanceOf[TMultiPart].world, parent.asInstanceOf[TMultiPart].x, parent.asInstanceOf[TMultiPart].y, parent.asInstanceOf[TMultiPart].z) else if (parent.isInstanceOf[TileEntity]) new VectorWorld(parent.asInstanceOf[TileEntity]) else null
  }
}
