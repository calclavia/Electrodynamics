package mffs.production

import io.netty.buffer.ByteBuf
import mffs.base.{TileModuleAcceptor, TilePacketType}
import mffs.item.card.ItemCardFrequency
import mffs.util.FortronUtility
import mffs.{ModularForceFieldSystem, Settings}
import net.minecraft.init.Items
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import resonant.api.mffs.modules.IModule
import resonant.lib.content.prefab.TElectric
import universalelectricity.api.UniversalClass
import universalelectricity.compatibility.Compatibility
import universalelectricity.core.transform.region.Cuboid

/**
 * A TileEntity that extract energy into Fortron.
 *
 * @author Calclavia
 */
object TileCoercionDeriver
{
  val fuelProcessTime = 10 * 20
  val productionMultiplier = 4

  /**
   * Ration from UE to Fortron. Multiply J by this value to convert to Fortron.
   */
  val ueToFortronRatio = 0.005f
  val energyConversionPercentage = 1

  val slotFrequency = 0
  val slotBattery = 1
  val slotFuel = 2

  /**
   * The amount of power (watts) this machine uses.
   */
  val power = 5000000
}

@UniversalClass
class TileCoercionDeriver extends TileModuleAcceptor with TElectric
{
  var processTime: Int = 0
  var isInversed: Boolean = false

  bounds = new Cuboid(0, 0, 0, 1, 0.8, 1)
  capacityBase = 30
  startModuleIndex = 3
  maxSlots = 6

  override def start()
  {
    super.start()
  }

  override def update()
  {
    super.update()

    if (!worldObj.isRemote)
    {
      if (isActive)
      {
        if (isInversed && Settings.enableElectricity)
        {
          //TODO: Check this
          if (electricNode.getVoltage < 100)
          {
            val withdrawnElectricity = (requestFortron(productionRate / 20, true) / TileCoercionDeriver.ueToFortronRatio)
            electricNode.applyPower(withdrawnElectricity * TileCoercionDeriver.energyConversionPercentage)
          }

          recharge(getStackInSlot(TileCoercionDeriver.slotBattery))
        }
        else
        {
          if (getFortronEnergy < getFortronCapacity)
          {
            discharge(getStackInSlot(TileCoercionDeriver.slotBattery))
            val energy = electricNode.getEnergy(getVoltage)

            if (energy >= getPower || (!Settings.enableElectricity && isItemValidForSlot(TileCoercionDeriver.slotFuel, getStackInSlot(TileCoercionDeriver.slotFuel))))
            {
              fortronTank.fill(FortronUtility.getFortron(productionRate), true)
              electricNode.drawPower(getPower)

              if (processTime == 0 && isItemValidForSlot(TileCoercionDeriver.slotFuel, getStackInSlot(TileCoercionDeriver.slotFuel)))
              {
                decrStackSize(TileCoercionDeriver.slotFuel, 1)
                processTime = TileCoercionDeriver.fuelProcessTime * Math.max(this.getModuleCount(ModularForceFieldSystem.Items.moduleScale) / 20, 1)
              }

              if (processTime > 0)
              {
                processTime -= 1

                if (processTime < 1)
                {
                  processTime = 0
                }
              }
              else
              {
                processTime = 0
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

  def getPower: Double = TileCoercionDeriver.power + (TileCoercionDeriver.power * (getModuleCount(ModularForceFieldSystem.Items.moduleSpeed) / 8d))

  override def getVoltage = 1000D

  /**
   * @return The Fortron production rate per tick!
   */
  def productionRate: Int =
  {
    if (this.isActive)
    {
      var production = (getPower.asInstanceOf[Float] / 20f * TileCoercionDeriver.ueToFortronRatio * Settings.fortronProductionMultiplier).asInstanceOf[Int]

      if (processTime > 0)
      {
        production *= TileCoercionDeriver.productionMultiplier
      }

      return production
    }
    return 0
  }

  def canConsume: Boolean =
  {
    if (this.isActive && !this.isInversed)
    {
      return FortronUtility.getAmount(this.fortronTank) < this.fortronTank.getCapacity
    }
    return false
  }

  override def onReceivePacket(packetID: Int, dataStream: ByteBuf)
  {
    super.onReceivePacket(packetID, dataStream)

    if (packetID == TilePacketType.TOGGLE_MODE.id)
    {
      isInversed = !isInversed
    }
  }

  override def readFromNBT(nbt: NBTTagCompound)
  {
    super.readFromNBT(nbt)
    processTime = nbt.getInteger("processTime")
    isInversed = nbt.getBoolean("isInversed")
  }

  override def writeToNBT(nbt: NBTTagCompound)
  {
    super.writeToNBT(nbt)
    nbt.setInteger("processTime", processTime)
    nbt.setBoolean("isInversed", isInversed)
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
        case TileCoercionDeriver.slotFrequency =>
          return itemStack.getItem.isInstanceOf[ItemCardFrequency]
        case TileCoercionDeriver.slotBattery =>
          return Compatibility.isHandler(itemStack.getItem)
        case TileCoercionDeriver.slotFuel =>
          return itemStack.isItemEqual(new ItemStack(Items.dye, 1, 4)) || itemStack.isItemEqual(new ItemStack(Items.quartz))
      }
    }
    return false
  }

}