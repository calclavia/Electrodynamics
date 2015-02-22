package mffs.item.card

import java.util.List

/**
 * A linking card used to link machines in specific positions.
 *
 * @author Calclavia
 */
class ItemCardLink extends ItemCard with ICoordLink
{
  @SideOnly(Side.CLIENT)
  override def addInformation(Item: Item, entityplayer: EntityPlayer, list: List[_], flag: Boolean)
  {
	  super.addInformation(Item, entityplayer, list, flag)

	  if (hasLink(Item))
    {
		val vec: VectorWorld = getLink(Item)
      val block = vec.getBlock(entityplayer.worldObj)

      if (block != null)
      {
        list.add("info.item.linkedWith".getLocal + " " + block.getLocalizedName)
      }

      list.add(vec.xi + ", " + vec.yi + ", " + vec.zi)
      list.add("info.item.dimension".getLocal + " " + vec.world.provider.getDimensionName)
    }
    else
    {
      list.add("info.item.notLinked".getLocal)
    }
  }

	def hasLink(Item: Item): Boolean = getLink(Item) != null

	def getLink(Item: Item): VectorWorld =
  {
	  if (Item.stackTagCompound == null || !Item.getTagCompound.hasKey("link"))
    {
      return null
    }
	  return new VectorWorld(Item.getTagCompound.getCompoundTag("link"))
  }

	override def onItemUse(Item: Item, player: EntityPlayer, world: World, x: Int, y: Int, z: Int, par7: Int, par8: Float, par9: Float, par10: Float): Boolean =
  {
    if (!world.isRemote)
    {
      val vector: VectorWorld = new VectorWorld(world, x, y, z)
		this.setLink(Item, vector)

      if (vector.getBlock(world) != null)
      {
        player.addChatMessage(new ChatComponentTranslation("info.item.linkedWith", x + ", " + y + ", " + z + " - " + vector.getBlock(world).getLocalizedName))
      }
    }
    return true
  }

	def setLink(Item: Item, vec: VectorWorld)
  {
	  if (Item.getTagCompound == null)
    {
		Item.setTagCompound(new NBTTagCompound)
    }

	  Item.getTagCompound.setTag("link", vec.toNBT)
  }

	def clearLink(Item: Item)
  {
	  Item.getTagCompound.removeTag("link")
  }
}