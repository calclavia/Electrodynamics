package edx.quantum.machine.boiler

import edx.core.Settings
import edx.quantum.QuantumContent
import io.netty.buffer.ByteBuf
import net.minecraft.block.material.Material
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.Packet
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids._
import resonant.engine.ResonantEngine
import resonant.lib.content.prefab.TEnergyStorage
import resonant.lib.grid.energy.EnergyStorage
import resonant.lib.network.Synced
import resonant.lib.network.discriminator.{PacketTile, PacketType}
import resonant.lib.network.handle.IPacketReceiver
import resonant.lib.prefab.tile.TileElectricInventory
import resonant.lib.prefab.tile.traits.TRotatable
import resonant.lib.transform.vector.Vector3

/**
 * Nuclear boiler TileEntity
 */
object TileNuclearBoiler
{
  final val DIAN: Long = 50000
}

class TileNuclearBoiler extends TileElectricInventory(Material.iron) with IPacketReceiver with IFluidHandler with TRotatable with TEnergyStorage
{
  final val SHI_JIAN: Int = 20 * 15

  @Synced
  final val waterTank: FluidTank = new FluidTank(QuantumContent.FLUIDSTACK_WATER.copy, FluidContainerRegistry.BUCKET_VOLUME * 5)

  @Synced
  final val gasTank: FluidTank = new FluidTank(QuantumContent.FLUIDSTACK_URANIUM_HEXAFLOURIDE.copy, FluidContainerRegistry.BUCKET_VOLUME * 5)

  @Synced
  var timer: Int = 0

  var rotation: Float = 0

  //Constructor
  //TODO: Dummy
  energy = new EnergyStorage(0)
  energy.setCapacity(TileNuclearBoiler.DIAN * 2)
  this.setSizeInventory(4)
  normalRender(false)
  isOpaqueCube(false)

  override def update
  {
    super.update
    if (timer > 0)
    {
      rotation += 0.1f
    }
    if (!this.worldObj.isRemote)
    {
      if (getStackInSlot(1) != null)
      {
        if (FluidContainerRegistry.isFilledContainer(getStackInSlot(1)))
        {
          val liquid: FluidStack = FluidContainerRegistry.getFluidForFilledItem(getStackInSlot(1))
          if (liquid.isFluidEqual(QuantumContent.FLUIDSTACK_WATER))
          {
            if (this.fill(ForgeDirection.UNKNOWN, liquid, false) > 0)
            {
              val resultingContainer: ItemStack = getStackInSlot(1).getItem.getContainerItem(getStackInSlot(1))
              if (resultingContainer == null && getStackInSlot(1).stackSize > 1)
              {
                getStackInSlot(1).stackSize -= 1
              }
              else
              {
                setInventorySlotContents(1, resultingContainer)
              }
              this.waterTank.fill(liquid, true)
            }
          }
        }
      }
      if (this.nengYong)
      {
        this.discharge(getStackInSlot(0))
        if (energy.extractEnergy(TileNuclearBoiler.DIAN, false) >= TileNuclearBoiler.DIAN)
        {
          if (this.timer == 0)
          {
            this.timer = SHI_JIAN
          }
          if (this.timer > 0)
          {
            this.timer -= 1
            if (this.timer < 1)
            {
              this.yong
              this.timer = 0
            }
          }
          else
          {
            this.timer = 0
          }
          energy.extractEnergy(TileNuclearBoiler.DIAN, true)
        }
      }
      else
      {
        this.timer = 0
      }
      if (this.ticks % 10 == 0)
      {
        this.sendDescPack
      }
    }
  }

  def sendDescPack
  {
    if (!this.worldObj.isRemote)
    {
    }
  }

  /**
   * Turn one item from the furnace source stack into the appropriate smelted item in the furnace result stack.
   */
  def yong
  {
    if (this.nengYong)
    {
      this.waterTank.drain(FluidContainerRegistry.BUCKET_VOLUME, true)
      val liquid: FluidStack = QuantumContent.FLUIDSTACK_URANIUM_HEXAFLOURIDE.copy
      liquid.amount = Settings.uraniumHexaflourideRatio * 2
      this.gasTank.fill(liquid, true)
      this.decrStackSize(3, 1)
    }
  }

  def nengYong: Boolean =
  {
    if (this.waterTank.getFluid != null)
    {
      if (this.waterTank.getFluid.amount >= FluidContainerRegistry.BUCKET_VOLUME)
      {
        if (getStackInSlot(3) != null)
        {
          if (QuantumContent.itemYellowCake == getStackInSlot(3).getItem || QuantumContent.isItemStackUraniumOre(getStackInSlot(3)))
          {
            if (QuantumContent.getFluidAmount(this.gasTank.getFluid) < this.gasTank.getCapacity)
            {
              return true
            }
          }
        }
      }
    }
    return false
  }

