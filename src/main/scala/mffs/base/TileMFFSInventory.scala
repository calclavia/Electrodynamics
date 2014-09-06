package mffs.base

import io.netty.buffer.ByteBuf
import mffs.ModularForceFieldSystem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import resonant.lib.content.prefab.TInventory
import resonant.lib.network.ByteBufWrapper.ByteBufWrapper
import resonant.lib.network.discriminator.{PacketTile, PacketType}
import resonant.lib.utility.inventory.TPrefabInventory

/**
 * All TileEntities that have an inventory should extend this.
 *
 * @author Calclavia
 */
abstract class TileMFFSInventory extends TileMFFS with TInventory with TPrefabInventory
{
  override def write(buf: ByteBuf, id: Int)
  {
    super.write(buf, id)

    if (id == TilePacketType.description.id)
    {
      val nbt = new NBTTagCompound
      getInventory.save(nbt)
      buf <<< nbt
    }
  }

  override def read(buf: ByteBuf, id: Int, player: EntityPlayer, packet: PacketType): Boolean =
  {
    super.read(buf, id, player, packet)

    if (id == TilePacketType.description.id || id == TilePacketType.inventory.id)
    {
      getInventory.load(buf.readTag())
    }


    return false
  }

  def sendInventoryToClients
  {
    val nbt: NBTTagCompound = new NBTTagCompound
    this.writeToNBT(nbt)
    ModularForceFieldSystem.packetHandler.sendToAll(new PacketTile(this, TilePacketType.inventory.id: Integer, nbt))
  }

  /**
   * Inventory Methods
   */
  def getCards: Set[ItemStack] = Set(getStackInSlot(0))
}