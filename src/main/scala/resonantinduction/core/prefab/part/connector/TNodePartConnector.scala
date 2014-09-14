package resonantinduction.core.prefab.part.connector

import codechicken.multipart.TMultiPart
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.ForgeDirection
import universalelectricity.api.core.grid.{INode, INodeProvider, ISave}

/**
 * A node trait that can be mixed into any multipart nodes.
 * @author Calclavia
 */
trait TNodePartConnector extends TMultiPart with INodeProvider
{
  protected lazy val node: INode = null

  override def onWorldJoin()
  {
    node.reconstruct()
  }

  override def onNeighborChanged()
  {
    node.reconstruct()
  }

  override def onWorldSeparate()
  {
    node.deconstruct()
  }

  override def save(nbt: NBTTagCompound)
  {
    super.save(nbt)

    if (node.isInstanceOf[ISave])
      node.asInstanceOf[ISave].save(nbt)
  }

  override def load(nbt: NBTTagCompound)
  {
    super.load(nbt)

    if (node.isInstanceOf[ISave])
      node.asInstanceOf[ISave].load(nbt)
  }

  override def getNode(nodeType: Class[_ <: INode], from: ForgeDirection): INode =
  {
    if (nodeType == node.getClass)
      return node
    return null
  }
}
