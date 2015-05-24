package com.calclavia.edx.quantum.machine.extractor

import com.calclavia.edx.quantum.QuantumContent
import edx.core.Settings
import QuantumContent
import net.minecraft.block.material.Material
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids._
import resonantengine.lib.content.prefab.TIO
import resonantengine.lib.grid.energy.EnergyStorage
import resonantengine.lib.mod.compat.energy.Compatibility
import resonantengine.lib.transform.vector.Vector3
import resonantengine.prefab.block.impl.{TEnergyProvider, TRotatable}

/**
 * Chemical extractor TileEntity
 */
object TileChemicalExtractor
{
  final val TICK_TIME: Int = 20 * 14
  final val EXTRACT_SPEED: Int = 100
  final val ENERGY: Long = 5000
}

class TileChemicalExtractor extends TileProcess(Material.iron) with IFluidHandler with TEnergyProvider with TRotatable with TIO
{
  //TODO: Dummy
  energy = new EnergyStorage
  energy.max = TileChemicalExtractor.ENERGY * 2
  isOpaqueCube = false
  normalRender = false
  inputSlot = 1
  outputSlot = 2
  tankInputFillSlot = 3
  tankInputDrainSlot = 4
  tankOutputFillSlot = 5
  tankOutputDrainSlot = 6

  final val inputTank: FluidTank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME * 10)
  final val outputTank: FluidTank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME * 10)
  var time: Int = 0
  var rotation: Float = 0

  override def getSizeInventory: Int = 7

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
        //discharge(getStackInSlot(0))
        if (energy >= TileChemicalExtractor.ENERGY)
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
        energy -= TileChemicalExtractor.ENERGY
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

  override def use(player: EntityPlayer, side: Int, hit: Vector3): Boolean =
  {
    openGui(player, QuantumContent)
    return true
  }

  def canUse: Boolean =
  {
    if (inputTank.getFluid != null)
    {
      if (inputTank.getFluid.amount >= FluidContainerRegistry.BUCKET_VOLUME && QuantumContent.isItemStackUraniumOre(getStackInSlot(inputSlot)))
      {
        if (isItemValidForSlot(outputSlot, new ItemStack(QuantumContent.itemYellowCake)))
        {
          return true
        }
      }
      if (outputTank.getFluidAmount < outputTank.getCapacity)
      {
        if (inputTank.getFluid.getFluid.getID == QuantumContent.FLUID_DEUTERIUM.getID && inputTank.getFluid.amount >= Settings.deutermiumPerTritium * TileChemicalExtractor.EXTRACT_SPEED)
        {
          if (outputTank.getFluid == null || (QuantumContent.getFluidStackTritium == outputTank.getFluid))
          {
            return true
          }
        }
        if (inputTank.getFluid.getFluid.getID == FluidRegistry.WATER.getID && inputTank.getFluid.amount >= Settings.waterPerDeutermium * TileChemicalExtractor.EXTRACT_SPEED)
        {
          if (outputTank.getFluid == null || (QuantumContent.FLUIDSTACK_DEUTERIUM == outputTank.getFluid))
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
      if (QuantumContent.isItemStackUraniumOre(getStackInSlot(inputSlot)))
      {
        inputTank.drain(FluidContainerRegistry.BUCKET_VOLUME, true)
        incrStackSize(outputSlot, new ItemStack(QuantumContent.itemYellowCake, 3))
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
        if (outputTank.fill(new FluidStack(QuantumContent.FLUIDSTACK_DEUTERIUM, TileChemicalExtractor.EXTRACT_SPEED), true) >= TileChemicalExtractor.EXTRACT_SPEED)
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
      if (drain != null && drain.amount >= 1 && drain.getFluid.getID == QuantumContent.FLUID_DEUTERIUM.getID)
      {
        if (outputTank.fill(new FluidStack(QuantumContent.getFluidStackTritium, TileChemicalExtractor.EXTRACT_SPEED), true) >= TileChemicalExtractor.EXTRACT_SPEED)
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
    return FluidRegistry.WATER.getID == fluid.getID || QuantumContent.FLUID_DEUTERIUM.getID == fluid.getID
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
      return QuantumContent.isItemStackWaterCell(itemStack)
    }
    if (slotID == 2)
    {
      return QuantumContent.isItemStackDeuteriumCell(itemStack) || QuantumContent.isItemStackTritiumCell(itemStack)
    }
    if (slotID == 3)
    {
      return QuantumContent.isItemStackEmptyCell(itemStack) || QuantumContent.isItemStackUraniumOre(itemStack) || QuantumContent.isItemStackDeuteriumCell(itemStack)
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