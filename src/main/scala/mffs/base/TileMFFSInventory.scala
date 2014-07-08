package mffs.base

import cpw.mods.fml.common.network.ByteBufUtils
import io.netty.buffer.ByteBuf
import mffs.ModularForceFieldSystem
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import resonant.lib.content.prefab.TInventory
import resonant.lib.network.PacketTile
import resonant.lib.utility.inventory.TPrefabInventory

/**
 * All TileEntities that have an inventory should extend this.
 *
 * @author Calclavia
 */
abstract class TileMFFSInventory extends TileMFFS with TInventory with TPrefabInventory
{
  override def getPacketData(packetID: Int): List[AnyRef] =
  {
    if (packetID == TilePacketType.DESCRIPTION.id)
    {
      val nbt = new NBTTagCompound
      getInventory.save(nbt)
      return super.getPacketData(packetID) :+ nbt
    }

    return super.getPacketData(packetID)
  }

  override def onReceivePacket(packetID: Int, dataStream: ByteBuf)
  {
    super.onReceivePacket(packetID, dataStream)

    if (worldObj.isRemote)
    {
      if (packetID == TilePacketType.DESCRIPTION.id || packetID == TilePacketType.INVENTORY.id)
      {
        getInventory.load(ByteBufUtils.readTag(dataStream))
      }
    }
  }

  def sendInventoryToClients
  {
    val nbt: NBTTagCompound = new NBTTagCompound
    this.writeToNBT(nbt)
    ModularForceFieldSystem.packetHandler.sendToAll(new PacketTile(this, TilePacketType.INVENTORY.id: Integer, nbt))
  }

  /**
   * Inventory Methods
   */
  def getCards: Set[ItemStack] = Set(getStackInSlot(0))
}