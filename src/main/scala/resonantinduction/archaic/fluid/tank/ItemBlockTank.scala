package resonantinduction.archaic.fluid.tank

import java.util.List

import net.minecraft.block.Block
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{ItemBlock, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import net.minecraftforge.fluids.{FluidStack, IFluidContainerItem}
import resonant.lib.utility.LanguageUtility
import resonant.lib.wrapper.WrapList._
import universalelectricity.api.UnitDisplay
import universalelectricity.api.UnitDisplay.Unit

/**
 * @author Darkguardsman
 */
class ItemBlockTank(block: Block) extends ItemBlock(block: Block) with IFluidContainerItem
{

  this.setMaxDamage(0)
  this.setHasSubtypes(true)

  override def getMetadata(damage: Int): Int =
  {
    return damage
  }

  @SuppressWarnings(Array("unchecked", "rawtypes")) override def addInformation(itemStack: ItemStack, player: EntityPlayer, list: List[_], par4: Boolean)
  {
    if (itemStack.getTagCompound != null && itemStack.getTagCompound.hasKey("fluid"))
    {
      val fluid: FluidStack = getFluid(itemStack)
      if (fluid != null)
      {
        list.add("Fluid: " + fluid.getFluid.getLocalizedName)
        list.add("Volume: " + new UnitDisplay(Unit.LITER, fluid.amount))
      }
    }
  }

  def getFluid(container: ItemStack): FluidStack =
  {
    if (container.stackTagCompound == null || !container.stackTagCompound.hasKey("fluid"))
    {
      return null
    }
    return FluidStack.loadFluidStackFromNBT(container.stackTagCompound.getCompoundTag("fluid"))
  }

  override def getItemStackLimit(stack: ItemStack): Int =
  {
    if (stack.getTagCompound != null && stack.getTagCompound.hasKey("fluid"))
    {
      return 1
    }
    return this.maxStackSize
  }

  override def getUnlocalizedName(itemStack: ItemStack): String =
  {
    val translation: String = LanguageUtility.getLocal(getUnlocalizedName() + "." + itemStack.getItemDamage)
    if (translation == null || translation.isEmpty)
    {
      return getUnlocalizedName()
    }
    return getUnlocalizedName() + "." + itemStack.getItemDamage
  }

  override def placeBlockAt(stack: ItemStack, player: EntityPlayer, world: World, x: Int, y: Int, z: Int, side: Int, hitX: Float, hitY: Float, hitZ: Float, metadata: Int): Boolean =
  {
    if (super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, metadata))
    {
      val tile: TileEntity = world.getTileEntity(x, y, z)
      if (tile.isInstanceOf[TileTank])
      {
        (tile.asInstanceOf[TileTank]).getTank.fill(getFluid(stack), true)
      }
      return true
    }
    return false
  }

  def fill(container: ItemStack, resource: FluidStack, doFill: Boolean): Int =
  {
    if (resource == null)
    {
      return 0
    }
    if (!doFill)
    {
      if (container.stackTagCompound == null || !container.stackTagCompound.hasKey("fluid"))
      {
        return Math.min(getCapacity(container), resource.amount)
      }
      val stack: FluidStack = FluidStack.loadFluidStackFromNBT(container.stackTagCompound.getCompoundTag("fluid"))
      if (stack == null)
      {
        return Math.min(getCapacity(container), resource.amount)
      }
      if (!stack.isFluidEqual(resource))
      {
        return 0
      }
      return Math.min(getCapacity(container) - stack.amount, resource.amount)
    }
    if (container.stackTagCompound == null)
    {
      container.stackTagCompound = new NBTTagCompound
    }
    if (!container.stackTagCompound.hasKey("fluid"))
    {
      val fluidTag: NBTTagCompound = resource.writeToNBT(new NBTTagCompound)
      if (getCapacity(container) < resource.amount)
      {
        fluidTag.setInteger("Amount", getCapacity(container))
        container.stackTagCompound.setTag("fluid", fluidTag)
        return getCapacity(container)
      }
      container.stackTagCompound.setTag("fluid", fluidTag)
      return resource.amount
    }
    val fluidTag: NBTTagCompound = container.stackTagCompound.getCompoundTag("fluid")
    val stack: FluidStack = FluidStack.loadFluidStackFromNBT(fluidTag)
    if (!stack.isFluidEqual(resource))
    {
      return 0
    }
    var filled: Int = getCapacity(container) - stack.amount
    if (resource.amount < filled)
    {
      stack.amount += resource.amount
      filled = resource.amount
    }
    else
    {
      stack.amount = getCapacity(container)
    }
    container.stackTagCompound.setTag("fluid", stack.writeToNBT(fluidTag))
    return filled
  }

  def getCapacity(container: ItemStack): Int =
  {
    return TileTank.VOLUME
  }

  def drain(container: ItemStack, maxDrain: Int, doDrain: Boolean): FluidStack =
  {
    if (container.stackTagCompound == null || !container.stackTagCompound.hasKey("fluid") || maxDrain == 0)
    {
      return null
    }
    val stack: FluidStack = FluidStack.loadFluidStackFromNBT(container.stackTagCompound.getCompoundTag("fluid"))
    if (stack == null)
    {
      return null
    }
    val drained: Int = Math.min(stack.amount, maxDrain)
    if (doDrain)
    {
      if (maxDrain >= stack.amount)
      {
        container.stackTagCompound.removeTag("fluid")
        if (container.stackTagCompound.hasNoTags)
        {
          container.stackTagCompound = null
        }
        return stack
      }
      val fluidTag: NBTTagCompound = container.stackTagCompound.getCompoundTag("fluid")
      fluidTag.setInteger("Amount", fluidTag.getInteger("Amount") - maxDrain)
      container.stackTagCompound.setTag("fluid", fluidTag)
    }
    stack.amount = drained
    return stack
  }
}