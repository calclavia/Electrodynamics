package edx.core.resource.content

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.block.material.Material
import net.minecraft.world.{IBlockAccess, World}
import net.minecraftforge.fluids.{BlockFluidFinite, Fluid, FluidContainerRegistry, FluidStack}

/**
 * Fluid class uses for molten materials.
 *
 * @author Calclavia
 */
class BlockFluidMaterial(fluid: Fluid) extends BlockFluidFinite(fluid, Material.lava)
{
  def setQuanta(world: World, x: Int, y: Int, z: Int, quanta: Int)
  {
    if (quanta > 0)
    {
      world.setBlockMetadataWithNotify(x, y, z, quanta, 3)
    }
    else
    {
      world.setBlockToAir(x, y, z)
    }
  }

  override def drain(world: World, x: Int, y: Int, z: Int, doDrain: Boolean): FluidStack =
  {
    val stack: FluidStack = new FluidStack(getFluid, (FluidContainerRegistry.BUCKET_VOLUME * this.getFilledPercentage(world, x, y, z)).toInt)
    if (doDrain)
    {
      world.setBlockToAir(x, y, z)
    }
    return stack
  }

  @SideOnly(Side.CLIENT)
  override def colorMultiplier(access: IBlockAccess, x: Int, y: Int, z: Int): Int =
  {
    return getFluid.getColor
  }

  override def canDrain(world: World, x: Int, y: Int, z: Int): Boolean = true
}