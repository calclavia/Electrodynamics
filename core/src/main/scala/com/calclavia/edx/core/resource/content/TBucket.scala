package com.calclavia.edx.core.resource.content

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.fluids.{FluidContainerRegistry, FluidStack, IFluidContainerItem}

/**
 * A trait implemented by buckets
 * @author Calclavia
 */
trait TBucket extends IFluidContainerItem
{
  protected var capacity = FluidContainerRegistry.BUCKET_VOLUME

  def getFluid(container: ItemStack): FluidStack =
  {
    if (container.stackTagCompound == null || !container.stackTagCompound.hasKey("Fluid"))
    {
      return null
    }
    return FluidStack.loadFluidStackFromNBT(container.stackTagCompound.getCompoundTag("Fluid"))
  }

  def getCapacity(container: ItemStack): Int =
  {
    return capacity
  }

  override def fill(container: ItemStack, resource: FluidStack, doFill: Boolean): Int =
  {
    if (resource == null)
    {
      return 0
    }
    if (!doFill)
    {
      if (container.stackTagCompound == null || !container.stackTagCompound.hasKey("Fluid"))
      {
        return Math.min(capacity, resource.amount)
      }
      val stack: FluidStack = FluidStack.loadFluidStackFromNBT(container.stackTagCompound.getCompoundTag("Fluid"))
      if (stack == null)
      {
        return Math.min(capacity, resource.amount)
      }
      if (!stack.isFluidEqual(resource))
      {
        return 0
      }
      return Math.min(capacity - stack.amount, resource.amount)
    }
    if (container.stackTagCompound == null)
    {
      container.stackTagCompound = new NBTTagCompound
    }
    if (!container.stackTagCompound.hasKey("Fluid"))
    {
      val fluidTag: NBTTagCompound = resource.writeToNBT(new NBTTagCompound)
      if (capacity < resource.amount)
      {
        fluidTag.setInteger("Amount", capacity)
        container.stackTagCompound.setTag("Fluid", fluidTag)
        return capacity
      }
      container.stackTagCompound.setTag("Fluid", fluidTag)
      return resource.amount
    }
    val fluidTag: NBTTagCompound = container.stackTagCompound.getCompoundTag("Fluid")
    val stack: FluidStack = FluidStack.loadFluidStackFromNBT(fluidTag)
    if (!stack.isFluidEqual(resource))
    {
      return 0
    }
    var filled: Int = capacity - stack.amount
    if (resource.amount < filled)
    {
      stack.amount += resource.amount
      filled = resource.amount
    }
    else
    {
      stack.amount = capacity
    }
    container.stackTagCompound.setTag("Fluid", stack.writeToNBT(fluidTag))
    return filled
  }

  override def drain(container: ItemStack, maxDrain: Int, doDrain: Boolean): FluidStack =
  {
    if (container.stackTagCompound == null || !container.stackTagCompound.hasKey("Fluid"))
    {
      return null
    }
    val stack: FluidStack = FluidStack.loadFluidStackFromNBT(container.stackTagCompound.getCompoundTag("Fluid"))
    if (stack == null)
    {
      return null
    }
    val currentAmount: Int = stack.amount
    stack.amount = Math.min(stack.amount, maxDrain)
    if (doDrain)
    {
      if (currentAmount == stack.amount)
      {
        container.stackTagCompound.removeTag("Fluid")
        if (container.stackTagCompound.hasNoTags)
        {
          container.stackTagCompound = null
        }
        return stack
      }
      val fluidTag: NBTTagCompound = container.stackTagCompound.getCompoundTag("Fluid")
      fluidTag.setInteger("Amount", currentAmount - stack.amount)
      container.stackTagCompound.setTag("Fluid", fluidTag)
    }
    return stack
  }
}
