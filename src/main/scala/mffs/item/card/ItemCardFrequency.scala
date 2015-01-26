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
import resonantengine.api.item.IItemFrequency
import resonantengine.api.network.IPacketReceiver
import resonantengine.core.network.discriminator.PacketType
import resonantengine.lib.utility.LanguageUtility
import resonantengine.lib.wrapper.CollectionWrapper._

class ItemCardFrequency extends ItemCard with IItemFrequency with IPacketReceiver
{
  override def addInformation(itemStack: ItemStack, par2EntityPlayer: EntityPlayer, list: List[_], par4: Boolean)
  {
    list.add(LanguageUtility.getLocal("info.cardFrequency.freq") + " " + getEncodedFrequency(itemStack))
  }

  def getEncodedFrequency(itemStack: ItemStack): String =
  {
    return Hashing.md5().hashInt(getFrequency(itemStack)).toString.take(12)
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

  override def read(data: ByteBuf, player: EntityPlayer, packet: PacketType)
  {
    setFrequency(data.readInt(), player.getCurrentEquippedItem)
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