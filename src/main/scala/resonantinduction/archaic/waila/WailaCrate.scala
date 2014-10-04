package resonantinduction.archaic.waila

import mcp.mobius.waila.api.IWailaConfigHandler
import mcp.mobius.waila.api.IWailaDataAccessor
import mcp.mobius.waila.api.IWailaDataProvider
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import resonant.lib.utility.LanguageUtility
import resonantinduction.archaic.crate.TileCrate
import java.util.List

/**
 * Waila support for crates
 *
 * @author Darkguardsman
 */
class WailaCrate extends IWailaDataProvider
{
    override def getWailaBody(itemStack: ItemStack, currenttip: List[String], accessor: IWailaDataAccessor, config: IWailaConfigHandler): List[String] =
    {
        val tile: TileEntity = accessor.getTileEntity
        if (tile.isInstanceOf[TileCrate])
        {
            val stored: ItemStack = (tile.asInstanceOf[TileCrate]).getSampleStack
            val cap: Int = (tile.asInstanceOf[TileCrate]).getSlotCount * 64
            if (stored != null)
            {
                currenttip.add(LanguageUtility.getLocal("info.waila.crate.stack") + " " + stored.getDisplayName)
                currenttip.add(LanguageUtility.getLocal("info.waila.crate.stored") + " " + stored.stackSize + " / " + cap)
            }
            else
            {
                currenttip.add(LanguageUtility.getLocal("info.waila.crate.empty"))
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