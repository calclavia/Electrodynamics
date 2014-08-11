package resonantinduction.archaic.crate

import java.util
import java.util.List

import io.netty.buffer.ByteBuf
import net.minecraft.block.material.Material
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.oredict.OreDictionary
import resonant.api.IFilterable
import resonant.api.IRemovable.ISneakPickup
import resonant.lib.content.prefab.java.TileInventory
import resonant.lib.network.ByteBufWrapper._
import resonant.lib.network.discriminator.{PacketTile, PacketType}
import resonant.lib.network.handle.TPacketReceiver
import resonantinduction.archaic.ArchaicBlocks

/** Basic single stack inventory.
  * <p/>
  * TODO: Add filter-locking feature. Put filter in, locks the crate to only use that item.
  *
  * @author DarkGuardsman */
object TileCrate {
  /** Gets the slot count for the crate meta */
  def getSlotCount(metadata: Int): Int = {
    if (metadata >= 2) {
      return 256
    }
    else if (metadata >= 1) {
      return 64
    }
    return 32
  }

  /** max meta size of the crate */
  final val maxSize: Int = 2
}

class TileCrate extends TileInventory(Material.rock) with TPacketReceiver with IFilterable with ISneakPickup {

  /** delay from last click */
  var prevClickTime: Long = -1000
  /** Check to see if oreName items can be force stacked */
  var oreFilterEnabled: Boolean = false
  /** Collective total stack of all inv slots */
  private var sampleStack: ItemStack = null
  private var filterStack: ItemStack = null
  private var updateTick: Long = 1
  private var doUpdate: Boolean = false

  override def getSizeInventory: Int = TileCrate.getSlotCount(getBlockMetadata)

  override def updateEntity {
    super.updateEntity
    if (!worldObj.isRemote) {
      this.writeToNBT(new NBTTagCompound)
      if (ticks % updateTick == 0) {
        updateTick = 5 + worldObj.rand.nextInt(50)
        doUpdate = true
      }
      if (doUpdate) {
        doUpdate = false
        sendPacket(getDescPacket)
      }
    }
  }
  override protected lazy val inventory = new InventoryCrate(this)

  /** Gets the sample stack that represent the total inventory */
  def getSampleStack: ItemStack = {
    if (this.sampleStack == null) {
      this.buildSampleStack
    }
    return this.sampleStack
  }

  /** Builds the sample stack using the inventory as a point of reference. Assumes all items match
    * each other, and only takes into account stack sizes */
  def buildSampleStack {
    buildSampleStack(true)
  }

  def buildSampleStack(buildInv: Boolean) {
    if (worldObj == null || !worldObj.isRemote) {
      var newSampleStack: ItemStack = null
      var rebuildBase: Boolean = false
      for (slot <- 0 until getSizeInventory) {

        val slotStack: ItemStack = this.getInventory.getStackInSlot(slot)
        if (slotStack != null && slotStack.getItem != null && slotStack.stackSize > 0) {
          if (newSampleStack == null) {
            newSampleStack = slotStack.copy
          }
          else {
            newSampleStack.stackSize += slotStack.stackSize
          }
          if (slotStack.stackSize > slotStack.getMaxStackSize) {
            rebuildBase = true
          }
        }
      }



      if (newSampleStack == null || newSampleStack.getItem == null || newSampleStack.stackSize <= 0) {
        this.sampleStack = if (this.getFilter != null) this.getFilter.copy else null
      }
      else {
        this.sampleStack = newSampleStack.copy
      }
      if (buildInv && this.sampleStack != null && (rebuildBase || this.getInventory.getContainedItems.length > this.getSizeInventory)) {
        this.getInventory.asInstanceOf[InventoryCrate].buildInventory(this.sampleStack)
      }
    }
    }


  def addStackToStorage(stack: ItemStack): ItemStack = {
    return BlockCrate.addStackToCrate(this, stack)
  }

  /** Adds an item to the stack */
  def addToStack(stack: ItemStack, amount: Int) {
    if (stack != null) {
      val newStack: ItemStack = stack.copy
      newStack.stackSize = amount
      this.addToStack(newStack)
    }
  }

  /** Adds the stack to the sample stack */
  def addToStack(stack: ItemStack) {
    if (stack != null && stack.stackSize > 0) {
      if (this.getSampleStack == null) {
        this.sampleStack = stack
        getInventory.asInstanceOf[InventoryCrate].buildInventory(getSampleStack)
      }
      else if (this.getSampleStack.isItemEqual(stack) || (this.oreFilterEnabled && OreDictionary.getOreID(getSampleStack) == OreDictionary.getOreID(stack))) {
        getSampleStack.stackSize += stack.stackSize
        getInventory.asInstanceOf[InventoryCrate].buildInventory(getSampleStack)
      }
    }
  }

