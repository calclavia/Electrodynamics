package mffs.base

import java.util._

import com.google.common.io.ByteArrayDataInput
import mffs.{ModularForceFieldSystem, Settings}
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.fluids.FluidContainerRegistry
import resonant.api.mffs.ICache
import resonant.api.mffs.modules.{IModule, IModuleAcceptor}

import scala.collection.JavaConversions._

abstract class TileModuleAcceptor extends TileFortron with IModuleAcceptor with ICache
{
  /**
   * Caching for the module stack data. This is used to reduce calculation time. Cache gets reset
   * when inventory changes.
   */
  val cache = new HashMap[String, AnyRef]
  var startModuleIndex = 0
  var endModuleIndex = this.getSizeInventory - 1
  /**
   * Used for client-side only.
   */
  var clientFortronCost = 0
  protected var capacityBase = 500
  protected var capacityBoost = 5

  override def getPacketData(packetID: Int): List[_] =
  {
    val data = super.getPacketData(packetID)

    if (packetID == TilePacketType.DESCRIPTION.ordinal)
    {
      data.add(this.getFortronCost)
    }

    return data
  }

  override def onReceivePacket(packetID: Int, dataStream: ByteArrayDataInput)
  {
    super.onReceivePacket(packetID, dataStream)

    if (packetID == TilePacketType.DESCRIPTION.ordinal)
    {
      clientFortronCost = dataStream.readInt
    }
  }

  override def start()
  {
    super.start()
    fortronTank.setCapacity((this.getModuleCount(ModularForceFieldSystem.itemModuleCapacity) * this.capacityBoost + this.capacityBase) * FluidContainerRegistry.BUCKET_VOLUME)
  }

  def consumeCost()
  {
    if (getFortronCost() > 0)
    {
      requestFortron(getFortronCost(), true)
    }
  }

  def getModule(module: IModule): ItemStack =
  {
    val cacheID: String = "getModule_" + module.hashCode
    if (Settings.USE_CACHE)
    {
      if (this.cache.containsKey(cacheID))
      {
        if (this.cache.get(cacheID).isInstanceOf[ItemStack])
        {
          return this.cache.get(cacheID).asInstanceOf[ItemStack]
        }
      }
    }
    val returnStack: ItemStack = new ItemStack(module.asInstanceOf[Item], 0)

    for (comparedModule <- getModuleStacks)
    {
      if (comparedModule.getItem eq module)
      {
        returnStack.stackSize += comparedModule.stackSize
      }
    }
    if (Settings.USE_CACHE)
    {
      this.cache.put(cacheID, returnStack.copy)
    }
    return returnStack
  }

  def getModuleCount(module: IModule, slots: Int*): Int =
  {
    var count: Int = 0
    if (module != null)
    {
      var cacheID: String = "getModuleCount_" + module.hashCode
      if (slots != null)
      {
        cacheID += "_" + Arrays.hashCode(slots)
      }
      if (Settings.USE_CACHE)
      {
        if (this.cache.containsKey(cacheID))
        {
          if (this.cache.get(cacheID).isInstanceOf[Integer])
          {
            return this.cache.get(cacheID).asInstanceOf[Integer]
          }
        }
      }
      if (slots != null && slots.length > 0)
      {
        for (slotID <- slots)
        {
          if (this.getStackInSlot(slotID) != null)
          {
            if (this.getStackInSlot(slotID).getItem eq module)
            {
              count += this.getStackInSlot(slotID).stackSize
            }
          }
        }
      }
      else
      {
        for (itemStack <- getModuleStacks)
        {
          if (itemStack.getItem eq module)
          {
            count += itemStack.stackSize
          }
        }
      }
      if (Settings.USE_CACHE)
      {
        this.cache.put(cacheID, count)
      }
    }
    return count
  }

