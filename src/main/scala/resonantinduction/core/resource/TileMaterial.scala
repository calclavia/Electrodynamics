package resonantinduction.core.resource

import io.netty.buffer.ByteBuf
import net.minecraft.block.material.Material
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.Packet
import resonant.content.spatial.block.SpatialTile
import resonant.lib.network.ByteBufWrapper.ByteBufWrapper
import resonant.lib.network.discriminator.{PacketTile, PacketType}
import resonant.lib.network.handle.TPacketReceiver
import resonantinduction.core.ResonantInduction

/**
 * A tile that stores the material name.
 *
 * @author Calclavia
 */
abstract class TileMaterial(material: Material) extends SpatialTile(material) with TPacketReceiver
{
  var materialName: String = null

  def getColor: Int =
  {
    return ResourceGenerator.getColor(materialName)
  }

  override def canUpdate: Boolean =
  {
    return false
  }

  /**
   * Reads a packet
   * @param buf   - data encoded into the packet
   * @param player - player that is receiving the packet
   * @param packet - The packet instance that was sending this packet.
   */
  override def read(buf: ByteBuf, player: EntityPlayer, packet: PacketType)
  {
    materialName = buf.readString()
    markRender()
  }

  override def getDescriptionPacket: Packet =
  {
    if (materialName != null)
    {
      return ResonantInduction.packetHandler.toMCPacket(new PacketTile(this) <<< materialName)
    }

    return null
  }

  override def readFromNBT(nbt: NBTTagCompound)
  {
    super.readFromNBT(nbt)
    materialName = nbt.getString("name")
  }

  override def writeToNBT(nbt: NBTTagCompound)
  {
    super.writeToNBT(nbt)
    if (materialName != null)
    {
      nbt.setString("name", materialName)
    }
  }
}