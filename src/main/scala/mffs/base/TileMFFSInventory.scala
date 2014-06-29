package mffs.base

import java.util.{HashSet, Set}

import com.google.common.io.ByteArrayDataInput
import cw.TileMFFS
import mffs.ModularForceFieldSystem
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.{IInventory, ISidedInventory}
import net.minecraft.item.ItemStack
import net.minecraft.nbt.{NBTTagCompound, NBTTagList}
import net.minecraft.tileentity.{TileEntity, TileEntityChest}
import net.minecraftforge.common.util.ForgeDirection
import resonant.lib.content.prefab.TInventory
import universalelectricity.core.transform.vector.Vector3

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

  def getCards: Set[ItemStack] =
  {
    val cards: Set[ItemStack] = new HashSet[ItemStack]
    cards.add(this.getStackInSlot(0))
    return cards
  }
}