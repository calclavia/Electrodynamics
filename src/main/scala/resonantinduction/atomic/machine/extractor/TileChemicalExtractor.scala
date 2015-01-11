package resonantinduction.atomic.machine.extractor

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
import resonant.lib.mod.compat.energy.Compatibility
import resonant.lib.network.Synced
import resonant.lib.network.discriminator.PacketAnnotation
import resonant.lib.prefab.tile.traits.TRotatable
import resonant.lib.transform.vector.Vector3
import resonantinduction.atomic.AtomicContent
import resonantinduction.core.Settings

/**
 * Chemical extractor TileEntity
 */
object TileChemicalExtractor
{
  final val TICK_TIME: Int = 20 * 14
  final val EXTRACT_SPEED: Int = 100
  final val ENERGY: Long = 5000
}

class TileChemicalExtractor extends TileProcess(Material.iron) with IFluidHandler with TEnergyStorage with TRotatable
{
  //TODO: Dummy
  energy = new EnergyStorage(0)
  energy.setCapacity(TileChemicalExtractor.ENERGY * 2)
  this.setSizeInventory(7)
  this.isOpaqueCube(false)
  this.normalRender(false)
  inputSlot = 1
  outputSlot = 2
  tankInputFillSlot = 3
  tankInputDrainSlot = 4
  tankOutputFillSlot = 5
  tankOutputDrainSlot = 6

