package resonantinduction.atomic.items

import java.util.List

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.FluidStack
import resonant.api.{IReactor, IReactorComponent}
import resonant.lib.wrapper.WrapList._
import resonantinduction.atomic.AtomicContent
import resonantinduction.atomic.machine.reactor.TileReactorCell
import resonantinduction.core.{Reference, ResonantTab, Settings}
import universalelectricity.core.transform.vector.Vector3

/**
 * Fissile fuel rod
 */
object ItemFissileFuel
{
    final val DECAY: Int = 2500
    /**
     * Temperature at which the fuel rod will begin to re-enrich itself.
     */
    final val BREEDING_TEMP: Int = 1100
    /**
     * The energy in one KG of uranium is: 72PJ, 100TJ in one cell of uranium.
     */
    final val ENERGY: Long = 100000000000L
    /**
     * Approximately 20,000,000J per tick. 400 MW.
     */
    final val ENERGY_PER_TICK: Long = ENERGY / 50000
}

class ItemFissileFuel extends ItemRadioactive with IReactorComponent
{

    //Constructor
    this.setMaxStackSize(1)
    this.setMaxDamage(ItemFissileFuel.DECAY)
    this.setNoRepair
    this.setUnlocalizedName(Reference.prefix + "rodBreederFuel")
    this.setTextureName(Reference.prefix + "rodBreederFuel")
    setCreativeTab(ResonantTab.tab)

    def onReact(itemStack: ItemStack, reactor: IReactor)
    {
        val tileEntity: TileEntity = reactor.asInstanceOf[TileEntity]
        val worldObj: World = tileEntity.getWorldObj
        var reactors: Int = 0

        for (i <- 0 to 6)
        {
            val checkPos: Vector3 = new Vector3(tileEntity).add(ForgeDirection.getOrientation(i))
            val tile: TileEntity = checkPos.getTileEntity(worldObj)
            if (tile.isInstanceOf[TileReactorCell] && (tile.asInstanceOf[TileReactorCell]).getTemperature > ItemFissileFuel.BREEDING_TEMP)
            {
                reactors += 1
            }
        }

        if (reactors >= 2)
        {
            if (worldObj.rand.nextInt(1000) <= 100 && reactor.getTemperature > (ItemFissileFuel.BREEDING_TEMP / 2))
            {
                val healAmt: Int = worldObj.rand.nextInt(5)
                itemStack.setItemDamage(Math.max(itemStack.getItemDamage - healAmt, 0))
            }
        }
        else
        {
            reactor.heat(ItemFissileFuel.ENERGY_PER_TICK)
            if (reactor.world.getWorldTime % 20 == 0)
            {
                itemStack.setItemDamage(Math.min(itemStack.getItemDamage + 1, itemStack.getMaxDamage))
            }
            if (Settings.allowToxicWaste && worldObj.rand.nextFloat > 0.5)
            {
                val fluid: FluidStack = AtomicContent.FLUIDSTACK_TOXIC_WASTE.copy
                fluid.amount = 1
                reactor.fill(ForgeDirection.UNKNOWN, fluid, true)
            }
        }
    }

    @SideOnly(Side.CLIENT) override def getSubItems(item: Item, par2CreativeTabs: CreativeTabs, par3List: List[_])
    {
        par3List.add(new ItemStack(item, 1, 0))
        par3List.add(new ItemStack(item, 1, getMaxDamage - 1))
    }
}