package edx.quantum.machine.extractor

import net.minecraft.block.material.Material
import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.{FluidContainerRegistry, FluidStack, FluidTank}
import resonant.api.recipe.{MachineRecipes, RecipeResource}
import resonant.lib.prefab.tile.mixed.TileElectricInventory

/**
 * General class for all machines that do traditional recipe processing
 *
 * @author Calclavia
 */
abstract class TileProcess(material: Material) extends TileElectricInventory(material)
{
  protected var inputSlot: Int = 0
  protected var outputSlot: Int = 0
  protected var tankInputFillSlot: Int = 0
  protected var tankInputDrainSlot: Int = 0
  protected var tankOutputFillSlot: Int = 0
  protected var tankOutputDrainSlot: Int = 0
  protected var machineName: String = null

  override def update
  {
    super.update
    if (getInputTank != null)
    {
      fillOrDrainTank(tankInputFillSlot, tankInputDrainSlot, getInputTank)
    }
    if (getOutputTank != null)
    {
      fillOrDrainTank(tankOutputFillSlot, tankOutputDrainSlot, getOutputTank)
    }
  }

  /**
   * Takes an fluid container item and try to fill the tank, dropping the remains in the output slot.
   */
  def fillOrDrainTank(containerInput: Int, containerOutput: Int, tank: FluidTank)
  {
    val inputStack: ItemStack = getStackInSlot(containerInput)
    val outputStack: ItemStack = getStackInSlot(containerOutput)
    if (FluidContainerRegistry.isFilledContainer(inputStack))
    {
      val fluidStack: FluidStack = FluidContainerRegistry.getFluidForFilledItem(inputStack)
      val result: ItemStack = inputStack.getItem.getContainerItem(inputStack)
      if (result != null && tank.fill(fluidStack, false) >= fluidStack.amount && (outputStack == null || result.isItemEqual(outputStack)))
      {
        tank.fill(fluidStack, true)
        decrStackSize(containerInput, 1)
        incrStackSize(containerOutput, result)
      }
    }
    else if (FluidContainerRegistry.isEmptyContainer(inputStack))
    {
      val avaliable: FluidStack = tank.getFluid
      if (avaliable != null)
      {
        val result: ItemStack = FluidContainerRegistry.fillFluidContainer(avaliable, inputStack)
        val filled: FluidStack = FluidContainerRegistry.getFluidForFilledItem(result)
        if (result != null && filled != null && (outputStack == null || result.isItemEqual(outputStack)))
        {
          decrStackSize(containerInput, 1)
          incrStackSize(containerOutput, result)
          tank.drain(filled.amount, true)
        }
      }
    }
  }

  /**
   * Gets the current result of the input set up.
   *
   * @return
   */
  def getResults: Array[RecipeResource] =
  {
    val inputStack: ItemStack = getStackInSlot(inputSlot)
    val mixedResult: Array[RecipeResource] = MachineRecipes.instance.getOutput(machineName, inputStack, getInputTank.getFluid)
    if (mixedResult.length > 0)
    {
      return mixedResult
    }
    return MachineRecipes.instance.getOutput(machineName, inputStack)
  }

  def hasResult: Boolean =
  {
    return getResults.length > 0
  }

  def getInputTank: FluidTank

  def getOutputTank: FluidTank
}