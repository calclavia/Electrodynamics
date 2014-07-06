package mffs.production

import java.util.EnumSet

import com.google.common.io.ByteArrayDataInput
import mffs.base.TileModuleAcceptor
import mffs.util.FortronUtility
import mffs.item.card.ItemCardFrequency
import mffs.{ModularForceFieldSystem, Settings}
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import resonant.api.mffs.modules.IModule
import universalelectricity.core.transform.region.Cuboid

/**
 * A TileEntity that extract energy into Fortron.
 *
 * @author Calclavia
 */
object TileCoercionDeriver
{
  val FUEL_PROCESS_TIME = 10 * 20
  val MULTIPLE_PRODUCTION = 4

  /**
   * Ration from UE to Fortron. Multiply J by this value to convert to Fortron.
   */
  val UE_FORTRON_RATIO = 0.005f
  val ENERGY_LOSS = 1
  val SLOT_FREQUENCY = 0
  val SLOT_BATTERY = 1
  val SLOT_FUEL = 2

  /**
   * The amount of KiloWatts this machine uses.
   */
  val DEFAULT_WATTAGE = 5000000
}

class TileCoercionDeriver extends TileModuleAcceptor
{
  bounds = new Cuboid(0, 0, 0, 1, 0.8, 1)
  capacityBase = 30
  startModuleIndex = 3
  updateEnergyInfo()

  private def updateEnergyInfo()
  {
    this.energy.setCapacity(getWattage)
    this.energy.setMaxTransfer(getWattage / 20)
  }

  override def start()
  {
    super.start()
    updateEnergyInfo
  }

  override def updateEntity
  {
    super.updateEntity
    if (!worldObj.isRemote)
    {
      if (isActive)
      {
        if (isInversed && Settings.enableElectricity)
        {
          if (energy.getEnergy < energy.getEnergyCapacity)
          {
            val withdrawnElectricity: Long = (requestFortron(getProductionRate / 20, true) / UE_FORTRON_RATIO).asInstanceOf[Long]
            energy.receiveEnergy(withdrawnElectricity * ENERGY_LOSS, true)
          }
          recharge(getStackInSlot(SLOT_BATTERY))
          produce
        }
        else
        {
          if (this.getFortronEnergy < this.getFortronCapacity)
          {
            this.discharge(this.getStackInSlot(SLOT_BATTERY))
            if (this.energy.checkExtract || (!Settings.ENABLE_ELECTRICITY && this.isItemValidForSlot(SLOT_FUEL, this.getStackInSlot(SLOT_FUEL))))
            {
              this.fortronTank.fill(FortronUtility.getFortron(this.getProductionRate), true)
              this.energy.extractEnergy
              if (this.processTime == 0 && this.isItemValidForSlot(SLOT_FUEL, this.getStackInSlot(SLOT_FUEL)))
              {
                this.decrStackSize(SLOT_FUEL, 1)
                this.processTime = FUEL_PROCESS_TIME * Math.max(this.getModuleCount(ModularForceFieldSystem.itemModuleScale) / 20, 1)
              }
              if (this.processTime > 0)
              {
                this.processTime -= 1
                if (this.processTime < 1)
                {
                  this.processTime = 0
                }
              }
              else
              {
                this.processTime = 0
              }
            }
          }
        }
      }
    }
    else if (this.isActive)
    {
      this.animation += 1
    }
  }

  def getWattage: Long =
  {
    return (DEFAULT_WATTAGE + (DEFAULT_WATTAGE * (this.getModuleCount(ModularForceFieldSystem.itemModuleSpeed).asInstanceOf[Float] / 8.asInstanceOf[Float]))).asInstanceOf[Long]
  }

  def onReceiveEnergy(from: Nothing, receive: Long, doReceive: Boolean): Long =
  {
    if (!isInversed)
    {
      return super.onReceiveEnergy(from, receive, doReceive)
    }
    return receive
  }

  def onExtractEnergy(from: Nothing, extract: Long, doExtract: Boolean): Long =
  {
    if (isInversed)
    {
      return super.onExtractEnergy(from, extract, doExtract)
    }
    return 0
  }

  def onInventoryChanged
  {
    super.onInventoryChanged
    updateEnergyInfo
  }

  def getOutputDirections: EnumSet[Nothing] =
  {
    return EnumSet.allOf(classOf[Nothing])
  }

  /**
   * @return The Fortron production rate per tick!
   */
  def getProductionRate: Int =
  {
    if (this.isActive)
    {
      var production: Int = (getWattage.asInstanceOf[Float] / 20f * UE_FORTRON_RATIO * Settings.FORTRON_PRODUCTION_MULTIPLIER).asInstanceOf[Int]
      if (this.processTime > 0)
      {
        production *= MULTIPLE_PRODUCTION
      }
      return production
    }
    return 0
  }

  override def getSizeInventory: Int =
  {
    return 6
  }

  def canConsume: Boolean =
  {
    if (this.isActive && !this.isInversed)
    {
      return FortronUtility.getAmount(this.fortronTank) < this.fortronTank.getCapacity
    }
    return false
  }

  override def onReceivePacket(packetID: Int, dataStream: ByteArrayDataInput)
  {
    super.onReceivePacket(packetID, dataStream)
    if (packetID == TilePacketType.TOGGLE_MODE.ordinal)
    {
      this.isInversed = !this.isInversed
    }
  }

  override def readFromNBT(nbt: NBTTagCompound)
  {
    super.readFromNBT(nbt)
    this.processTime = nbt.getInteger("processTime")
    this.isInversed = nbt.getBoolean("isInversed")
  }

  override def writeToNBT(nbt: NBTTagCompound)
  {
    super.writeToNBT(nbt)
    nbt.setInteger("processTime", this.processTime)
    nbt.setBoolean("isInversed", this.isInversed)
  }

  override def isItemValidForSlot(slotID: Int, itemStack: ItemStack): Boolean =
  {
    if (itemStack != null)
    {
      if (slotID >= this.startModuleIndex)
      {
        return itemStack.getItem.isInstanceOf[IModule]
      }
      slotID match
      {
        case SLOT_FREQUENCY =>
          return itemStack.getItem.isInstanceOf[ItemCardFrequency]
        case SLOT_BATTERY =>
          return CompatibilityModule.isHandler(itemStack.getItem)
        case SLOT_FUEL =>
          return itemStack.isItemEqual(new ItemStack(Item.dyePowder, 1, 4)) || itemStack.isItemEqual(new ItemStack(Item.netherQuartz))
      }
    }
    return false
  }

  def canConnect(direction: Nothing, obj: AnyRef): Boolean =
  {
    return true
  }

  var processTime: Int = 0
  var isInversed: Boolean = false
}