package resonantinduction.archaic.crate

import java.util.List

import net.minecraft.block.Block
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{ItemBlock, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.potion.{Potion, PotionEffect}
import net.minecraft.world.World
import resonant.lib.utility.LanguageUtility

import scala.util.control.Breaks._
import resonant.lib.wrapper.WrapList._
object ItemBlockCrate
{
  def setContainingItemStack(itemStack: ItemStack, containingStack: ItemStack)
  {
    if (itemStack.stackTagCompound == null)
    {
      itemStack.setTagCompound(new NBTTagCompound)
    }
    if (containingStack != null)
    {
      val itemTagCompound: NBTTagCompound = new NBTTagCompound
      containingStack.stackSize = Math.abs(containingStack.stackSize)
      containingStack.writeToNBT(itemTagCompound)
      itemStack.getTagCompound.setTag("Item", itemTagCompound)
      itemStack.getTagCompound.setInteger("Count", containingStack.stackSize)
    }
    else
    {
      itemStack.getTagCompound.setTag("Item", new NBTTagCompound)
      itemStack.getTagCompound.setInteger("Count", 0)
    }
  }

  def getContainingItemStack(itemStack: ItemStack): ItemStack =
  {
    if (itemStack.stackTagCompound == null)
    {
      itemStack.setTagCompound(new NBTTagCompound)
      return null
    }
    val itemTagCompound: NBTTagCompound = itemStack.getTagCompound.getCompoundTag("Item")
    val containingStack: ItemStack = ItemStack.loadItemStackFromNBT(itemTagCompound)
    if (containingStack != null)
    {
      containingStack.stackSize = itemStack.getTagCompound.getInteger("Count")
    }
    return containingStack
  }
}

class ItemBlockCrate(block: Block) extends ItemBlock(block: Block)
{
  this.setHasSubtypes(true)

  override def getUnlocalizedName(itemStack: ItemStack): String =
  {
    return getUnlocalizedName() + "." + itemStack.getItemDamage
  }

  override def addInformation(itemStack: ItemStack, par2EntityPlayer: EntityPlayer, list: List[_], par4: Boolean)
  {
    super.addInformation(itemStack, par2EntityPlayer, list, par4)
    val containingStack: ItemStack = ItemBlockCrate.getContainingItemStack(itemStack)
    if (containingStack != null)
    {
      val s = LanguageUtility.getLocal("crate.tooltip.amount") + " " + containingStack.stackSize
      list.add(containingStack.getDisplayName)
      list.add(s)
    }
  }

  override def getItemStackLimit(stack: ItemStack): Int =
  {
    val containingStack: ItemStack = ItemBlockCrate.getContainingItemStack(stack)
    if (containingStack != null)
    {
      return 1
    }
    return this.maxStackSize
  }

  override def onUpdate(itemStack: ItemStack, par2World: World, entity: Entity, par4: Int, par5: Boolean)
  {
    if (entity.isInstanceOf[EntityPlayer])
    {
      val player: EntityPlayer = entity.asInstanceOf[EntityPlayer]
      val containingStack: ItemStack = ItemBlockCrate.getContainingItemStack(itemStack)
      if (containingStack != null && !player.capabilities.isCreativeMode)
      {
        player.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 5, (containingStack.stackSize.asInstanceOf[Float] / TileCrate.getSlotCount(itemStack.getItemDamage).asInstanceOf[Float]).asInstanceOf[Int] * 5))
      }
    }
  }

  override def getMetadata(metadata: Int): Int =
  {
    return metadata
  }

  override def placeBlockAt(stack: ItemStack, player: EntityPlayer, world: World, x: Int, y: Int, z: Int, side: Int, hitX: Float, hitY: Float, hitZ: Float, metadata: Int): Boolean =
  {
    if (super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, metadata))
    {
      var containingItem: ItemStack = ItemBlockCrate.getContainingItemStack(stack)
      if (world.getTileEntity(x, y, z) != null && containingItem != null)
      {
        if (containingItem.stackSize > 0)
        {
          val tileEntity: TileCrate = world.getTileEntity(x, y, z).asInstanceOf[TileCrate]
          var count: Int = containingItem.stackSize
          for (slot <- 0 until tileEntity.getInventory.getSizeInventory)
          {
            val stackSize: Int = Math.min(64, count)
            tileEntity.getInventory.setInventorySlotContents(slot, new ItemStack(containingItem.getItem, stackSize, containingItem.getItemDamage))
            count -= stackSize
            if (count <= 0)
            {
              containingItem = null
              break
            }
          }
          tileEntity.buildSampleStack
        }
      }
    }
    return true
  }
}