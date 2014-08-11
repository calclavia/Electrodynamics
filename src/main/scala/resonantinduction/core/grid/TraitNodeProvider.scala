package resonantinduction.core.grid

import codechicken.multipart.{PartMap, TileMultipart}
import net.minecraftforge.common.util.ForgeDirection
import universalelectricity.api.core.grid.{INode, INodeProvider}

trait TraitNodeProvider extends TileMultipart with INodeProvider
{
  override def getNode(nodeType: Class[_ <: INode], from: ForgeDirection): INode =
  {
    var nodePart = partMap(from.ordinal)

    if (nodePart == null)
    {
      nodePart = partMap(PartMap.CENTER.ordinal)
    }
    if (nodePart.isInstanceOf[INodeProvider])
    {
      return nodePart.asInstanceOf[INodeProvider].getNode(nodeType, from)
    }

    return null
  }
}