  /**
   * Tank Methods
   */
  def fill(from: ForgeDirection, resource: FluidStack, doFill: Boolean): Int =
  {
    if (QuantumContent.FLUIDSTACK_WATER.isFluidEqual(resource))
    {
      return this.waterTank.fill(resource, doFill)
    }
    return 0
  }

  def read(data: ByteBuf, player: EntityPlayer, `type`: PacketType)
  {
    this.waterTank.setFluid(new FluidStack(QuantumContent.FLUIDSTACK_WATER.fluidID, data.readInt))
    this.gasTank.setFluid(new FluidStack(QuantumContent.FLUIDSTACK_URANIUM_HEXAFLOURIDE.fluidID, data.readInt))
    this.timer = data.readInt
  }

  override def getDescriptionPacket: Packet =
  {
    return ResonantEngine.packetHandler.toMCPacket(getDescPacket)
  }

  override def getDescPacket: PacketTile =
  {
    return new PacketTile(xi, yi, zi, Array[Any](this.timer, QuantumContent.getFluidAmount(this.waterTank.getFluid), QuantumContent.getFluidAmount(this.gasTank.getFluid)))
  }

  override def use(player: EntityPlayer, side: Int, hit: Vector3): Boolean =
  {
    openGui(player, QuantumContent)
    return true
  }

  /**
   * Reads a tile entity from NBT.
   */
  override def readFromNBT(nbt: NBTTagCompound)
  {
    super.readFromNBT(nbt)
    this.timer = nbt.getInteger("shiJian")
    val waterCompound: NBTTagCompound = nbt.getCompoundTag("water")
    this.waterTank.setFluid(FluidStack.loadFluidStackFromNBT(waterCompound))
    val gasCompound: NBTTagCompound = nbt.getCompoundTag("gas")
    this.gasTank.setFluid(FluidStack.loadFluidStackFromNBT(gasCompound))
  }

  /**
   * Writes a tile entity to NBT.
   */
  override def writeToNBT(nbt: NBTTagCompound)
  {
    super.writeToNBT(nbt)
    nbt.setInteger("shiJian", this.timer)
    if (this.waterTank.getFluid != null)
    {
      val compound: NBTTagCompound = new NBTTagCompound
      this.waterTank.getFluid.writeToNBT(compound)
      nbt.setTag("water", compound)
    }
    if (this.gasTank.getFluid != null)
    {
      val compound: NBTTagCompound = new NBTTagCompound
      this.gasTank.getFluid.writeToNBT(compound)
      nbt.setTag("gas", compound)
    }
  }

  def drain(from: ForgeDirection, resource: FluidStack, doDrain: Boolean): FluidStack =
  {
    if (QuantumContent.FLUIDSTACK_URANIUM_HEXAFLOURIDE.isFluidEqual(resource))
    {
      return this.gasTank.drain(resource.amount, doDrain)
    }
    return null
  }

  def drain(from: ForgeDirection, maxDrain: Int, doDrain: Boolean): FluidStack =
  {
    return this.gasTank.drain(maxDrain, doDrain)
  }

  def canFill(from: ForgeDirection, fluid: Fluid): Boolean =
  {
    return QuantumContent.FLUIDSTACK_WATER.fluidID == fluid.getID
  }

  def canDrain(from: ForgeDirection, fluid: Fluid): Boolean =
  {
    return QuantumContent.FLUIDSTACK_URANIUM_HEXAFLOURIDE.fluidID == fluid.getID
  }

  def getTankInfo(from: ForgeDirection): Array[FluidTankInfo] =
  {
    return Array[FluidTankInfo](this.waterTank.getInfo, this.gasTank.getInfo)
  }

  override def getAccessibleSlotsFromSide(side: Int): Array[Int] =
  {
    return if (side == 0) Array[Int](2) else Array[Int](1, 3)
  }

  override def canInsertItem(slotID: Int, itemStack: ItemStack, side: Int): Boolean =
  {
    return this.isItemValidForSlot(slotID, itemStack)
  }

  /**
   * Inventory
   */
  override def isItemValidForSlot(slotID: Int, itemStack: ItemStack): Boolean =
  {
    if (slotID == 1)
    {
      return QuantumContent.isItemStackWaterCell(itemStack)
    }
    else if (slotID == 3)
    {
      return itemStack.getItem eq QuantumContent.itemYellowCake
    }
    return false
  }

  override def canExtractItem(slotID: Int, itemstack: ItemStack, j: Int): Boolean =
  {
    return slotID == 2
  }
}