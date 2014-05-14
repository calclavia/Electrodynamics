package resonantinduction.archaic.waila;

import resonant.lib.modproxy.ICompatProxy;
import resonant.lib.utility.Mods;
import cpw.mods.fml.common.event.FMLInterModComms;

/**
 * @since 21/03/14
 * @author tgame14
 */
public class Waila implements ICompatProxy
{
    @Override
    public void preInit ()
    {
        // nothing
    }

    @Override
    public void init ()
    {
        FMLInterModComms.sendMessage(Mods.WAILA(), "register", "resonantinduction.archaic.waila.WailaRegistrar.wailaCallBack");
    }

    @Override
    public void postInit ()
    {
        // nothing
    }

    @Override
    public String modId ()
    {
        return Mods.WAILA();
    }
}
