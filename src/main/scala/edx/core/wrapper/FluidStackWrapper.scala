package edx.core.wrapper

import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.fluids.FluidStack

/**
 * @author Calclavia
 */
object FluidStackWrapper
{

  implicit class FluidStackWrapper(fluidStack: FluidStack)
  {
    def getTemperature: Int =
    {
      if (fluidStack.tag == null)
        fluidStack.tag = new NBTTagCompound

      fluidStack.tag.getInteger("temperature")
    }

    def setTemperature(temp: Int)
    {
      if (fluidStack.tag == null)
        fluidStack.tag = new NBTTagCompound

      fluidStack.tag.setInteger("temperature", temp)
    }
  }

}
