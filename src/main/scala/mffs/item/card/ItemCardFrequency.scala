package mffs.item.card

import java.util.List

import com.google.common.hash.Hashing
import io.netty.buffer.ByteBuf
import mffs.ModularForceFieldSystem
import mffs.item.gui.EnumGui
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.World
import resonant.api.items.IItemFrequency
import resonant.lib.network.discriminator.PacketType
import resonant.lib.network.handle.TPacketReceiver
import resonant.lib.utility.LanguageUtility
import resonant.lib.wrapper.WrapList._

class ItemCardFrequency extends ItemCard with IItemFrequency with TPacketReceiver
{
  override def addInformation(itemStack: ItemStack, par2EntityPlayer: EntityPlayer, list: List[_], par4: Boolean)
  {
    list.add(LanguageUtility.getLocal("info.cardFrequency.freq") + " " + getEncodedFrequency(itemStack))
  }

  override def onItemRightClick(itemStack: ItemStack, world: World, player: EntityPlayer): ItemStack =
  {
    if (!world.isRemote)
    {
      /**
       * Open item GUI
       */
      player.openGui(ModularForceFieldSystem, EnumGui.frequency.id, world, 0, 0, 0)
    }

    return itemStack
  }

  def getEncodedFrequency(itemStack: ItemStack): String =
  {
    return Hashing.md5().hashInt(getFrequency(itemStack)).toString.take(12)
  }

  override def read(data: ByteBuf, player: EntityPlayer, packet: PacketType)
  {
    setFrequency(data.readInt(), player.getCurrentEquippedItem)
  }

  /**
   * Frequency methods
   **/
  override def getFrequency(itemStack: ItemStack): Int =
  {
    if (itemStack != null)
    {
      if (itemStack.getTagCompound == null)
      {
        itemStack.setTagCompound(new NBTTagCompound)
      }
      return itemStack.getTagCompound.getInteger("frequency")
    }
    return 0
  }

  override def setFrequency(frequency: Int, itemStack: ItemStack)
  {
    if (itemStack != null)
    {
      if (itemStack.getTagCompound == null)
      {
        itemStack.setTagCompound(new NBTTagCompound)
      }
      itemStack.getTagCompound.setInteger("frequency", frequency)
    }
  }

}