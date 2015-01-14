package edx.basic.waila

import java.util.List

import edx.basic.crate.TileCrate
import mcp.mobius.waila.api.{IWailaConfigHandler, IWailaDataAccessor, IWailaDataProvider}
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import resonant.lib.utility.LanguageUtility

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