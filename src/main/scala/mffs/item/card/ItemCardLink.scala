package mffs.item.card

import java.util.List

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ChatComponentTranslation
import net.minecraft.world.World
import resonant.api.mffs.card.ICoordLink
import resonant.lib.wrapper.StringWrapper._
import resonant.lib.wrapper.WrapList._
import universalelectricity.core.transform.vector.VectorWorld

/**
 * A linking card used to link machines in specific positions.
 *
 * @author Calclavia
 */
class ItemCardLink extends ItemCard with ICoordLink
{
  @SideOnly(Side.CLIENT)
  override def addInformation(itemstack: ItemStack, entityplayer: EntityPlayer, list: List[_], flag: Boolean)
  {
    super.addInformation(itemstack, entityplayer, list, flag)

    if (hasLink(itemstack))
    {
      val vec: VectorWorld = getLink(itemstack)
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

  override def onItemUse(itemStack: ItemStack, player: EntityPlayer, world: World, x: Int, y: Int, z: Int, par7: Int, par8: Float, par9: Float, par10: Float): Boolean =
  {
    if (!world.isRemote)
    {
      val vector: VectorWorld = new VectorWorld(world, x, y, z)
      this.setLink(itemStack, vector)

      if (vector.getBlock(world) != null)
      {
        player.addChatMessage(new ChatComponentTranslation("info.item.linkedWith", x + ", " + y + ", " + z + " - " + vector.getBlock(world).getLocalizedName))
      }
    }
    return true
  }

  def hasLink(itemStack: ItemStack): Boolean = getLink(itemStack) != null

  def getLink(itemStack: ItemStack): VectorWorld =
  {
    if (itemStack.stackTagCompound == null || !itemStack.getTagCompound.hasKey("link"))
    {
      return null
    }
    return new VectorWorld(itemStack.getTagCompound.getCompoundTag("link"))
  }

  def setLink(itemStack: ItemStack, vec: VectorWorld)
  {
    if (itemStack.getTagCompound == null)
    {
      itemStack.setTagCompound(new NBTTagCompound)
    }

    itemStack.getTagCompound.setTag("link", vec.toNBT)
  }

  def clearLink(itemStack: ItemStack)
  {
    itemStack.getTagCompound.removeTag("link")
  }
}