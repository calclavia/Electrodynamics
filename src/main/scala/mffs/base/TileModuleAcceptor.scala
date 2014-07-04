package mffs.base

import java.util._

import com.google.common.io.ByteArrayDataInput
import mffs.{ModularForceFieldSystem, Settings, TCache}
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.fluids.FluidContainerRegistry
import resonant.api.mffs.modules.{IModule, IModuleAcceptor}

import scala.collection.JavaConversions._

abstract class TileModuleAcceptor extends TileFortron with IModuleAcceptor with TCache
{
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
    val cacheID = "getModule_" + module.hashCode

    if (getCache(classOf[ItemStack], cacheID)) return getCache(classOf[ItemStack], cacheID)

    val returnStack: ItemStack = new ItemStack(module.asInstanceOf[Item], 0)

    for (comparedModule <- getModuleStacks)
    {
      if (comparedModule.getItem eq module)
      {
        returnStack.stackSize += comparedModule.stackSize
      }
    }
    cache(cacheID, returnStack.copy)


    return returnStack
  }

  def getModuleCount(module: IModule, slots: Int*): Int =
  {
    var count: Int = 0
    if (module != null)
    {
      var cacheID = "getModuleCount_" + module.hashCode

      if (slots != null)
      {
        cacheID += "_" + Arrays.hashCode(slots)
      }

      if (getCache(classOf[Integer], cacheID)) return getCache(classOf[Integer], cacheID)

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
      cache(cacheID, count)

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

    if (hasCache(classOf[Set[ItemStack]], cacheID)) return getCache(classOf[Set[ItemStack]], cacheID)

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
    cache(cacheID, modules)

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

    if (hasCache(classOf[Set[IModule]], cacheID)) return getCache(classOf[Set[IModule]], cacheID)

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
    cache(cacheID, modules)


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

    val cacheID = "getFortronCost"

    if (hasCache(classOf[Integer], cacheID)) return getCache(classOf[Integer], cacheID)

    val result = doGetFortronCost

    cache(cacheID, result)

    return result
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
    clearCache()
  }

  override def readFromNBT(nbt: NBTTagCompound)
  {
    clearCache()
    super.readFromNBT(nbt)
    this.clientFortronCost = nbt.getInteger("fortronCost")
  }

  override def writeToNBT(nbt: NBTTagCompound)
  {
    super.writeToNBT(nbt)
    nbt.setInteger("fortronCost", this.clientFortronCost)
  }

}