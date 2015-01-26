package mffs.base

import io.netty.buffer.ByteBuf
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import resonantengine.core.network.discriminator.PacketType
import resonantengine.lib.content.prefab.TInventory
import resonantengine.lib.utility.inventory.TPrefabInventory
import resonantengine.lib.wrapper.ByteBufWrapper._

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

  override def read(buf: ByteBuf, id: Int, packetType: PacketType)
  {
    super.read(buf, id, packetType)

    if (id == TilePacketType.description.id || id == TilePacketType.inventory.id)
    {
      getInventory.load(buf.readTag())
    }
  }

  /**
   * Inventory Methods
   */
  def getCards: Set[ItemStack] = Set(getStackInSlot(0))
}