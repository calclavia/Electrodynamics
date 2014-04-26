package resonantinduction.archaic.waila;

import mcp.mobius.waila.api.IWailaRegistrar;
import resonantinduction.archaic.crate.TileCrate;

public class WailaRegistrar
{
    public static void wailaCallBack(IWailaRegistrar registrar)
    {
        registrar.registerBodyProvider(new WailaCrate(), TileCrate.class);
    }
}
