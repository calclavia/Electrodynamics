package resonantinduction.archaic.waila

import mcp.mobius.waila.api.IWailaConfigHandler
import mcp.mobius.waila.api.IWailaDataAccessor
import mcp.mobius.waila.api.IWailaDataProvider
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.fluids.IFluidTank
import resonant.lib.utility.LanguageUtility
import resonantinduction.archaic.fluid.tank.TileTank
import java.util.List

/**
 * Waila support for tanks
 *
 * @author Darkguardsman
 */
class WailaFluidTank extends IWailaDataProvider
{
    override def getWailaBody(itemStack: ItemStack, currenttip: List[String], accessor: IWailaDataAccessor, config: IWailaConfigHandler): List[String] =
    {
        val tile: TileEntity = accessor.getTileEntity
        if (tile.isInstanceOf[TileTank])
        {
            val tank: IFluidTank = (tile.asInstanceOf[TileTank]).getTank
            if (tank != null && tank.getFluid != null)
            {
                currenttip.add(LanguageUtility.getLocal("info.waila.tank.fluid") + " " + tank.getFluid.getFluid.getLocalizedName)
                currenttip.add(LanguageUtility.getLocal("info.waila.tank.vol") + " " + tank.getFluidAmount + " / " + tank.getCapacity)
            }
        }
        return currenttip
    }

    override def getWailaHead(itemStack: ItemStack, currenttip: List[String], accessor: IWailaDataAccessor, config: IWailaConfigHandler): List[String] =
    {
        return currenttip
    }

    override def getWailaStack(accessor: IWailaDataAccessor, config: IWailaConfigHandler): ItemStack =
    {
        return null
    }

    override def getWailaTail(itemStack: ItemStack, currenttip: List[String], accessor: IWailaDataAccessor, config: IWailaConfigHandler): List[String] =
    {
        return currenttip
    }
}