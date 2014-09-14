package resonantinduction.core.prefab

import codechicken.multipart.PartMap
import codechicken.multipart.TMultiPart
import codechicken.multipart.TileMultipart
import net.minecraftforge.common.util.ForgeDirection
import universalelectricity.api.core.grid.INode
import universalelectricity.api.core.grid.INodeProvider

/**
 * Created by robert on 8/13/2014.
 */
class TNodeProvider extends TileMultipart with INodeProvider
{
  def getNode(nodeType: Class[_ <: INode], from: ForgeDirection): INode =
  {
    var nodePart: TMultiPart = partMap(from.ordinal)
    if (nodePart == null)
    {
      nodePart = partMap(PartMap.CENTER.ordinal)
    }
    if (nodePart.isInstanceOf[INodeProvider])
    {
      return (nodePart.asInstanceOf[INodeProvider]).getNode(nodeType, from)
    }
    return null
  }
}