package mffs.item.card

import java.util.List

import com.google.common.hash.Hashing
import mffs.ModularForceFieldSystem
import mffs.item.gui.EnumGui

class ItemCardFrequency extends ItemCard with IItemFrequency with IPacketReceiver
{
	override def addInformation(Item: Item, par2EntityPlayer: EntityPlayer, list: List[_], par4: Boolean)
  {
	  list.add(LanguageUtility.getLocal("info.cardFrequency.freq") + " " + getEncodedFrequency(Item))
  }

	def getEncodedFrequency(Item: Item): String =
  {
	  return Hashing.md5().hashInt(getFrequency(Item)).toString.take(12)
  }

  /**
   * Frequency methods
   **/
  override def getFrequency(Item: Item): Int =
  {
	  if (Item != null)
    {
		if (Item.getTagCompound == null)
      {
		  Item.setTagCompound(new NBTTagCompound)
      }
		return Item.getTagCompound.getInteger("frequency")
    }
    return 0
  }

	override def onItemRightClick(Item: Item, world: World, player: EntityPlayer): Item =
  {
    if (!world.isRemote)
    {
      /**
       * Open item GUI
       */
      player.openGui(ModularForceFieldSystem, EnumGui.frequency.id, world, 0, 0, 0)
    }

	  return Item
  }

  override def read(data: ByteBuf, player: EntityPlayer, packet: PacketType)
  {
    setFrequency(data.readInt(), player.getCurrentEquippedItem)
  }

	override def setFrequency(frequency: Int, Item: Item)
  {
	  if (Item != null)
    {
		if (Item.getTagCompound == null)
      {
		  Item.setTagCompound(new NBTTagCompound)
      }
		Item.getTagCompound.setInteger("frequency", frequency)
    }
  }

}