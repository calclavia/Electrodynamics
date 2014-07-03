package mffs.base

import com.google.common.io.ByteArrayDataInput
import mffs.ModularForceFieldSystem
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import resonant.lib.content.prefab.TInventory

/**
 * All TileEntities that have an inventory should extend this.
 *
 * @author Calclavia
 */
abstract class TileMFFSInventory extends TileMFFS with TInventory
{
  override def getPacketData(packetID: Int): List[_] =
  {
    val data = super.getPacketData(packetID)

    if (packetID == TileMFFS.TilePacketType.DESCRIPTION.ordinal)
    {
      val nbt: NBTTagCompound = new NBTTagCompound
      this.writeToNBT(nbt)
      data.add(nbt)
    }
    return data
  }

  def onReceivePacket(packetID: Int, dataStream: ByteArrayDataInput)
  {
    super.onReceivePacket(packetID, dataStream)
    if (this.worldObj.isRemote)
    {
      if (packetID == TilePacketType.DESCRIPTION.ordinal || packetID == TilePacketType.INVENTORY.ordinal)
      {
        this.readFromNBT(PacketHandler.readNBTTagCompound(dataStream))
      }
    }
  }

  def sendInventoryToClients
  {
    val nbt: NBTTagCompound = new NBTTagCompound
    this.writeToNBT(nbt)
    PacketHandler.sendPacketToClients(ModularForceFieldSystem.PACKET_TILE.getPacket(this, TilePacketType.INVENTORY.ordinal, nbt))
  }

  /**
   * Inventory Methods
   */
  def getCards: Set[ItemStack] = Set(getStackInSlot(0))
}