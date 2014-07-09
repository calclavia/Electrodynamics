package mffs.item.card

import java.util.List

import mffs.Settings
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ChatComponentText
import net.minecraft.world.World
import resonant.api.blocks.IBlockFrequency
import resonant.api.items.IItemFrequency
import resonant.lib.utility.LanguageUtility
import resonant.lib.wrapper.WrapList._

class ItemCardFrequency extends ItemCard with IItemFrequency
{
  override def addInformation(itemStack: ItemStack, par2EntityPlayer: EntityPlayer, list: List[_], par4: Boolean)
  {
    //TODO: Hash this
    list.add(LanguageUtility.getLocal("info.cardFrequency.freq") + " " + getFrequency(itemStack))
  }

  def getFrequency(itemStack: ItemStack): Int =
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

  def setFrequency(frequency: Int, itemStack: ItemStack)
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

  override def onItemRightClick(itemStack: ItemStack, world: World, player: EntityPlayer): ItemStack =
  {
    if (!world.isRemote)
    {
      if (player.isSneaking)
      {
        this.setFrequency(world.rand.nextInt(Math.pow(10, (Settings.maxFrequencyDigits - 1)).asInstanceOf[Int]), itemStack)
        player.addChatMessage(new ChatComponentText(LanguageUtility.getLocal("message.cardFrequency.generated") + " " + getFrequency(itemStack)))
      }
    }
    return itemStack
  }

  override def onItemUse(itemStack: ItemStack, player: EntityPlayer, world: World, x: Int, y: Int, z: Int, side: Int, hitX: Float, hitY: Float, hitZ: Float): Boolean =
  {
    val tileEntity: TileEntity = world.getTileEntity(x, y, z)
    if (tileEntity.isInstanceOf[IBlockFrequency])
    {
      if (!world.isRemote)
      {
        (tileEntity.asInstanceOf[IBlockFrequency]).setFrequency(this.getFrequency(itemStack))
        world.markBlockForUpdate(x, y, z)
        player.addChatMessage(new ChatComponentText(LanguageUtility.getLocal("message.cardFrequency.set").replaceAll("%p", "" + this.getFrequency(itemStack))))
      }
      return true
    }
    return false
  }
}