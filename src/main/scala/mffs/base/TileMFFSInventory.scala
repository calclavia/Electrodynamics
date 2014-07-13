package mffs.base

import cpw.mods.fml.common.network.ByteBufUtils
import io.netty.buffer.ByteBuf
import mffs.ModularForceFieldSystem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import resonant.lib.content.prefab.TInventory
import resonant.lib.network.discriminator.{PacketTile, PacketType}
import resonant.lib.utility.inventory.TPrefabInventory
import resonant.lib.network.ByteBufWrapper.ByteBufWrapper
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

    if (id == TilePacketType.descrption.id)
    {
      val nbt = new NBTTagCompound
      getInventory.save(nbt)
      buf <<< nbt
    }
  }

  override def read(buf: ByteBuf, id: Int, player: EntityPlayer, packet: PacketType)
  {
    super.read(buf, id, player, packet)

    if (id == TilePacketType.descrption.id || id == TilePacketType.INVENTORY.id)
    {
      getInventory.load(buf.readTag())
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