package resonantinduction.atomic.items

import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import resonant.api.IReactor
import resonant.api.IReactorComponent

/**
 * Breeder rods
 */
class ItemBreederFuel extends ItemRadioactive with IReactorComponent
{
    //Constructor
    this.setMaxDamage(ItemFissileFuel.DECAY)
    this.setMaxStackSize(1)
    this.setNoRepair

    override def onReact(itemStack: ItemStack, reactor: IReactor)
    {
        val tileEntity: TileEntity = reactor.asInstanceOf[TileEntity]
        val worldObj: World = tileEntity.getWorldObj
        reactor.heat(ItemFissileFuel.ENERGY_PER_TICK / 2)
        if (reactor.world.getWorldTime % 20 == 0)
        {
            itemStack.setItemDamage(Math.min(itemStack.getItemDamage + 1, itemStack.getMaxDamage))
        }
    }
}