package edx.quantum.items

import java.util.List

import cpw.mods.fml.relauncher.{Side, SideOnly}
import edx.core.{EDXCreativeTab, Reference}
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.common.util.ForgeDirection
import resonant.api.tile.{IReactor, IReactorComponent}
import resonant.lib.grid.thermal.GridThermal
import resonant.lib.transform.vector.VectorWorld
import resonant.lib.wrapper.CollectionWrapper._

/**
 * Fissile fuel rod
 */
object ItemFuelRod
{
  final val decay = 2500
  /**
   * Temperature at which the fuel rod will begin to re-enrich itself.
   */
  final val breedingTemp: Int = 1100

  /**
   * The energy in one KG of uranium is: 72PJ, 100TJ in one cell of uranium.
   */
  final val energyDensity = 100000000000d
  /**
   * Approximately 20,000,000J per tick. 400 MW.
   */
  final val energyPerTick = (energyDensity / 100000) / decay
}

class ItemFuelRod extends ItemRadioactive with IReactorComponent
{
  //Constructor
  setMaxStackSize(1)
  setMaxDamage(ItemFuelRod.decay)
  setNoRepair()
  setUnlocalizedName(Reference.prefix + "rodBreederFuel")
  setTextureName(Reference.prefix + "breederFuel")
  setCreativeTab(EDXCreativeTab)

  def onReact(itemStack: ItemStack, reactor: IReactor)
  {
    val tile = reactor.asInstanceOf[TileEntity]
    val world = tile.getWorldObj
    val reactors =
      ForgeDirection.VALID_DIRECTIONS
        .map(d => (new VectorWorld(tile) + d).getTileEntity)
        .filter(_.isInstanceOf[IReactor])
        .count(t => GridThermal.getTemperature(new VectorWorld(t)) > ItemFuelRod.breedingTemp)

    if (reactors >= 2)
    {
      /**
       * Do fuel breeding
       */
      if (world.rand.nextInt(1000) <= 100 && GridThermal.getTemperature(new VectorWorld(tile)) > (ItemFuelRod.breedingTemp / 2))
      {
        val breedAmount = world.rand.nextInt(5)
        itemStack.setItemDamage(Math.max(itemStack.getItemDamage - breedAmount, 0))
      }
    }
    else
    {
      /**
       * Do fission
       */
      reactor.heat(ItemFuelRod.energyPerTick)

      if (reactor.world.getWorldTime % 20 == 0)
      {
        itemStack.setItemDamage(Math.min(itemStack.getItemDamage + 1, itemStack.getMaxDamage))
      }
    }
  }

  @SideOnly(Side.CLIENT)
  override def getSubItems(item: Item, par2CreativeTabs: CreativeTabs, par3List: List[_])
  {
    par3List.add(new ItemStack(item, 1, 0))
    par3List.add(new ItemStack(item, 1, getMaxDamage - 1))
  }
}