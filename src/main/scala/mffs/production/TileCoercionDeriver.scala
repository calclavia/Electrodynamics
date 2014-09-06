package mffs.production

import cpw.mods.fml.relauncher.{Side, SideOnly}
import io.netty.buffer.ByteBuf
import mffs.base.{TileModuleAcceptor, TilePacketType}
import mffs.item.card.ItemCardFrequency
import mffs.util.FortronUtility
import mffs.{Content, Settings}
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.ForgeDirection
import resonant.api.mffs.modules.IModule
import resonant.lib.content.prefab.TElectric
import resonant.lib.network.ByteBufWrapper.ByteBufWrapper
import resonant.lib.network.discriminator.PacketType
import universalelectricity.api.UniversalClass
import universalelectricity.compatibility.Compatibility
import universalelectricity.core.transform.vector.Vector3

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
class TileCoercionDeriver extends TileModuleAcceptor with TEnergyBridge
{
  var processTime: Int = 0
  var isInversed: Boolean = false

  //Client
  var animationTween = 0f

  capacityBase = 30
  startModuleIndex = 3

  override def getSizeInventory = 6

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
          val withdrawnElectricity = (requestFortron(productionRate / 20, true) / TileCoercionDeriver.ueToFortronRatio)
          electricNode.addEnergy(ForgeDirection.UNKNOWN, withdrawnElectricity * TileCoercionDeriver.energyConversionPercentage, true)

          recharge(getStackInSlot(TileCoercionDeriver.slotBattery))
        }
        else
        {
          if (getFortronEnergy < getFortronCapacity)
          {
            discharge(getStackInSlot(TileCoercionDeriver.slotBattery))
            val energy = electricNode.getEnergy(ForgeDirection.UNKNOWN)

            if (energy >= getPower || (!Settings.enableElectricity && isItemValidForSlot(TileCoercionDeriver.slotFuel, getStackInSlot(TileCoercionDeriver.slotFuel))))
            {
              fortronTank.fill(FortronUtility.getFortron(productionRate), true)
              electricNode.removeEnergy(ForgeDirection.UNKNOWN, getPower, true)

              if (processTime == 0 && isItemValidForSlot(TileCoercionDeriver.slotFuel, getStackInSlot(TileCoercionDeriver.slotFuel)))
              {
                decrStackSize(TileCoercionDeriver.slotFuel, 1)
                processTime = TileCoercionDeriver.fuelProcessTime * Math.max(this.getModuleCount(Content.moduleScale) / 20, 1)
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
    else
    {
      /**
       * Handle animation
       */
      if (isActive)
      {
        animation += 1

        if (animationTween < 1)
          animationTween += 0.01f
      }
      else
      {
        if (animationTween > 0)
          animationTween -= 0.01f
      }
    }
  }

  def getPower: Double = TileCoercionDeriver.power + (TileCoercionDeriver.power * (getModuleCount(Content.moduleSpeed) / 8d))

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

  override def write(buf: ByteBuf, id: Int)
  {
    super.write(buf, id)

    if (id == TilePacketType.description.id)
    {
      buf <<< isInversed
      buf <<< processTime
    }
  }

  override def read(buf: ByteBuf, id: Int, player: EntityPlayer, packet: PacketType): Boolean =
  {
    super.read(buf, id, player, packet)

    if (world.isRemote)
    {
      if (id == TilePacketType.description.id)
      {
        isInversed = buf.readBoolean()
        processTime = buf.readInt()
        return true
      }
    }
    else
    {
      if (id == TilePacketType.toggleMoe.id)
      {
        isInversed = !isInversed
        return true
      }
    }

    return false
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

  @SideOnly(Side.CLIENT)
  override def renderStatic(renderer: RenderBlocks, pos: Vector3, pass: Int): Boolean =
  {
    return false
  }

  @SideOnly(Side.CLIENT)
  override def renderDynamic(pos: Vector3, frame: Float, pass: Int)
  {
    RenderCoercionDeriver.render(this, pos.x, pos.y, pos.z, frame, isActive, false)
  }

  @SideOnly(Side.CLIENT)
  override def renderInventory(itemStack: ItemStack)
  {
    RenderCoercionDeriver.render(this, -0.5, -0.5, -0.5, 0, true, true)
  }
}