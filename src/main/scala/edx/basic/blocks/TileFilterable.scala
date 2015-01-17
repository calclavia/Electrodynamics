package edx.basic.blocks

import net.minecraft.block.material.Material
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.ForgeDirection
import resonant.api.tile.{IFilterable, IRotatable}
import resonant.lib.prefab.tile.mixed.TileInventory
import resonant.lib.transform.vector.Vector3

object TileFilterable
{
  final val FILTER_SLOT: Int = 0
  final val BATERY_DRAIN_SLOT: Int = 1
}

abstract class TileFilterable extends TileInventory(Material.wood) with IRotatable with IFilterable
{
  private var filterItem: ItemStack = null
  private var inverted: Boolean = false

  override def getSizeInventory: Int = 2

  /**
   * Allows filters to be placed inside of this block.
   */
  override def use(player: EntityPlayer, side: Int, hit: Vector3): Boolean =
  {
    val containingStack: ItemStack = getFilter
    if (containingStack != null)
    {
      if (!world.isRemote)
      {
        val dropStack: EntityItem = new EntityItem(world, player.posX, player.posY, player.posZ, containingStack)
        dropStack.delayBeforeCanPickup = 0
        world.spawnEntityInWorld(dropStack)
      }
      setFilter(null)
      return true
    }
    else
    {
      if (player.getCurrentEquippedItem != null)
      {
        if (player.getCurrentEquippedItem.getItem.isInstanceOf[ItemImprint])
        {
          setFilter(player.getCurrentEquippedItem)
          player.inventory.setInventorySlotContents(player.inventory.currentItem, null)
          return true
        }
      }
    }
    return false
  }

  def setFilter(filter: ItemStack)
  {
    this.filterItem = filter
    this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord)
  }

  def getFilter: ItemStack =
  {
    return this.filterItem
  }

  override def configure(player: EntityPlayer, side: Int, hit: Vector3): Boolean =
  {
    toggleInversion
    markUpdate
    markRender
    return true
  }

  def toggleInversion
  {
    setInverted(!isInverted)
  }

  def isInverted: Boolean =
  {
    return this.inverted
  }

  def setInverted(inverted: Boolean)
  {
    this.inverted = inverted
    this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord)
  }

  /**
   * Looks through the things in the filter and finds out which item is being filtered.
   *
   * @return Is this filterable block filtering this specific ItemStack?
   */
  def isFiltering(itemStack: ItemStack): Boolean =
  {
    if (this.getFilter != null && itemStack != null)
    {
      val checkStacks: java.util.List[ItemStack] = ItemImprint.getFilters(getFilter)
      if (checkStacks != null)
      {
        import scala.collection.JavaConversions._
        for (stack <- checkStacks)
        {
          if (stack.isItemEqual(itemStack))
          {
            return !inverted
          }
        }
      }
    }
    return inverted
  }

  override def getDirection: ForgeDirection =
  {
    return ForgeDirection.getOrientation(if (getBlockType != null) getBlockMetadata else 0)
  }

  override def setDirection(facingDirection: ForgeDirection)
  {
    this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, facingDirection.ordinal, 3)
  }

  override def writeToNBT(nbt: NBTTagCompound)
  {
    super.writeToNBT(nbt)
    nbt.setBoolean("inverted", inverted)
  }

  override def readFromNBT(nbt: NBTTagCompound)
  {
    super.readFromNBT(nbt)
    if (nbt.hasKey("filter"))
    {
      this.getInventory.setInventorySlotContents(0, ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("filter")))
    }
    inverted = nbt.getBoolean("inverted")
  }

  protected def isFunctioning: Boolean =
  {
    return true
  }

}