package mffs

import net.minecraftforge.fluids.{Fluid, FluidStack, FluidTank}

/**
 * A class with useful functions related to Fortron.
 *
 * @author Calclavia
 */
object FortronHelper
{
  var FLUID_FORTRON: Fluid = null
  var FLUIDSTACK_FORTRON: FluidStack = null

  def getFortron(amount: Int): FluidStack =
  {
    val stack: FluidStack = new FluidStack(FLUID_FORTRON, amount)
    return stack
  }

  def getAmount(liquidStack: FluidStack): Int =
  {
    if (liquidStack != null)
    {
      return liquidStack.amount
    }
    return 0
  }

  def getAmount(fortronTank: FluidTank): Int =
  {
    if (fortronTank != null)
    {
      return getAmount(fortronTank.getFluid)
    }
    return 0
  }
}