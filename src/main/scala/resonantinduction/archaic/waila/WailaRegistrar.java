package resonantinduction.archaic.waila;

import archaic.crate.TileCrate;
import resonantinduction.archaic.fluid.tank.TileTank;
import mcp.mobius.waila.api.IWailaRegistrar;
import archaic.crate.TileCrate;
import resonantinduction.archaic.fluid.tank.TileTank;
import resonantinduction.archaic.fluid.tank.TileTank;

public class WailaRegistrar
{
    public static void wailaCallBack(IWailaRegistrar registrar)
    {
        registrar.registerBodyProvider(new WailaCrate(), TileCrate.class);
        registrar.registerBodyProvider(new WailaFluidTank(), TileTank.class);
    }
}
