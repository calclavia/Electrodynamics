package resonantinduction.mechanical.mech

import codechicken.multipart.ControlKeyModifer
import cpw.mods.fml.common.network.ByteBufUtils
import io.netty.buffer.ByteBuf
import net.minecraft.block.material.Material
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.Packet
import resonant.content.prefab.java.TileNode
import resonant.engine.ResonantEngine
import resonant.lib.grid.node.TSpatialNodeProvider
import resonant.lib.network.discriminator.{PacketTile, PacketType}
import resonant.lib.network.handle.{TPacketSender, TPacketReceiver, IPacketReceiver, IPacketIDReceiver}
import resonant.lib.transform.vector.Vector3
import resonant.lib.network.ByteBufWrapper._
/** Prefab for resonantinduction.mechanical tiles
  *
  * @author Calclavia */
abstract class TileMechanical(material: Material) extends TileNode(material) with TSpatialNodeProvider with TPacketSender with  TPacketReceiver
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
        sendRotationPacket()
        mechanicalNode.markRotationUpdate = false
        mechanicalNode.markTorqueUpdate = false
      }
    }
  }

  override def use(player: EntityPlayer, side: Int, hit: Vector3): Boolean =
  {
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
              frame.showDebugFrame
            }
            else
            {
              frame.closeDebugFrame
              frame = null
            }
          }
        }
      }
    }
    return false
  }

  override def getDescriptionPacket: Packet =
  {
    val tag: NBTTagCompound = new NBTTagCompound
    writeToNBT(tag)
    return ResonantEngine.instance.packetHandler.toMCPacket(new PacketTile(xi, yi, zi, Array(nbt_packet_id, tag)))
  }

  /** Sends the torque and angular velocity to the client */
  private def sendRotationPacket()
  {
    ResonantEngine.instance.packetHandler.sendToAllAround(new PacketTile(xi, yi, zi, Array(vel_packet_id, mechanicalNode.angularVelocity, mechanicalNode.torque)), this)
  }

  override def write(buf: ByteBuf, id: Int): Unit =
  {
    super.write(buf, id)

    id match
    {
      case 0 =>
      case 1 => buf <<< mechanicalNode.angularVelocity <<<  mechanicalNode.torque
    }
  }

  override def read(buf: ByteBuf, id: Int, packetType: PacketType)
  {
    super.read(buf,id,packetType)
    if (world.isRemote)
    {
      if (id == nbt_packet_id)
      {
        readFromNBT(ByteBufUtils.readTag(buf))
        return true
      }
      else if (id == vel_packet_id)
      {
        mechanicalNode.angularVelocity = buf.readDouble
        mechanicalNode.torque = buf.readDouble
        return true
      }
    }
    return false
  }

  override def readFromNBT(nbt: NBTTagCompound)
  {
    super.readFromNBT(nbt)
    mechanicalNode.load(nbt)
  }

  override def writeToNBT(nbt: NBTTagCompound)
  {
    super.writeToNBT(nbt)
    mechanicalNode.save(nbt)
  }
}