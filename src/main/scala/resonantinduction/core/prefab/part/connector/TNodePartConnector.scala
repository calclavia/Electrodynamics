package resonantinduction.core.prefab.part.connector

import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.ForgeDirection
import resonant.api.ISave
import resonant.api.grid.{INode, INodeProvider}

/**
 * A node trait that can be mixed into any multipart nodes. Mixing this trait will cause nodes to reconstruct/deconstruct when needed.
 * @author Calclavia
 */
trait TNodePartConnector extends PartAbstract with INodeProvider
{
  protected lazy val node: INode = null

  override def start()
  {
    super.start()
    node.reconstruct()
  }

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

  override def getNode[N <: INode](nodeType: Class[_ <: N], from: ForgeDirection): N =
  {
    if (nodeType.isAssignableFrom(node.getClass))
      return node.asInstanceOf[N]

    return null.asInstanceOf[N]
  }
}
