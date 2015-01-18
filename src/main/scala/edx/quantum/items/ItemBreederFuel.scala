package edx.quantum.items

import net.minecraft.item.ItemStack
import resonant.api.tile.{IReactor, IReactorComponent}

/**
 * Breeder rods
 */
class ItemBreederFuel extends ItemRadioactive with IReactorComponent
{
  //Constructor
  this.setMaxDamage(ItemFuelRod.decay)
  this.setMaxStackSize(1)
  this.setNoRepair

  override def onReact(itemStack: ItemStack, reactor: IReactor)
  {
    reactor.heat(ItemFuelRod.energyPerTick / 2)
    if (reactor.world.getWorldTime % 20 == 0)
    {
      itemStack.setItemDamage(Math.min(itemStack.getItemDamage + 1, itemStack.getMaxDamage))
    }
  }
}