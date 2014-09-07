package resonantinduction.archaic.crate

import java.util.List

import codechicken.multipart.ControlKeyModifer
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP}
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.IIcon
import net.minecraft.world.World
import net.minecraftforge.oredict.OreDictionary
import resonant.content.spatial.block.SpatialBlock
import resonant.lib.wrapper.WrapList._
import resonantinduction.archaic.ArchaicBlocks
import resonantinduction.core.Reference

import scala.collection.JavaConversions._
import scala.util.control.Breaks._

/** A block that allows the placement of mass amount of a specific item within it. It will be allowed
  * to go on Conveyor Belts.
  *
  * NOTE: Crates should be upgraded with an item.
  *
  * @author DarkGuardsman */
object BlockCrate
{

  /** Puts an itemStack into the crate.
    *
    * @param tileEntity
    * @param itemStack */
  def addStackToCrate(tileEntity: TileCrate, itemStack: ItemStack): ItemStack =
  {
    if (itemStack == null || itemStack.getItem.isDamageable && itemStack.getItem.getDamage(itemStack) > 0)
    {
      return itemStack
    }
    val containingStack: ItemStack = tileEntity.getSampleStack
    if (containingStack == null || (containingStack.isItemEqual(itemStack) || (tileEntity.oreFilterEnabled && OreDictionary.getOreID(containingStack) == OreDictionary.getOreID(itemStack))))
    {
      val room: Int = Math.max((tileEntity.getInventory.getSizeInventory * 64) - (if (containingStack != null) containingStack.stackSize else 0), 0)
      if (itemStack.stackSize <= room)
      {
        tileEntity.addToStack(itemStack)
        return null
      }
      else
      {
        tileEntity.addToStack(itemStack, room)
        itemStack.stackSize -= room
      }
      return itemStack
    }
    if (itemStack.stackSize <= 0)
    {
      return null
    }
    return itemStack
  }
}

class BlockCrate extends SpatialBlock(Material.iron)
{

  private[crate] var blockIcon: IIcon = null
  private[crate] var advanced: IIcon = null
  private[crate] var elite: IIcon = null

  @SideOnly(Side.CLIENT) override def registerIcons(iconReg: IIconRegister)
  {
    this.blockIcon = iconReg.registerIcon(Reference.prefix + "crate_wood")
    this.advanced = iconReg.registerIcon(Reference.prefix + "crate_iron")
    this.elite = iconReg.registerIcon(Reference.prefix + "crate_steel")
  }

  @SideOnly(Side.CLIENT) override def getIcon(side: Int, meta: Int): IIcon =
  {
    if (meta == 1)
    {
      return advanced
    }
    else if (meta == 2)
    {
      return elite
    }
    return this.blockIcon
  }

  def onBlockClicked(world: World, x: Int, y: Int, z: Int, player: EntityPlayer)
  {
    if (!world.isRemote && world.getTileEntity(x, y, z).isInstanceOf[TileCrate])
    {
      val tileEntity: TileCrate = world.getTileEntity(x, y, z).asInstanceOf[TileCrate]
      this.tryEject(tileEntity, player, (System.currentTimeMillis - tileEntity.prevClickTime) < 200)
      tileEntity.prevClickTime = System.currentTimeMillis
    }
  }

  def tryEject(tileEntity: TileCrate, player: EntityPlayer, allMode: Boolean)
  {
    if (tileEntity.getSampleStack == null)
    {
      return
    }
    if (allMode && !player.isSneaking)
    {
      this.ejectItems(tileEntity, player, tileEntity.getSlotCount * 64)
    }
    else
    {
      if (player.isSneaking)
      {
        this.ejectItems(tileEntity, player, 64)
      }
      else
      {
        this.ejectItems(tileEntity, player, tileEntity.getSampleStack.getMaxStackSize)
      }
    }
  }

  /** Ejects and item out of the crate and spawn it under the player entity.
    *
    * @param tileEntity
    * @param player
    * @param requestSize - The maximum stack size to take out. Default should be 64.
    * @return True on success */
  def ejectItems(tileEntity: TileCrate, player: EntityPlayer, requestSize: Int): Boolean =
  {
    val world: World = tileEntity.getWorldObj
    if (!world.isRemote)
    {
      val sampleStack: ItemStack = tileEntity.getSampleStack
      var ammountEjected: Int = 0
      if (sampleStack != null && requestSize > 0)
      {
        for (slot <- 0 until tileEntity.getInventory.getSizeInventory)
        {
          {
            val slotStack: ItemStack = tileEntity.getInventory.getStackInSlot(slot)
            if (slotStack != null && slotStack.stackSize > 0)
            {
              val amountToTake: Int = Math.min(slotStack.stackSize, requestSize)
              val dropStack: ItemStack = slotStack.copy
              dropStack.stackSize = amountToTake
              if (!player.inventory.addItemStackToInventory(dropStack))
              {
                tileEntity.getInventory.setInventorySlotContents(slot, slotStack)
                ammountEjected += amountToTake - slotStack.stackSize
                break
              }
              else
              {
                tileEntity.getInventory.setInventorySlotContents(slot, null)
                ammountEjected += amountToTake
              }
            }
            if (ammountEjected >= requestSize)
            {
              return true
            }
          }
        }
        player.inventoryContainer.detectAndSendChanges()
        tileEntity.onInventoryChanged
        return true
      }
    }
    return false
  }

