package edx.basic.waila

import edx.basic.crate.TileCrate
import edx.basic.fluid.tank.TileTank
import mcp.mobius.waila.api.IWailaRegistrar

object WailaRegistrar
{
  def wailaCallBack(registrar: IWailaRegistrar)
  {
    registrar.registerBodyProvider(new WailaCrate, classOf[TileCrate])
    registrar.registerBodyProvider(new WailaFluidTank, classOf[TileTank])
  }
}