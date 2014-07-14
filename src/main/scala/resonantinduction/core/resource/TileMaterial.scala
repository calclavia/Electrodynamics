package resonantinduction.core.resource

import com.google.common.io.ByteArrayDataInput
import net.minecraft.block.material.Material
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.Packet
import resonant.content.spatial.block.SpatialTile
import resonant.lib.network.handle.TPacketReceiver
import resonantinduction.core.ResonantInduction

/**
 * A tile that stores the material name.
 *
 * @author Calclavia
 */
abstract class TileMaterial(mateiral: Material) extends SpatialTile(material) with TPacketReceiver
{
  var name: String = null

  def getColor: Int =
  {
    return ResourceGenerator.getColor(name)
  }

  override def canUpdate: Boolean =
  {
    return false
  }

  def onReceivePacket(data: ByteArrayDataInput, player: EntityPlayer, extra: AnyRef*)
  {
    name = data.readUTF
    markRender
  }

  override def getDescriptionPacket: Packet =
  {
    if (name != null)
    {
      return ResonantInduction.packetHandler.toMCPacket(this, name)
    }

    return null
  }

  override def readFromNBT(nbt: NBTTagCompound)
  {
    super.readFromNBT(nbt)
    name = nbt.getString("name")
  }

  override def writeToNBT(nbt: NBTTagCompound)
  {
    super.writeToNBT(nbt)
    if (name != null)
    {
      nbt.setString("name", name)
    }
  }
}