  def onMachineActivated(world: World, x: Int, y: Int, z: Int, player: EntityPlayer, side: Int, hitX: Float, hitY: Float, hitZ: Float): Boolean =
  {
    if (!world.isRemote && world.getTileEntity(x, y, z).isInstanceOf[TileCrate])
    {
      val tile: TileCrate = world.getTileEntity(x, y, z).asInstanceOf[TileCrate]
      if (ControlKeyModifer.isControlDown(player))
      {
        if (player.getCurrentEquippedItem != null && (!player.getCurrentEquippedItem.getItem.isDamageable || player.getCurrentEquippedItem.getItem.getDamage(player.getCurrentEquippedItem) > 0))
        {
          val filter: ItemStack = player.getCurrentEquippedItem.copy
          filter.stackSize = 0
          tile.setFilter(filter)
        }
        else
        {
          tile.setFilter(null)
        }
      }
      else
      {
        val current: ItemStack = player.inventory.getCurrentItem
        if (player.capabilities.isCreativeMode)
        {
          if (side == 1)
          {
            if (current != null && tile.getSampleStack == null)
            {
              val cStack: ItemStack = current.copy
              cStack.stackSize = TileCrate.getSlotCount(world.getBlockMetadata(x, y, z)) * 64
              BlockCrate.addStackToCrate(tile, cStack)
            }
          }
          else if (hitY <= 0.5)
          {
            tryEject(tile, player, System.currentTimeMillis - tile.prevClickTime < 250)
          }
          else
          {
            tryInsert(tile, player, System.currentTimeMillis - tile.prevClickTime < 250)
          }
        }
        else
        {
          tryInsert(tile, player, System.currentTimeMillis - tile.prevClickTime < 250)
        }
      }
      tile.prevClickTime = System.currentTimeMillis
    }
    return true
  }

  /** Try to inject it into the crate. Otherwise, look around for nearby crates and try to put them
    * in. */
  def tryInsert(tileEntity: TileCrate, player: EntityPlayer, allMode: Boolean, doSearch: Boolean)
  {
    val success: Boolean = if (allMode) insertAllItems(tileEntity, player) else insertCurrentItem(tileEntity, player)

    val pathfinder: PathfinderCrate = new PathfinderCrate().init(tileEntity)

    for (checkTile <- pathfinder.iteratedNodes)
    {
      if (checkTile.isInstanceOf[TileCrate])
      {
        this.tryInsert((checkTile.asInstanceOf[TileCrate]), player, allMode, false)
      }
    }
  }

  def tryInsert(tileEntity: TileCrate, player: EntityPlayer, allMode: Boolean)
  {
    tryInsert(tileEntity, player, allMode, true)
  }

  /** Inserts a the itemStack the player is holding into the crate. */
  def insertCurrentItem(tileEntity: TileCrate, player: EntityPlayer): Boolean =
  {
    val currentStack: ItemStack = player.getCurrentEquippedItem
    if (currentStack != null)
    {
      if (currentStack.getItem() == Item.getItemFromBlock(ArchaicBlocks.blockCrate))
      {
        val containedStack: ItemStack = ItemBlockCrate.getContainingItemStack(currentStack)
        val crateStack: ItemStack = tileEntity.getSampleStack
        if (containedStack != null && (crateStack == null || ItemStack.areItemStacksEqual(containedStack, crateStack)))
        {
          val returned: ItemStack = BlockCrate.addStackToCrate(tileEntity, containedStack)
          ItemBlockCrate.setContainingItemStack(currentStack, returned)
          return true
        }
      }
      else
      {
        if (tileEntity.getSampleStack != null)
        {
          if (!(tileEntity.getSampleStack.isItemEqual(currentStack) || (tileEntity.oreFilterEnabled && !(OreDictionary.getOreName(OreDictionary.getOreID(tileEntity.getSampleStack)) == "Unknown") && OreDictionary.getOreID(tileEntity.getSampleStack) == OreDictionary.getOreID(currentStack))))
          {
            return false
          }
        }
        player.inventory.setInventorySlotContents(player.inventory.currentItem, BlockCrate.addStackToCrate(tileEntity, currentStack))
        return true
      }
    }
    return false
  }

  /** Inserts all items of the same type this player has into the crate.
    *
    * @return True on success */
  def insertAllItems(tileEntity: TileCrate, player: EntityPlayer): Boolean =
  {
    var requestStack: ItemStack = null
    if (tileEntity.getSampleStack != null)
    {
      requestStack = tileEntity.getSampleStack.copy
    }
    if (requestStack == null)
    {
      requestStack = player.getCurrentEquippedItem
    }
    if (requestStack != null && requestStack.getItem != Item.getItemFromBlock(ArchaicBlocks.blockCrate))
    {
      var success: Boolean = false
      for (i <- 0 until player.inventory.getSizeInventory)
      {
        val currentStack: ItemStack = player.inventory.getStackInSlot(i)
        if (currentStack != null)
        {
          if (requestStack.isItemEqual(currentStack))
          {
            player.inventory.setInventorySlotContents(i, BlockCrate.addStackToCrate(tileEntity, currentStack))
            if (player.isInstanceOf[EntityPlayerMP])
            {
              (player.asInstanceOf[EntityPlayerMP]).sendContainerToPlayer(player.inventoryContainer)
            }
            success = true
          }
        }
      }
      return success
    }
    return false
  }

  def damageDropped(metadata: Int): Int =
  {
    return metadata
  }

  def createNewTileEntity(var1: World): TileEntity =
  {
    return new TileCrate
  }

  override def getSubBlocks(item: Item, par2CreativeTabs: CreativeTabs, par3List: List[_])
  {
    par3List.add(new ItemStack(item, 1, 0))
    par3List.add(new ItemStack(item, 1, 1))
    par3List.add(new ItemStack(item, 1, 2))
  }
}