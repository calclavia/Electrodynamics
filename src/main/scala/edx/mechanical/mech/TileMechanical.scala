package edx.mechanical.mech

import edx.mechanical.mech.grid.NodeMechanical
import io.netty.buffer.ByteBuf
import net.minecraft.block.material.Material
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.common.util.ForgeDirection
import resonantengine.lib.grid.core.TBlockNodeProvider
import resonantengine.lib.network.discriminator.PacketType
import resonantengine.prefab.network.{TPacketReceiver, TPacketSender}
import resonantengine.lib.prefab.tile.spatial.ResonantTile
import resonantengine.lib.prefab.tile.traits.TRotatable
import resonantengine.lib.transform.vector.Vector3
import resonantengine.lib.wrapper.ByteBufWrapper._

import scala.collection.convert.wrapAll._

/** Prefab for resonantinduction.mechanical tiles
  *
  * @author Calclavia */
abstract class TileMechanical(material: Material) extends ResonantTile(material: Material) with TRotatable with TBlockNodeProvider with TPacketSender with TPacketReceiver
{
  /** Node that handles most mechanical actions */
  private var _mechanicalNode: NodeMechanical = null

  override def setDirection(direction: ForgeDirection): Unit =
  {
    super.setDirection(direction)

    if (!world.isRemote)
      nodes.foreach(_.reconstruct())
  }

  override def use(player: EntityPlayer, side: Int, hit: Vector3): Boolean =
  {
    if (!world.isRemote)
    {
      println(mechanicalNode + " in " + mechanicalNode.grid)
      mechanicalNode.reconstruct()
    }

    return false
  }

  override def write(buf: ByteBuf, id: Int)
  {
    super.write(buf, id)

    id match
    {
      case 0 =>
      case 1 => buf <<< mechanicalNode.angularVelocity.toFloat
    }
  }

  override def read(buf: ByteBuf, id: Int, packetType: PacketType)
  {
    super.read(buf, id, packetType)

    id match
    {
      case 0 =>
      case 1 => mechanicalNode.angularVelocity = buf.readFloat()
    }
  }

  def mechanicalNode = _mechanicalNode

  def mechanicalNode_=(newNode: NodeMechanical)
{
  _mechanicalNode = newNode
  mechanicalNode.onVelocityChanged = () => sendPacket(1)
  nodes.removeAll(nodes.filter(_.isInstanceOf[NodeMechanical]))
  nodes.add(mechanicalNode)
  }

  mechanicalNode = new NodeMechanical(this)





}