  @Synced final val inputTank: FluidTank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME * 10)
  @Synced final val outputTank: FluidTank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME * 10)
  @Synced var time: Int = 0
  var rotation: Float = 0

  override def update
  {
    super.update
    if (time > 0)
    {
      rotation += 0.2f
    }
    if (!worldObj.isRemote)
    {
      if (canUse)
      {
        discharge(getStackInSlot(0))
        if (energy.checkExtract(TileChemicalExtractor.ENERGY))
        {
          if (time == 0)
          {
            time = TileChemicalExtractor.TICK_TIME
          }
          if (time > 0)
          {
            time -= 1
            if (time < 1)
            {
              if (!refineUranium)
              {
                if (!extractTritium)
                {
                  extractDeuterium
                }
              }
              time = 0
            }
          }
          else
          {
            time = 0
          }
        }
        energy.extractEnergy(TileChemicalExtractor.ENERGY, true)
      }
      else
      {
        time = 0
      }
      if (ticks % 10 == 0)
      {
      }
    }
  }

  override def getDescriptionPacket: Packet =
  {
    return ResonantEngine.packetHandler.toMCPacket(new PacketAnnotation(this))
  }

  override def use(player: EntityPlayer, side: Int, hit: Vector3): Boolean =
  {
    openGui(player, AtomicContent)
    return true
  }

  def canUse: Boolean =
  {
    if (inputTank.getFluid != null)
    {
      if (inputTank.getFluid.amount >= FluidContainerRegistry.BUCKET_VOLUME && AtomicContent.isItemStackUraniumOre(getStackInSlot(inputSlot)))
      {
        if (isItemValidForSlot(outputSlot, new ItemStack(AtomicContent.itemYellowCake)))
        {
          return true
        }
      }
      if (outputTank.getFluidAmount < outputTank.getCapacity)
      {
        if (inputTank.getFluid.getFluid.getID == AtomicContent.FLUID_DEUTERIUM.getID && inputTank.getFluid.amount >= Settings.deutermiumPerTritium * TileChemicalExtractor.EXTRACT_SPEED)
        {
          if (outputTank.getFluid == null || (AtomicContent.getFluidStackTritium == outputTank.getFluid))
          {
            return true
          }
        }
        if (inputTank.getFluid.getFluid.getID == FluidRegistry.WATER.getID && inputTank.getFluid.amount >= Settings.waterPerDeutermium * TileChemicalExtractor.EXTRACT_SPEED)
        {
          if (outputTank.getFluid == null || (AtomicContent.FLUIDSTACK_DEUTERIUM == outputTank.getFluid))
          {
            return true
          }
        }
      }
    }
    return false
  }

  /**
   * Turn one item from the furnace source stack into the appropriate smelted item in the furnace result stack.
   */
  def refineUranium: Boolean =
  {
    if (canUse)
    {
      if (AtomicContent.isItemStackUraniumOre(getStackInSlot(inputSlot)))
      {
        inputTank.drain(FluidContainerRegistry.BUCKET_VOLUME, true)
        incrStackSize(outputSlot, new ItemStack(AtomicContent.itemYellowCake, 3))
        decrStackSize(inputSlot, 1)
        return true
      }
    }
    return false
  }

  def extractDeuterium: Boolean =
  {
    if (canUse)
    {
      val drain: FluidStack = inputTank.drain(Settings.waterPerDeutermium * TileChemicalExtractor.EXTRACT_SPEED, false)
      if (drain != null && drain.amount >= 1 && drain.getFluid.getID == FluidRegistry.WATER.getID)
      {
        if (outputTank.fill(new FluidStack(AtomicContent.FLUIDSTACK_DEUTERIUM, TileChemicalExtractor.EXTRACT_SPEED), true) >= TileChemicalExtractor.EXTRACT_SPEED)
        {
          inputTank.drain(Settings.waterPerDeutermium * TileChemicalExtractor.EXTRACT_SPEED, true)
          return true
        }
      }
    }
    return false
  }

  def extractTritium: Boolean =
  {
    if (canUse)
    {
      val waterUsage: Int = Settings.deutermiumPerTritium
      val drain: FluidStack = inputTank.drain(Settings.deutermiumPerTritium * TileChemicalExtractor.EXTRACT_SPEED, false)
      if (drain != null && drain.amount >= 1 && drain.getFluid.getID == AtomicContent.FLUID_DEUTERIUM.getID)
      {
        if (outputTank.fill(new FluidStack(AtomicContent.getFluidStackTritium, TileChemicalExtractor.EXTRACT_SPEED), true) >= TileChemicalExtractor.EXTRACT_SPEED)
        {
          inputTank.drain(Settings.deutermiumPerTritium * TileChemicalExtractor.EXTRACT_SPEED, true)
          return true
        }
      }
    }
    return false
  }

  /**
   * Reads a tile entity from NBT.
   */
  override def readFromNBT(nbt: NBTTagCompound)
  {
    super.readFromNBT(nbt)
    time = nbt.getInteger("time")
    val water: NBTTagCompound = nbt.getCompoundTag("inputTank")
    inputTank.setFluid(FluidStack.loadFluidStackFromNBT(water))
    val deuterium: NBTTagCompound = nbt.getCompoundTag("outputTank")
    outputTank.setFluid(FluidStack.loadFluidStackFromNBT(deuterium))
  }

  /**
   * Writes a tile entity to NBT.
   */
  override def writeToNBT(nbt: NBTTagCompound)
  {
    super.writeToNBT(nbt)
    nbt.setInteger("time", time)
    if (inputTank.getFluid != null)
    {
      val compound: NBTTagCompound = new NBTTagCompound
      inputTank.getFluid.writeToNBT(compound)
      nbt.setTag("inputTank", compound)
    }
    if (outputTank.getFluid != null)
    {
      val compound: NBTTagCompound = new NBTTagCompound
      outputTank.getFluid.writeToNBT(compound)
      nbt.setTag("outputTank", compound)
    }
  }

  /**
   * Tank Methods
   */
  def fill(from: ForgeDirection, resource: FluidStack, doFill: Boolean): Int =
  {
    if (resource != null && canFill(from, resource.getFluid))
    {
      return inputTank.fill(resource, doFill)
    }
    return 0
  }

  def canFill(from: ForgeDirection, fluid: Fluid): Boolean =
  {
    return FluidRegistry.WATER.getID == fluid.getID || AtomicContent.FLUID_DEUTERIUM.getID == fluid.getID
  }

  def drain(from: ForgeDirection, resource: FluidStack, doDrain: Boolean): FluidStack =
  {
    return drain(from, resource.amount, doDrain)
  }

  def drain(from: ForgeDirection, maxDrain: Int, doDrain: Boolean): FluidStack =
  {
    return outputTank.drain(maxDrain, doDrain)
  }

  def canDrain(from: ForgeDirection, fluid: Fluid): Boolean =
  {
    return outputTank.getFluid != null && outputTank.getFluid.getFluid.getID == fluid.getID
  }

  def getTankInfo(from: ForgeDirection): Array[FluidTankInfo] =
  {
    return Array[FluidTankInfo](this.inputTank.getInfo, this.outputTank.getInfo)
  }

  override def getAccessibleSlotsFromSide(side: Int): Array[Int] =
  {
    return Array[Int](1, 2, 3)
  }

  override def canInsertItem(slotID: Int, itemStack: ItemStack, side: Int): Boolean =
  {
    return this.isItemValidForSlot(slotID, itemStack)
  }

  override def isItemValidForSlot(slotID: Int, itemStack: ItemStack): Boolean =
  {
    if (slotID == 0)
    {
      return Compatibility.isHandler(itemStack.getItem, null)
    }
    if (slotID == 1)
    {
      return AtomicContent.isItemStackWaterCell(itemStack)
    }
    if (slotID == 2)
    {
      return AtomicContent.isItemStackDeuteriumCell(itemStack) || AtomicContent.isItemStackTritiumCell(itemStack)
    }
    if (slotID == 3)
    {
      return AtomicContent.isItemStackEmptyCell(itemStack) || AtomicContent.isItemStackUraniumOre(itemStack) || AtomicContent.isItemStackDeuteriumCell(itemStack)
    }
    return false
  }

  override def canExtractItem(slotID: Int, itemstack: ItemStack, side: Int): Boolean =
  {
    return slotID == 2
  }

  def getInputTank: FluidTank =
  {
    return inputTank
  }

  def getOutputTank: FluidTank =
  {
    return outputTank
  }
}