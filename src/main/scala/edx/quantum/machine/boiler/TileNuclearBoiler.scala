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
import resonantengine.api.network.IPacketReceiver
import resonantengine.core.ResonantEngine
import resonantengine.core.network.discriminator.{PacketTile, PacketType}
import resonantengine.lib.content.prefab.{TIO, TInventory}
import resonantengine.lib.grid.core.TBlockNodeProvider
import resonantengine.lib.grid.energy.EnergyStorage
import resonantengine.lib.grid.energy.electric.NodeElectricComponent
import resonantengine.lib.modcontent.block.ResonantTile
import resonantengine.lib.transform.vector.Vector3
import resonantengine.prefab.block.traits.{TEnergyProvider, TRotatable}

/**
 * Nuclear boiler TileEntity
 */
object TileNuclearBoiler
{
  final val power: Long = 50000
}

class TileNuclearBoiler extends ResonantTile(Material.iron) with TInventory with TBlockNodeProvider with IPacketReceiver with IFluidHandler with TRotatable with TEnergyProvider with TIO
{
  final val totalTime: Int = 20 * 15
  final val waterTank: FluidTank = new FluidTank(QuantumContent.fluidStackWater.copy, FluidContainerRegistry.BUCKET_VOLUME * 5)
  final val gasTank: FluidTank = new FluidTank(QuantumContent.fluidStackUraniumHexaflouride.copy, FluidContainerRegistry.BUCKET_VOLUME * 5)
  private val electricNode = new NodeElectricComponent(this)
  var timer: Int = 0

  var rotation: Float = 0

  //Constructor
  //TODO: Dummy
  energy = new EnergyStorage
  normalRender = false
  isOpaqueCube = false

  override def getSizeInventory: Int = 4

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
          if (liquid.isFluidEqual(QuantumContent.fluidStackWater))
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
        //discharge(getStackInSlot(0))
        if (energy >= TileNuclearBoiler.power)
        {
          if (this.timer == 0)
          {
            this.timer = totalTime
          }
          if (this.timer > 0)
          {
            this.timer -= 1
            if (this.timer < 1)
            {
              this.use
              this.timer = 0
            }
          }
          else
          {
            this.timer = 0
          }
          energy -= TileNuclearBoiler.power
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
  def use
  {
    if (this.nengYong)
    {
      this.waterTank.drain(FluidContainerRegistry.BUCKET_VOLUME, true)
      val liquid: FluidStack = QuantumContent.fluidStackUraniumHexaflouride.copy
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
    if (QuantumContent.fluidStackWater.isFluidEqual(resource))
    {
      return this.waterTank.fill(resource, doFill)
    }
    return 0
  }

  def read(data: ByteBuf, player: EntityPlayer, `type`: PacketType)
  {
    this.waterTank.setFluid(new FluidStack(QuantumContent.fluidStackWater.fluidID, data.readInt))
    this.gasTank.setFluid(new FluidStack(QuantumContent.fluidStackUraniumHexaflouride.fluidID, data.readInt))
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
    if (QuantumContent.fluidStackUraniumHexaflouride.isFluidEqual(resource))
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
    return QuantumContent.fluidStackWater.fluidID == fluid.getID
  }

  def canDrain(from: ForgeDirection, fluid: Fluid): Boolean =
  {
    return QuantumContent.fluidStackUraniumHexaflouride.fluidID == fluid.getID
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