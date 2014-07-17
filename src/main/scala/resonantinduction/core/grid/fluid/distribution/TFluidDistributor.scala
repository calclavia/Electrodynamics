package resonantinduction.core.grid.fluid.distribution

import net.minecraft.tileentity.TileEntity
import net.minecraftforge.fluids.{FluidTank, IFluidHandler}
import universalelectricity.api.core.grid.INodeProvider

/**
 * Generic interface for any tile that acts as part of a fluid network. Generally network assume
 * that each part can only support one fluid tank internally
 *
 * @author DarkGuardsman
 */
abstract trait TFluidDistributor extends TileEntity with INodeProvider with IFluidHandler
{
  /**
   * FluidTank that the network will have access to fill or drain
   */
  def getTank: FluidTank

  def onFluidChanged
}