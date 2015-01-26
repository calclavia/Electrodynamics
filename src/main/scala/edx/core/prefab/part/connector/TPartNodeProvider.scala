package edx.core.prefab.part.connector

import java.util.{List => JList}

import codechicken.multipart.TMultiPart
import net.minecraft.nbt.NBTTagCompound
import resonant.api.ISave
import resonant.lib.grid.core.TNodeProvider

import scala.collection.convert.wrapAll._

/**
 * A node trait that can be mixed into any multipart nodes. Mixing this trait will cause nodes to reconstruct/deconstruct when needed.
 * @author Calclavia
 */
trait TPartNodeProvider extends PartAbstract with TNodeProvider
{
  override def start()
  {
    super.start()

    if (!world.isRemote)
      nodes.foreach(_.reconstruct())
  }

  override def onWorldJoin()
  {
    if (!world.isRemote)
      nodes.foreach(_.reconstruct())
  }

  override def onNeighborChanged()
  {
    if (!world.isRemote)
      nodes.foreach(_.reconstruct())
  }

  override def onPartChanged(part: TMultiPart)
  {
    if (!world.isRemote)
      nodes.foreach(_.reconstruct())
  }

  override def onWorldSeparate()
  {
    if (!world.isRemote)
      nodes.foreach(_.deconstruct())
  }

  override def save(nbt: NBTTagCompound)
  {
    super.save(nbt)
    nodes.filter(_.isInstanceOf[ISave]).foreach(_.asInstanceOf[ISave].save(nbt))
  }

  override def load(nbt: NBTTagCompound)
  {
    super.load(nbt)
    nodes.filter(_.isInstanceOf[ISave]).foreach(_.asInstanceOf[ISave].load(nbt))
  }
}
