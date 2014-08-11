package resonantinduction.archaic.waila;

import resonantinduction.archaic.crate.TileCrate;
import resonantinduction.archaic.fluid.tank.TileTank;
import mcp.mobius.waila.api.IWailaRegistrar;

public class WailaRegistrar
{
    public static void wailaCallBack(IWailaRegistrar registrar)
    {
        registrar.registerBodyProvider(new WailaCrate(), TileCrate.class);
        registrar.registerBodyProvider(new WailaFluidTank(), TileTank.class);
    }
}
