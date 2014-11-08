package resonantinduction.mechanical.mech

import codechicken.multipart.ControlKeyModifer
import io.netty.buffer.ByteBuf
import net.minecraft.block.material.Material
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import resonant.content.prefab.java.TileNode
import resonant.engine.ResonantEngine
import resonant.lib.grid.node.TSpatialNodeProvider
import resonant.lib.network.ByteBufWrapper._
import resonant.lib.network.discriminator.PacketType
import resonant.lib.network.handle.{TPacketReceiver, TPacketSender}
import resonant.lib.transform.vector.Vector3

/** Prefab for resonantinduction.mechanical tiles
  *
  * @author Calclavia */
abstract class TileMechanical(material: Material) extends TileNode(material) with TSpatialNodeProvider with TPacketSender with TPacketReceiver
{
  /** Node that handles most mechanical actions */
  var mechanicalNode = new MechanicalNode(this)

  /** External debug GUI */
  var frame: DebugFrameMechanical = null

  nodes.add(mechanicalNode)

  override def update()
  {
    super.update()

    if (frame != null)
    {
      frame.update()

      if (!frame.isVisible)
      {
        frame.dispose()
        frame = null
      }
    }

    if (!world.isRemote)
    {
      if (ticks % 3 == 0 && (mechanicalNode.markTorqueUpdate || mechanicalNode.markRotationUpdate))
      {
        sendPacket(1)
        mechanicalNode.markRotationUpdate = false
        mechanicalNode.markTorqueUpdate = false
      }
    }
  }

  override def use(player: EntityPlayer, side: Int, hit: Vector3): Boolean =
  {
    //Debugging
    val itemStack: ItemStack = player.getHeldItem

    if (ResonantEngine.runningAsDev)
    {
      if (itemStack != null && !world.isRemote)
      {
        if (itemStack.getItem == Items.stick)
        {
          if (ControlKeyModifer.isControlDown(player))
          {
            if (frame == null)
            {
              frame = new DebugFrameMechanical(this)
              frame.showDebugFrame()
            }
            else
            {
              frame.closeDebugFrame()
              frame = null
            }
          }
        }
      }
    }
    return false
  }

  override def write(buf: ByteBuf, id: Int): Unit =
  {
    super.write(buf, id)

    id match
    {
      case 0 =>
      case 1 => buf <<< mechanicalNode.angularVelocity <<< mechanicalNode.torque
    }
  }

  override def read(buf: ByteBuf, id: Int, packetType: PacketType)
  {
    super.read(buf, id, packetType)

    id match
    {
      case 0 =>
      case 1 =>
      {
        mechanicalNode.angularVelocity = buf.readDouble
        mechanicalNode.torque = buf.readDouble
      }
    }
  }
}