package com.calclavia.edx.basic.waila

import java.util.List
import com.calclavia.edx.basic.fluid.tank.TileTank
import mcp.mobius.waila.api.{IWailaConfigHandler, IWailaDataAccessor, IWailaDataProvider}
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.fluids.IFluidTank
import resonantengine.lib.utility.LanguageUtility

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
      val tank: IFluidTank = tile.asInstanceOf[TileTank].fluidNode
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