  @SuppressWarnings(Array("unchecked"))
  def getModuleStacks(slots: Int*): Set[ItemStack] =
  {
    var cacheID: String = "getModuleStacks_"
    if (slots != null)
    {
      cacheID += Arrays.hashCode(slots)
    }
    if (Settings.USE_CACHE)
    {
      if (this.cache.containsKey(cacheID))
      {
        if (this.cache.get(cacheID).isInstanceOf[Set[_]])
        {
          return this.cache.get(cacheID).asInstanceOf[Set[ItemStack]]
        }
      }
    }
    val modules: Set[ItemStack] = new HashSet[ItemStack]
    if (slots == null || slots.length <= 0)
    {
      {
        var slotID: Int = startModuleIndex
        while (slotID <= endModuleIndex)
        {
          {
            val itemStack: ItemStack = this.getStackInSlot(slotID)
            if (itemStack != null)
            {
              if (itemStack.getItem.isInstanceOf[IModule])
              {
                modules.add(itemStack)
              }
            }
          }
          ({
            slotID += 1;
            slotID - 1
          })
        }
      }
    }
    else
    {
      for (slotID <- slots)
      {
        val itemStack: ItemStack = this.getStackInSlot(slotID)
        if (itemStack != null)
        {
          if (itemStack.getItem.isInstanceOf[IModule])
          {
            modules.add(itemStack)
          }
        }
      }
    }
    if (Settings.USE_CACHE)
    {
      this.cache.put(cacheID, modules)
    }
    return modules
  }

  @SuppressWarnings(Array("unchecked"))
  def getModules(slots: Int*): Set[IModule] =
  {
    var cacheID: String = "getModules_"
    if (slots != null)
    {
      cacheID += Arrays.hashCode(slots)
    }
    if (Settings.USE_CACHE)
    {
      if (this.cache.containsKey(cacheID))
      {
        if (this.cache.get(cacheID).isInstanceOf[Set[_]])
        {
          return this.cache.get(cacheID).asInstanceOf[Set[IModule]]
        }
      }
    }
    val modules: Set[IModule] = new HashSet[IModule]
    if (slots == null || slots.length <= 0)
    {
      {
        var slotID: Int = startModuleIndex
        while (slotID <= endModuleIndex)
        {
          {
            val itemStack: ItemStack = this.getStackInSlot(slotID)
            if (itemStack != null)
            {
              if (itemStack.getItem.isInstanceOf[IModule])
              {
                modules.add(itemStack.getItem.asInstanceOf[IModule])
              }
            }
          }
          ({
            slotID += 1;
            slotID - 1
          })
        }
      }
    }
    else
    {
      for (slotID <- slots)
      {
        val itemStack: ItemStack = this.getStackInSlot(slotID)
        if (itemStack != null)
        {
          if (itemStack.getItem.isInstanceOf[IModule])
          {
            modules.add(itemStack.getItem.asInstanceOf[IModule])
          }
        }
      }
    }
    if (Settings.USE_CACHE)
    {
      this.cache.put(cacheID, modules)
    }

    return modules
  }

  /**
   * Returns Fortron cost in ticks.
   */
  final def getFortronCost: Int =
  {
    if (this.worldObj.isRemote)
    {
      return this.clientFortronCost
    }
    val cacheID: String = "getFortronCost"
    if (Settings.USE_CACHE)
    {
      if (this.cache.containsKey(cacheID))
      {
        val obj: AnyRef = this.cache.get(cacheID)
        if (obj != null && obj.isInstanceOf[Integer])
        {
          return obj.asInstanceOf[Integer]
        }
      }
    }
    val result: Int = this.doGetFortronCost
    if (Settings.USE_CACHE)
    {
      this.cache.put(cacheID, result)
    }
    return this.doGetFortronCost
  }

  protected def doGetFortronCost: Int =
  {
    var cost: Float = 0
    for (itemStack <- this.getModuleStacks)
    {
      if (itemStack != null)
      {
        cost += itemStack.stackSize * (itemStack.getItem.asInstanceOf[IModule]).getFortronCost(this.getAmplifier)
      }
    }
    return Math.round(cost)
  }

  protected def getAmplifier: Float =
  {
    return 1
  }

  def onInventoryChanged
  {
    super.onInventoryChanged
    this.fortronTank.setCapacity((this.getModuleCount(ModularForceFieldSystem.itemModuleCapacity) * this.capacityBoost + this.capacityBase) * FluidContainerRegistry.BUCKET_VOLUME)
    this.clearCache
  }

  def getCache(cacheID: String): AnyRef =
  {
    return this.cache.get(cacheID)
  }

  def clearCache(cacheID: String)
  {
    this.cache.remove(cacheID)
  }

  def clearCache
  {
    this.cache.clear
  }

  override def readFromNBT(nbt: NBTTagCompound)
  {
    clearCache
    super.readFromNBT(nbt)
    this.clientFortronCost = nbt.getInteger("fortronCost")
  }

  override def writeToNBT(nbt: NBTTagCompound)
  {
    super.writeToNBT(nbt)
    nbt.setInteger("fortronCost", this.clientFortronCost)
  }

}