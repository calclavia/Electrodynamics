package resonantinduction.archaic.waila

import mcp.mobius.waila.api.IWailaRegistrar
import resonantinduction.archaic.crate.TileCrate
import resonantinduction.archaic.fluid.tank.TileTank

object WailaRegistrar
{
    def wailaCallBack(registrar: IWailaRegistrar)
    {
        registrar.registerBodyProvider(new WailaCrate, classOf[TileCrate])
        registrar.registerBodyProvider(new WailaFluidTank, classOf[TileTank])
    }
}