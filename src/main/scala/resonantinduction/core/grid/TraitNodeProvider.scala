package resonantinduction.core.grid

import codechicken.multipart.{PartMap, TileMultipart}
import net.minecraftforge.common.util.ForgeDirection
import universalelectricity.api.core.grid.{INode, INodeProvider}

trait TraitNodeProvider extends TileMultipart with INodeProvider
{
  def getNode(nodeType: Class[INode], from: ForgeDirection): INode =
  {
    var part = partMap(from.ordinal)

    if (part == null)
    {
      part = partMap(PartMap.CENTER.ordinal)
    }
    if (part.isInstanceOf[INodeProvider])
    {
      return part.asInstanceOf[INodeProvider].getNode(nodeType, from)
    }

    return null
  }
}