  override def decrStackSize(slot: Int, amount: Int): ItemStack = {
    if (sampleStack != null) {
      var var3: ItemStack = null
      if (sampleStack.stackSize <= amount) {
        var3 = sampleStack
        sampleStack = null
        this.onInventoryChanged
        getInventory.asInstanceOf[InventoryCrate].buildInventory(getSampleStack)
        return var3
      }
      else {
        var3 = sampleStack.splitStack(amount)
        if (sampleStack.stackSize == 0) {
          sampleStack = null
        }
        getInventory.asInstanceOf[InventoryCrate].buildInventory(getSampleStack)
        onInventoryChanged
        return var3
      }
    }
    else {
      return null
    }
  }

  def onInventoryChanged {
    if (worldObj != null && !worldObj.isRemote) doUpdate = true
  }

  override def canStore(stack: ItemStack, slot: Int, side: ForgeDirection): Boolean = {
    return getSampleStack == null || stack != null && (stack.isItemEqual(getSampleStack) || (this.oreFilterEnabled && OreDictionary.getOreID(getSampleStack) == OreDictionary.getOreID(stack)))
  }

  /** Gets the current slot count for the crate */
  def getSlotCount: Int = {
    if (this.worldObj == null) {
      return TileCrate.getSlotCount(TileCrate.maxSize)
    }
    return TileCrate.getSlotCount(this.getBlockMetadata)
  }

  def read(data: ByteBuf, player: EntityPlayer, packet: PacketType)
  {
    if (this.worldObj.isRemote) {
      try {
        if (data.readBoolean) {
          this.sampleStack = ItemStack.loadItemStackFromNBT(data.readTag())
          this.sampleStack.stackSize = data.readInt
        }
        else {
          this.sampleStack = null
        }
      }
      catch {
        case e: Exception => {
          e.printStackTrace
        }
      }
    }
  }

  override def getDescPacket: PacketTile = {
    var packet: PacketTile = new PacketTile(this)
    this.buildSampleStack
    val stack: ItemStack = this.getSampleStack
    if (stack != null) {
      packet <<< true
      packet <<< stack
      packet <<< stack.stackSize
    }
    else {
      packet <<< false
    }
    return packet
  }

  /** NBT Data */
  override def readFromNBT(nbt: NBTTagCompound) {
    super.readFromNBT(nbt)
    var stack: ItemStack = null
    val count: Int = nbt.getInteger("Count")
    if (nbt.hasKey("itemID")) {
      stack = new ItemStack(Item.getItemById(nbt.getInteger("itemID")), count, nbt.getInteger("itemMeta"))
    }
    else {
      stack = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("stack"))
      if (stack != null) {
        stack.stackSize = count
      }
    }
    if (stack != null && stack.getItem != null && stack.stackSize > 0) {
      this.sampleStack = stack
      this.getInventory.asInstanceOf[InventoryCrate].buildInventory(this.sampleStack)
    }
    this.oreFilterEnabled = nbt.getBoolean("oreFilter")
    if (nbt.hasKey("filter")) {
      filterStack = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("filter"))
    }
  }

  override def writeToNBT(nbt: NBTTagCompound) {
    super.writeToNBT(nbt)
    this.buildSampleStack(false)
    val stack: ItemStack = this.getSampleStack
    if (stack != null) {
      nbt.setInteger("Count", stack.stackSize)
      nbt.setTag("stack", stack.writeToNBT(new NBTTagCompound))
    }
    nbt.setBoolean("oreFilter", this.oreFilterEnabled)
    if (this.filterStack != null) {
      nbt.setTag("filter", filterStack.writeToNBT(new NBTTagCompound))
    }
  }

  def getFilter: ItemStack = {
    return this.filterStack
  }

  def setFilter(filter: ItemStack) {
    this.filterStack = filter
    this.onInventoryChanged
  }

  @SuppressWarnings(Array("unchecked")) def getRemovedItems(entity: EntityPlayer): List[ItemStack] = {
    val list = new util.ArrayList[ItemStack]()
    val sampleStack: ItemStack = getSampleStack
    val drop: ItemStack = new ItemStack(Item.getItemFromBlock(ArchaicBlocks.blockCrate), 1, this.getBlockMetadata)
    if (sampleStack != null && sampleStack.stackSize > 0)
    {
      ItemBlockCrate.setContainingItemStack(drop, sampleStack)
    }
    list.add(drop)
    return list
  }
}